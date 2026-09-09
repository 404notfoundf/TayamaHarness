package com.tayama.prd.ingestion.controller;

import com.tayama.prd.ingestion.model.response.BaseResponse;
import com.tayama.prd.ingestion.service.FileUploadService;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传 REST 控制器（MinIO 分块上传）。
 *
 * <p>支持分块上传、断点续传、合并、进度查询。</p>
 */
@RestController
@RequestMapping("/files")
public class FileUploadController {

    private final FileUploadService uploadService;
    private final MinioClient minioClient;

    public FileUploadController(FileUploadService uploadService,
                                 @Autowired(required = false) MinioClient minioClient) {
        this.uploadService = uploadService;
        this.minioClient = minioClient;
    }

    /**
     * MinIO 未启用时的统一错误响应。
     */
    private ResponseEntity<BaseResponse<Map<String, Object>>> minioDisabled() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(BaseResponse.error("MINIO_DISABLED", "MinIO 未启用，无法执行文件操作"));
    }

    /**
     * POST /api/v1/files/uploadChunk - 上传一个分片。
     *
     * <p>multipart/form-data 字段：</p>
     * <ul>
     *   <li>file: 分片文件</li>
     *   <li>fileMd5: 整个文件的 MD5</li>
     *   <li>fileName: 原始文件名</li>
     *   <li>chunkIndex: 分片索引(0-based)</li>
     *   <li>totalChunks: 总分片数</li>
     * </ul>
     */
    @PostMapping(value = "/uploadChunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<Map<String, Object>>> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("fileName") String fileName,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks) {

        if (minioClient == null) return minioDisabled();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", "分片文件不能为空"));
        }

        Map<String, Object> result = uploadService.uploadChunk(fileMd5, fileName, chunkIndex, totalChunks, file);
        return ResponseEntity.ok(BaseResponse.ok(result));
    }

    /**
     * POST /api/v1/files/uploadMerge - 合并文件分片。
     */
    @PostMapping("/uploadMerge")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<Map<String, Object>>> mergeChunks(
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("fileName") String fileName) {

        if (minioClient == null) return minioDisabled();

        String status = uploadService.getFileStatus(fileMd5);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "文件未找到，请先上传分片"));
        }
        if ("MERGING".equals(status) || "PARSING".equals(status) || "COMPLETED".equals(status)) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_STATE", "文件已在合并中或已完成合并"));
        }

        Map<String, Object> result = uploadService.mergeChunks(fileMd5, fileName);
        return ResponseEntity.ok(BaseResponse.ok(result));
    }

    /**
     * GET /api/v1/files/uploadProgress?fileMd5=xxx&totalChunks=10 - 查询上传进度。
     */
    @GetMapping("/uploadProgress")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getUploadProgress(
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("totalChunks") int totalChunks) {

        if (minioClient == null) return minioDisabled();
        Map<String, Object> progress = uploadService.getUploadProgress(fileMd5, totalChunks);
        return ResponseEntity.ok(BaseResponse.ok(progress));
    }
}