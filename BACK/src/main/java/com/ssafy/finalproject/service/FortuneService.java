package com.ssafy.finalproject.service;

import com.ssafy.finalproject.exception.ServiceUnavailableException;
import com.ssafy.finalproject.model.dto.fastapi.ComprehensiveFortuneRequestDto;
import com.ssafy.finalproject.model.dto.fastapi.ComprehensiveFortuneResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * 통합 운세 서비스
 * FastAPI의 통합 운세 API를 호출하여 꿈 해몽 + 오늘의 운세 결과를 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneService {
    
    private final WebClient webClient;
    
    /**
     * 통합 운세 조회 (꿈 해몽 + 오늘의 운세)
     * @param request 통합 운세 요청 DTO
     * @return 통합 운세 응답 DTO
     */
    public ComprehensiveFortuneResponseDto getComprehensiveFortune(ComprehensiveFortuneRequestDto request) {
        log.info("=== 통합 운세 조회 시작 ===");
        log.info("요청 정보 - 이름: {}, 성별: {}, 생년월일: {}", 
                request.getName(), request.getGender(), request.getBirthDate());
        log.debug("꿈 내용: {}", request.getDreamContent());
        
        try {
            ComprehensiveFortuneResponseDto response = webClient.post()
                    .uri("/api/v1/fortune/comprehensive")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ComprehensiveFortuneResponseDto.class)
                    .timeout(Duration.ofSeconds(120))  // 2분 타임아웃 (LLM 처리 시간 고려)
                    .block();
            
            if (response == null) {
                log.error("FastAPI 응답이 null입니다.");
                throw new ServiceUnavailableException("AI 서비스로부터 응답을 받지 못했습니다.");
            }
            
            log.info("✅ 통합 운세 조회 성공");
            log.debug("꿈 해몽 결과 길이: {}", 
                    response.getDreamInterpretation() != null ? response.getDreamInterpretation().length() : 0);
            
            return response;
            
        } catch (WebClientResponseException e) {
            log.error("❌ FastAPI 호출 실패 - 상태코드: {}, 응답: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceUnavailableException("AI 서비스 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ 통합 운세 조회 중 오류 발생", e);
            throw new ServiceUnavailableException("AI 서비스 연결에 실패했습니다: " + e.getMessage());
        }
    }
}

