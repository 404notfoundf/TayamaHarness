package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.model.prd.ArchitectureDecision;
import com.tayama.prd.ingestion.model.prd.DataEntity;
import com.tayama.prd.ingestion.model.prd.InterfaceProtocol;
import com.tayama.prd.ingestion.model.prd.RequirementEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节树流水线的统一输出：四类提取结果，供 {@code simulateParsing} 落库与文档渲染共用。
 */
final class ParseOutcome {

    final List<RequirementEntity> requirements = new ArrayList<>();
    final List<DataEntity> dataEntities = new ArrayList<>();
    final List<InterfaceProtocol> interfaces = new ArrayList<>();
    final List<ArchitectureDecision> archDecisions = new ArrayList<>();

    int totalItems() {
        return requirements.size() + dataEntities.size() + interfaces.size() + archDecisions.size();
    }
}