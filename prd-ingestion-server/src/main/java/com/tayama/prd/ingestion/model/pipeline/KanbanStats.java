package com.tayama.prd.ingestion.model.pipeline;

import java.util.Map;

/**
 * 看板统计。
 */
public class KanbanStats {
    private Map<String, ColumnInfo> columns;

    public Map<String, ColumnInfo> getColumns() { return columns; }
    public void setColumns(Map<String, ColumnInfo> columns) { this.columns = columns; }

    public static class ColumnInfo {
        private int count;
        private String label;

        public ColumnInfo() {}

        public ColumnInfo(int count, String label) {
            this.count = count;
            this.label = label;
        }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
}