package com.tayama.prd.ingestion.model.prd;

/**
 * 候选状态：confirmed=由 PRD 原文直接提取（可靠）；proposed=由规则推导生成（需人工确认）。
 */
public interface CandidateStatus {
    String CONFIRMED = "confirmed";
    String PROPOSED = "proposed";
}