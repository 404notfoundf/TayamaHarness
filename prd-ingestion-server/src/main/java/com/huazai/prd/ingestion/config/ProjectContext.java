package com.huazai.prd.ingestion.config;

/**
 * 当前请求的项目上下文（ThreadLocal）。
 * <p>
 * 由 {@link ProjectIdInterceptor} 在请求进入时从 {@code X-Project-Id} 请求头写入，
 * 请求结束时清理。Service / Repository 层可直接读取，实现所有接口自动携带 project_id。
 */
public final class ProjectContext {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private ProjectContext() {
    }

    /** 写入当前项目 ID（可为 null）。 */
    public static void set(String projectId) {
        HOLDER.set(projectId);
    }

    /** 获取当前项目 ID；请求未携带时为 null。 */
    public static String get() {
        return HOLDER.get();
    }

    /** 是否有项目上下文。 */
    public static boolean hasProject() {
        String pid = HOLDER.get();
        return pid != null && !pid.isBlank();
    }

    /** 请求结束时清理，防止线程池复用导致串号。 */
    public static void clear() {
        HOLDER.remove();
    }
}