package com.gateway.client.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 CORS 配置，允许前端（Vite dev/preview）跨域访问后端 API。
 * 仅针对 /api/** 路径放行预检与实际请求。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 允许本机开发与预览地址（localhost 与 127.0.0.1）
                .allowedOrigins(
                        "http://localhost:4173",
                        "http://127.0.0.1:4173",
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        // 补充：Vite dev 端口占用时会自动切换到 5174
                        "http://localhost:5174",
                        "http://127.0.0.1:5174"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // 不携带凭证，避免与通配域冲突
                .allowCredentials(false)
                // 预检缓存 1 小时
                .maxAge(3600);
    }
}
