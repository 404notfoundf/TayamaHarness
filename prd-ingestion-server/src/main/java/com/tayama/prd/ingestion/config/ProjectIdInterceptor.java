package com.tayama.prd.ingestion.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 项目 ID 拦截器：从 {@code X-Project-Id} 请求头读取当前项目 ID 写入
 * {@link ProjectContext}，请求结束后清理。
 */
@Component
public class ProjectIdInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Project-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        ProjectContext.set(request.getHeader(HEADER));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        ProjectContext.clear();
    }
}