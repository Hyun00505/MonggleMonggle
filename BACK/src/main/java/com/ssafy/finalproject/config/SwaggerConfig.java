package com.ssafy.finalproject.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        // JWT 인증 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
        
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");
        
        return new OpenAPI()
                .info(new Info()
                        .title("꿈 일기 & AI 해몽 서비스 API")
                        .description("캘린더 기반 꿈 일기 작성 및 AI 해몽, 운세, 행운의 색 제공 서비스")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("지민이팀")
                                .email("hb000122ssafy@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.your-domain.com")
                                .description("운영 서버")))
                .tags(List.of(
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("01. 인증 API")
                                .description("회원가입, 로그인, 사용자 정보 관리 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("02. 꿈 일기 API")
                                .description("꿈 일기 작성, 조회, 수정, 삭제 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("03. AI 분석 결과 API")
                                .description("꿈 해몽 및 운세 분석 결과 관리 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("04. 감정 점수 API")
                                .description("감정 점수 목록 조회 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("05. 월별 분석 API")
                                .description("월별 꿈 통계 및 AI 리포트 생성 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("06. 월별 메모 API")
                                .description("월별 메모 작성, 조회, 삭제 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("07. 공지사항 API")
                                .description("공지사항 작성, 조회, 수정, 삭제 API (관리자 전용 작성/수정/삭제)"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("08. 공지사항 댓글 API")
                                .description("공지사항 댓글 작성, 조회, 삭제 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("09. 공지사항 좋아요 API")
                                .description("공지사항 좋아요 토글 및 조회 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("10. 통합 운세 API")
                                .description("꿈 해몽 + 오늘의 운세 통합 분석 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("11. 꿈 이미지 생성 API")
                                .description("AI 꿈 이미지 생성 API (코인 차감 포함)"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("12. 이미지 업로드 API")
                                .description("이미지 업로드 및 삭제 API"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("13. 랭킹 API")
                                .description("사용자 꿈 일기 랭킹 조회 API")
                ))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}

