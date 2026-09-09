package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.config.ProjectContext;
import com.tayama.prd.ingestion.model.pipeline.*;
import com.tayama.prd.ingestion.repository.ChangeRepository;
import com.tayama.prd.ingestion.repository.PipelineRepository;
import com.tayama.prd.ingestion.repository.PrdIngestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流水线管理服务。
 */
@Service
public class PipelineService {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineService.class);

    private final PipelineRepository pipelineRepo;
    private final ChangeRepository changeRepo;
    private final PrdIngestionRepository prdRepo;

    public PipelineService(PipelineRepository pipelineRepo, ChangeRepository changeRepo, PrdIngestionRepository prdRepo) {
        this.pipelineRepo = pipelineRepo;
        this.changeRepo = changeRepo;
        this.prdRepo = prdRepo;
    }

    public PipelineStatus getPipelineStatus(String changeId) {
        List<PipelineStatus.StageInfo> stages = pipelineRepo.findStages(changeId);
        if (stages.isEmpty()) return null;

        PipelineStatus status = new PipelineStatus();
        status.setChangeId(changeId);
        status.setStages(stages);

        // 计算当前阶段和进度
        String currentStage = "drafting";
        int completedCount = 0;
        for (PipelineStatus.StageInfo s : stages) {
            if ("active".equals(s.getStatus())) {
                currentStage = s.getStage();
            }
            if ("completed".equals(s.getStatus())) {
                completedCount++;
            }
        }
        status.setCurrentStage(currentStage);
        status.setProgress(stages.isEmpty() ? 0 : (completedCount * 100 / stages.size()));
        return status;
    }

    public List<ChangeLogEntry> getChangeLogs(String changeId) {
        return pipelineRepo.findLogs(changeId);
    }

    public PipelineStatus advancePipeline(String changeId, String stage) {
        // 验证阶段是否有效
        String[] validStages = {"drafting", "reviewing", "approved", "completed"};
        boolean valid = false;
        for (String s : validStages) {
            if (s.equals(stage)) { valid = true; break; }
        }
        if (!valid) {
            throw new IllegalArgumentException("无效的流水线阶段: " + stage);
        }

        pipelineRepo.advanceStage(changeId, stage);
        changeRepo.updateChangeStatus(changeId, stage);
        try {
            pipelineRepo.insertLog(changeId, "status_change", "流水线推进至: " + stage, null, "system", ProjectContext.get());
        } catch (RuntimeException e) {
            // 日志仅为辅助记录，记录失败不应阻断流水线推进
            LOG.warn("记录流水线推进日志失败 changeId={}, stage={}", changeId, stage, e);
        }

        return getPipelineStatus(changeId);
    }

    public TraceNode getRequirementTrace(String reqId) {
        // 构建追溯树
        TraceNode root = new TraceNode();
        root.setId(reqId);
        root.setLabel("需求: " + reqId);
        root.setType("requirement");
        root.setStatus("active");

        TraceNode docNode = new TraceNode();
        docNode.setId("doc-ref-" + reqId);
        docNode.setLabel("文档引用");
        docNode.setType("document");
        docNode.setStatus("draft");

        TraceNode changeNode = new TraceNode();
        changeNode.setId("change-ref-" + reqId);
        changeNode.setLabel("Change 引用");
        changeNode.setType("change");
        changeNode.setStatus("drafting");

        docNode.setChildren(List.of(changeNode));
        root.setChildren(List.of(docNode));
        return root;
    }

    public KanbanStats getKanbanStats() {
        String projectId = ProjectContext.get();
        KanbanStats stats = new KanbanStats();
        Map<String, KanbanStats.ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("prd_imported", new KanbanStats.ColumnInfo(pipelineRepo.countByStatus("drafting", projectId), "PRD 导入"));
        columns.put("document_review", new KanbanStats.ColumnInfo(pipelineRepo.countByStatus("reviewing", projectId), "文档校验"));
        columns.put("design_review", new KanbanStats.ColumnInfo(pipelineRepo.countByStatus("approved", projectId), "设计评审"));
        columns.put("completed", new KanbanStats.ColumnInfo(pipelineRepo.countByStatus("completed", projectId), "已完成"));
        stats.setColumns(columns);
        return stats;
    }
}