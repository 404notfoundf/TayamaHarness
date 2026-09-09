package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.config.ProjectContext;
import com.tayama.prd.ingestion.repository.FileUploadRepository;
import io.minio.*;
import io.minio.http.Method;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * MinIO 分块上传服务。
 *
 * <p>支持流程：前端将文件分割为 5MB 分片 → 逐片上传到 MinIO →
 * 全部上传完成后调用合并 → 读取文件内容用于 PRD 解析。</p>
 */
@Service
public class FileUploadService {

    private static final Logger LOG = LoggerFactory.getLogger(FileUploadService.class);

    /** 分片默认大小 5MB */
    public static final long DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024L;

    private MinioClient minioClient;
    private final String bucketUploads;
    private final String bucketMerged;
    private final FileUploadRepository repo;

    public FileUploadService(@Autowired(required = false) MinioClient minioClient,
                             FileUploadRepository repo,
                             @Value("${minio.bucket-uploads:prd-uploads}") String bucketUploads,
                             @Value("${minio.bucket-merged:prd-merged}") String bucketMerged) {
        this.minioClient = minioClient;
        this.repo = repo;
        this.bucketUploads = bucketUploads;
        this.bucketMerged = bucketMerged;
        initBucketsAndCleanup();
    }

    /** 初始化存储桶 */
    private void initBucketsAndCleanup() {
        if (minioClient == null) {
            LOG.info("MinIO 未启用，跳过存储桶初始化");
            return;
        }
        for (String bucket : new String[]{bucketUploads, bucketMerged}) {
            try {
                boolean exists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    minioClient.makeBucket(
                            MakeBucketArgs.builder().bucket(bucket).build());
                    LOG.info("Created MinIO bucket: {}", bucket);
                }
            } catch (Exception e) {
                LOG.warn("Failed to init MinIO bucket '{}' (may be fine if it exists): {}", bucket, e.getMessage());
            }
        }
    }

    /**
     * 上传一个分片到 MinIO。
     */
    public Map<String, Object> uploadChunk(String fileMd5, String fileName, int chunkIndex, int totalChunks, MultipartFile file) {
        if (minioClient == null) {
            throw new RuntimeException("MinIO 未启用，无法上传分片");
        }
        try {
            // 从当前请求上下文（X-Project-Id header → ProjectContext）取项目 ID
            String projectId = ProjectContext.get();
            // 计算分片 MD5
            byte[] fileBytes = file.getBytes();
            String chunkMd5 = DigestUtils.md5Hex(fileBytes);

            // 检查是否已上传（断点续传）
            if (repo.chunkExists(fileMd5, chunkIndex)) {
                LOG.info("Chunk already uploaded, skip. fileMd5={}, chunkIndex={}", fileMd5, chunkIndex);
                return buildChunkResult(fileMd5, chunkIndex, totalChunks, file.getSize());
            }

            // MinIO 分片路径
            String chunkPath = "file-chunks/" + fileMd5 + "/" + chunkIndex;

            // 上传到 MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketUploads)
                    .object(chunkPath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .build());

            LOG.debug("Chunk uploaded to MinIO: fileMd5={}, chunkIndex={}, path={}, size={}",
                    fileMd5, chunkIndex, chunkPath, file.getSize());

            // 写入数据库记录
            repo.insertChunk(fileMd5, chunkMd5, chunkIndex, file.getSize(), chunkPath, projectId);

            // 如果是第一个分片，创建文件元数据
            if (chunkIndex == 0 && !repo.existsByFileMd5(fileMd5)) {
                String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
                repo.insertFileMetadata(fileMd5, fileName, file.getSize() * totalChunks, totalChunks, contentType, "UPLOADING", projectId);
            }

            return buildChunkResult(fileMd5, chunkIndex, totalChunks, file.getSize());

        } catch (Exception e) {
            LOG.error("Failed to upload chunk: fileMd5={}, chunkIndex={}", fileMd5, chunkIndex, e);
            throw new RuntimeException("分片上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询上传进度。
     */
    public Map<String, Object> getUploadProgress(String fileMd5, int totalChunks) {
        List<Integer> uploaded = repo.findUploadedChunkIndices(fileMd5);
        double progress = totalChunks > 0 ? (double) uploaded.size() / totalChunks * 100 : 0;
        String status = repo.findFileStatus(fileMd5);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileMd5", fileMd5);
        result.put("uploadedChunks", uploaded);
        result.put("totalChunks", totalChunks);
        result.put("progress", Math.round(progress * 100.0) / 100.0);
        result.put("status", status != null ? status : "UPLOADING");
        return result;
    }

    /**
     * 合并文件分片。
     *
     * <p>使用 MinIO composeObject 将分片合并为一个完整文件，
     * 合并后返回预签名 URL 供后续读取。</p>
     */
    public Map<String, Object> mergeChunks(String fileMd5, String fileName) {
        if (minioClient == null) {
            throw new RuntimeException("MinIO 未启用，无法合并分片");
        }
        // 1. 更新状态为合并中
        repo.updateFileStatus(fileMd5, "MERGING");

        // 2. 获取所有分片路径
        List<String> chunkObjectNames = repo.findChunkObjectNames(fileMd5);
        String mergedObjectName = "file-merged/" + fileMd5 + "/" + fileName;

        try {
            // 3. 使用 MinIO composeObject 合并
            List<ComposeSource> sources = chunkObjectNames.stream()
                    .map(obj -> ComposeSource.builder()
                            .bucket(bucketUploads)
                            .object(obj)
                            .build())
                    .collect(Collectors.toList());

            minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(bucketMerged)
                    .object(mergedObjectName)
                    .sources(sources)
                    .build());

            LOG.info("File merged in MinIO: fileMd5={}, mergedObject={}, bucket={}", fileMd5, mergedObjectName, bucketMerged);

            // 4. 验证合并结果
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketMerged)
                    .object(mergedObjectName)
                    .build());
            LOG.info("Merged file verified: size={}", stat.size());

            // 5. 生成预签名 URL
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketMerged)
                            .object(mergedObjectName)
                            .expiry(24, TimeUnit.HOURS)
                            .build());

            LOG.info("Presigned URL generated: fileMd5={}", fileMd5);

            // 6. 更新数据库（状态改为 PARSING 表示可解析）
            repo.updateFileMinioInfo(fileMd5, mergedObjectName, presignedUrl);

            // 7. 清理分片（可选：删除已合并的分片）
            cleanupChunks(chunkObjectNames);

            // 返回结果
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fileMd5", fileMd5);
            result.put("fileName", fileName);
            result.put("status", "PARSING");
            result.put("minioObject", mergedObjectName);
            result.put("presignedUrl", presignedUrl);
            return result;

        } catch (Exception e) {
            LOG.error("Failed to merge chunks: fileMd5={}", fileMd5, e);
            repo.updateFileStatus(fileMd5, "FAILED");
            throw new RuntimeException("文件合并失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 MinIO 读取已合并文件的内容（文本）。
     */
    public String readFileContent(String fileMd5) {
        if (minioClient == null) {
            throw new RuntimeException("MinIO 未启用，无法读取文件");
        }
        String minioObject = repo.findMinioObject(fileMd5);
        if (minioObject == null) {
            throw new RuntimeException("文件未找到或尚未合并: " + fileMd5);
        }
        try {
            try (var response = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketMerged).object(minioObject).build())) {
                return new String(response.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOG.error("Failed to read file from MinIO: fileMd5={}, object={}", fileMd5, minioObject, e);
            throw new RuntimeException("从 MinIO 读取文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件元数据状态。
     */
    public String getFileStatus(String fileMd5) {
        return repo.findFileStatus(fileMd5);
    }

    public String getFileName(String fileMd5) {
        return repo.findFileName(fileMd5);
    }

    /**
     * 将 PRD 文本内容保存到 MinIO prd-merged 桶做持久备份。
     *
     * <p>无论 PRD 来源是文本粘贴还是文件上传，都调用此方法确保原始内容落入 MinIO。</p>
     *
     * @param ingestionId 解析记录 ID，用于构造对象路径
     * @param title       PRD 标题，用于文件名
     * @param content     PRD 原始 Markdown 文本
     */
    public void savePrdContent(String ingestionId, String title, String content) {
        if (minioClient == null) {
            LOG.info("[savePrdContent] MinIO 未启用，跳过保存: ingestionId={}", ingestionId);
            return;
        }
        if (ingestionId == null || content == null || content.isBlank()) {
            LOG.warn("[savePrdContent] 跳过保存：参数不完整，ingestionId={}, contentLength={}",
                    ingestionId, content == null ? 0 : content.length());
            return;
        }
        try {
            String safeTitle = title != null ? title.replaceAll("[\\\\/:*?\"<>|]", "_") : "untitled";
            // 路径：prd-files/{ingestionId}/{title}.md，与分片上传的 file-merged/ 路径不冲突
            String objectName = "prd-files/" + ingestionId + "/" + safeTitle + ".md";
            byte[] contentBytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketMerged)
                    .object(objectName)
                    .stream(new java.io.ByteArrayInputStream(contentBytes), contentBytes.length, -1)
                    .contentType("text/markdown")
                    .build());

            LOG.info("[savePrdContent] PRD 已保存到 MinIO: ingestionId={}, object={}, bucket={}, size={}",
                    ingestionId, objectName, bucketMerged, contentBytes.length);
        } catch (Exception e) {
            // MinIO 写入失败不应阻断解析主流程，仅记录警告
            LOG.warn("[savePrdContent] 保存到 MinIO 失败: ingestionId={}, error={}", ingestionId, e.getMessage());
        }
    }

    // ---- Private helpers ----

    private Map<String, Object> buildChunkResult(String fileMd5, int chunkIndex, int totalChunks, long size) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileMd5", fileMd5);
        result.put("chunkIndex", chunkIndex);
        result.put("totalChunks", totalChunks);
        result.put("size", size);
        return result;
    }

    /** 清理已合并的分片 */
    private void cleanupChunks(List<String> chunkObjectNames) {
        if (minioClient == null) return;
        for (String obj : chunkObjectNames) {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder().bucket(bucketUploads).object(obj).build());
            } catch (Exception e) {
                LOG.warn("Failed to remove chunk: {}", obj, e);
            }
        }
        // 清理数据库记录
        if (!chunkObjectNames.isEmpty()) {
            String fileMd5 = chunkObjectNames.get(0).replaceAll("^file-chunks/([^/]+)/.*$", "$1");
            // 提取 fileMd5 失败则直接从 chunk 路径提取
            int firstSlash = chunkObjectNames.get(0).indexOf('/');
            int secondSlash = chunkObjectNames.get(0).indexOf('/', firstSlash + 1);
            if (secondSlash > 0) {
                String fmd5 = chunkObjectNames.get(0).substring(firstSlash + 1, secondSlash);
                repo.deleteChunksByFileMd5(fmd5);
            }
        }
    }
}