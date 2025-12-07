package com.ssafy.finalproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:3000", 
                        "http://localhost:5173",
                        "https://*.ngrok-free.app",  // ngrok 무료 도메인
                        "https://*.ngrok.io"         // ngrok 기존 도메인
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        
        // 업로드 이미지에 대한 CORS 설정
        registry.addMapping("/uploads/**")
                .allowedOriginPatterns(
                        "http://localhost:3000", 
                        "http://localhost:5173",
                        "https://*.ngrok-free.app",
                        "https://*.ngrok.io"
                )
                .allowedMethods("GET")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** URL로 접근 시 실제 파일 시스템 경로에서 서빙
        // URL: /uploads/images/dream/1/xxx.png -> 파일: uploads/images/dream/1/xxx.png
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600); // 1시간 캐시
    }
}

