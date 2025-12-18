package com.ssafy.finalproject.service;

import com.ssafy.finalproject.exception.BadRequestException;
import com.ssafy.finalproject.model.dto.fastapi.DreamImageGenerationRequestDto;
import com.ssafy.finalproject.model.dto.fastapi.DreamImageGenerationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * 꿈 이미지 생성 서비스
 * FastAPI 프록시 + 코인 차감
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DreamImageService {
    
    private final CoinService coinService;
    private final RestTemplate restTemplate;
    
    @Value("${fastapi.url}")
    private String fastApiUrl;
    
    /**
     * 꿈 이미지 생성 (코인 차감 + FastAPI 호출)
     * @param userId 사용자 ID
     * @param request 이미지 생성 요청
     * @return 생성된 이미지 데이터
     */
    @Transactional
    public DreamImageGenerationResponseDto generateDreamImage(Long userId, DreamImageGenerationRequestDto request) {
        log.info("=== 이미지 생성 시작: userId={}, style={} ===", userId, request.getStyle());
        
        // 0. 코인 충분한지 먼저 검증
        coinService.validateCoinForImageVisualization(userId);
        
        // 1. 코인 차감 (꿈 시각화 1회 = 2코인)
        try {
            coinService.consumeForImageVisualization(userId);
            log.info("✅ 코인 차감 성공: userId={}", userId);
        } catch (Exception e) {
            log.error("❌ 코인 차감 실패: userId={}", userId, e);
            throw e; // 코인 차감 실패 시 즉시 종료
        }
        
        // 2. FastAPI 호출
        try {
            String url = fastApiUrl + "/api/v1/dream/generate-image";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<DreamImageGenerationRequestDto> entity = new HttpEntity<>(request, headers);
            
            log.info("📤 FastAPI 요청: URL={}", url);
            ResponseEntity<DreamImageGenerationResponseDto> response = restTemplate.postForEntity(
                    url,
                    entity,
                    DreamImageGenerationResponseDto.class
            );
            
            DreamImageGenerationResponseDto responseBody = response.getBody();
            
            if (responseBody == null || !Boolean.TRUE.equals(responseBody.getSuccess())) {
                String errorMessage = responseBody != null ? responseBody.getMessage() : "FastAPI 응답이 null입니다.";
                log.error("❌ FastAPI 이미지 생성 실패: {}", errorMessage);
                
                // FastAPI 실패 시 코인 환불
                refundCoin(userId);
                
                throw new BadRequestException("이미지 생성에 실패했습니다: " + errorMessage);
            }
            
            log.info("✅ 이미지 생성 성공: userId={}, imageCount={}", userId, 
                    responseBody.getImages() != null ? responseBody.getImages().size() : 0);
            
            return responseBody;
            
        } catch (BadRequestException e) {
            // 이미 코인 환불 처리됨
            throw e;
        } catch (Exception e) {
            log.error("❌ FastAPI 호출 중 오류 발생", e);
            
            // FastAPI 호출 실패 시 코인 환불
            refundCoin(userId);
            
            throw new BadRequestException("이미지 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 코인 환불 (이미지 생성 실패 시)
     * @param userId 사용자 ID
     */
    private void refundCoin(Long userId) {
        try {
            // 환불 로직: 코인 2개 복구
            // UserDao에 refund 메서드가 없으므로, 수동으로 UPDATE 실행
            // 또는 CoinService에 refund 메서드를 추가해야 함
            // 임시로 로그만 남김
            log.warn("⚠️ 코인 환불 필요: userId={}, amount=2", userId);
            // TODO: 코인 환불 로직 구현 필요
        } catch (Exception e) {
            log.error("❌ 코인 환불 실패: userId={}", userId, e);
        }
    }
}
