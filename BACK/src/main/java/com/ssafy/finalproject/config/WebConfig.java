package com.ssafy.finalproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // BACK 폴더 내 uploads/images를 기본값으로 사용
    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;
    
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
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
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // /uploads/** URL로 접근 시 실제 파일 시스템 경로에서 서빙
        // URL: /uploads/images/dream/1/xxx.png -> 파일: uploads/images/dream/1/xxx.png
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        // 기본 업로드 디렉터리(uploads/images)를 기준으로 상위 uploads 폴더를 노출
        Path baseUploadPath = uploadPath.getParent() != null ? uploadPath.getParent() : uploadPath;

        String uploadUri = uploadPath.toUri().toString();
        String baseUploadUri = baseUploadPath.toUri().toString();

        // 경로 끝에 슬래시 보장 (리소스 해석 오류 방지)
        if (!uploadUri.endsWith("/")) {
            uploadUri = uploadUri + "/";
        }
        if (!baseUploadUri.endsWith("/")) {
            baseUploadUri = baseUploadUri + "/";
        }

        registry.addResourceHandler("/uploads/**")
                // OS 독립적 file:///C:/... 형태 + 하위/상위 경로 모두 노출
                .addResourceLocations(baseUploadUri, uploadUri)
                .setCachePeriod(3600); // 1시간 캐시
    }
}

