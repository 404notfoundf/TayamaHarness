package com.tayama.prd.ingestion.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文件上传（MinIO 分块上传）数据访问层。
 */
@Repository
public class FileUploadRepository {

    private final JdbcTemplate jdbc;

    public FileUploadRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---- File Metadata ----

    public void insertFileMetadata(String fileMd5, String fileName, long fileSize, int totalChunks, String contentType, String status, String projectId) {
        jdbc.update("INSERT INTO prd_file_metadata (project_id, file_md5, file_name, file_size, total_chunks, content_type, status) VALUES (?,?,?,?,?,?,?)",
                projectId, fileMd5, fileName, fileSize, totalChunks, contentType, status);
    }

    public void updateFileStatus(String fileMd5, String status) {
        jdbc.update("UPDATE prd_file_metadata SET status=? WHERE file_md5=?", status, fileMd5);
    }

    public void updateFileMinioInfo(String fileMd5, String minioObject, String minioUrl) {
        jdbc.update("UPDATE prd_file_metadata SET minio_object=?, minio_url=?, status='COMPLETED' WHERE file_md5=?",
                minioObject, minioUrl, fileMd5);
    }

    public boolean existsByFileMd5(String fileMd5) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM prd_file_metadata WHERE file_md5=?", Integer.class, fileMd5);
        return count != null && count > 0;
    }

    public String findFileStatus(String fileMd5) {
        List<String> results = jdbc.query("SELECT status FROM prd_file_metadata WHERE file_md5=?",
                (rs, row) -> rs.getString("status"), fileMd5);
        return results.isEmpty() ? null : results.get(0);
    }

    public String findFileName(String fileMd5) {
        List<String> results = jdbc.query("SELECT file_name FROM prd_file_metadata WHERE file_md5=?",
                (rs, row) -> rs.getString("file_name"), fileMd5);
        return results.isEmpty() ? null : results.get(0);
    }

    public String findMinioObject(String fileMd5) {
        List<String> results = jdbc.query("SELECT minio_object FROM prd_file_metadata WHERE file_md5=?",
                (rs, row) -> rs.getString("minio_object"), fileMd5);
        return results.isEmpty() ? null : results.get(0);
    }

    // ---- File Chunks ----

    public void insertChunk(String fileMd5, String chunkMd5, int chunkIndex, long chunkSize, String minioObjectName, String projectId) {
        jdbc.update("INSERT INTO prd_file_chunks (project_id, file_md5, file_chunk_md5, file_chunk_index, file_chunk_size, minio_object_name) VALUES (?,?,?,?,?,?)",
                projectId, fileMd5, chunkMd5, chunkIndex, chunkSize, minioObjectName);
    }

    public boolean chunkExists(String fileMd5, int chunkIndex) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM prd_file_chunks WHERE file_md5=? AND file_chunk_index=?",
                Integer.class, fileMd5, chunkIndex);
        return count != null && count > 0;
    }

    public int countChunks(String fileMd5) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM prd_file_chunks WHERE file_md5=?", Integer.class, fileMd5);
        return count != null ? count : 0;
    }

    public List<Integer> findUploadedChunkIndices(String fileMd5) {
        return jdbc.queryForList(
                "SELECT file_chunk_index FROM prd_file_chunks WHERE file_md5=? ORDER BY file_chunk_index",
                Integer.class, fileMd5);
    }

    public List<String> findChunkObjectNames(String fileMd5) {
        return jdbc.queryForList(
                "SELECT minio_object_name FROM prd_file_chunks WHERE file_md5=? ORDER BY file_chunk_index",
                String.class, fileMd5);
    }

    public void deleteChunksByFileMd5(String fileMd5) {
        jdbc.update("DELETE FROM prd_file_chunks WHERE file_md5=?", fileMd5);
    }

    public void deleteFileMetadata(String fileMd5) {
        jdbc.update("DELETE FROM prd_file_metadata WHERE file_md5=?", fileMd5);
    }
}