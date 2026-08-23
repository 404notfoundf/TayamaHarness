package com.huazai.prd.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PRD 前置转换后端服务启动类。
 *
 * <p>提供 REST API 入口，支持 PRD 导入解析、文档生成、变更管理、流水线追踪等功能。</p>
 */
@SpringBootApplication
public class PrdIngestionApplication {

    private static final Logger LOG = LoggerFactory.getLogger(PrdIngestionApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PrdIngestionApplication.class, args);
        // 启动横幅：用于确认跑的是最新代码（含解析链路日志）。重启后看到此日志 = 新版本生效
        LOG.info("================================================================");
        LOG.info("[boot] PRD Ingestion Server 启动完成（含解析链路日志）");
        LOG.info("[boot] 提交 PRD 后请在控制台观察: [ingest-api] 请求进入 -> [parse] 各阶段 -> [ingest-api] 返回成功");
        LOG.info("================================================================");
    }
}