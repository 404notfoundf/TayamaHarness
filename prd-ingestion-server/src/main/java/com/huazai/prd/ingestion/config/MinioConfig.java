package com.huazai.prd.ingestion.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储客户端配置。
 *
 * <p>从环境变量 / application.yml 读取配置，支持 fail-fast 启动校验。</p>
 */
@Configuration(proxyBeanMethods = false)
public class MinioConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${minio.endpoint:http://127.0.0.1:9000}")
    private String endpoint;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${minio.bucket-uploads:prd-uploads}")
    private String bucketUploads;

    @Value("${minio.bucket-merged:prd-merged}")
    private String bucketMerged;

    @Bean
    @ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = true)
    public MinioClient minioClient() {
        LOG.info("Initializing MinIO client: endpoint={}, bucketUploads={}, bucketMerged={}",
                endpoint, bucketUploads, bucketMerged);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public String bucketUploads() {
        return bucketUploads;
    }

    @Bean
    public String bucketMerged() {
        return bucketMerged;
    }
}