package com.ssafy.finalproject.service;

import com.ssafy.finalproject.exception.ConflictException;
import com.ssafy.finalproject.exception.ForbiddenException;
import com.ssafy.finalproject.exception.ResourceNotFoundException;
import com.ssafy.finalproject.model.dao.DreamsDao;
import com.ssafy.finalproject.model.dao.DreamsResultsDao;
import com.ssafy.finalproject.model.dto.request.SaveDreamResultRequest;
import com.ssafy.finalproject.model.dto.request.UpdateDreamResultRequest;
import com.ssafy.finalproject.model.dto.response.DreamResultResponse;
import com.ssafy.finalproject.model.entity.Dream;
import com.ssafy.finalproject.model.entity.DreamResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class DreamResultService {
    
    private final DreamsResultsDao dreamsResultsDao;
    private final DreamsDao dreamsDao;
    private final ImageService imageService;
    private final CoinService coinService;
    
    // AI 분석 결과 저장
    public Long saveDreamResult(Long userId, Long dreamId, SaveDreamResultRequest request) {
        // 꿈 일기 존재 확인
        Dream dream = dreamsDao.findById(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("꿈 일기를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!dream.getUserId().equals(userId)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        
        // 이미 분석 결과가 있는지 확인
        if (dreamsResultsDao.existsByDreamId(dreamId)) {
            throw new ConflictException("이미 분석 결과가 존재합니다.");
        }
        
        // 코인 차감: 이미지 URL이 없는 경우에만 1코인 차감 (해몽만 요청)
        // 이미지가 있는 경우는 이미지 생성 시 2코인이 이미 차감됨
        if (request.getImageUrl() == null || request.getImageUrl().isEmpty()) {
            coinService.consumeForDreamInterpretation(userId);
        }
        
        // DreamResult 엔티티 생성
        DreamResult dreamResult = DreamResult.builder()
                .dreamId(dreamId)
                .dreamInterpretation(request.getDreamInterpretation())
                .todayFortuneSummary(request.getTodayFortuneSummary())
                .luckyColorName(request.getLuckyColor().getName())
                .luckyColorNumber(request.getLuckyColor().getNumber())
                .luckyColorReason(request.getLuckyColor().getReason())
                .luckyItemName(request.getLuckyItem().getName())
                .luckyItemReason(request.getLuckyItem().getReason())
                .imageUrl(request.getImageUrl())
                .build();
        
        dreamsResultsDao.insertDreamResult(dreamResult);
        
        return dreamResult.getId();
    }
    
    // AI 분석 결과 조회
    @Transactional(readOnly = true)
    public DreamResultResponse getDreamResult(Long userId, Long dreamId) {
        // 꿈 일기 존재 확인
        Dream dream = dreamsDao.findById(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("꿈 일기를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!dream.getUserId().equals(userId)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        
        // 분석 결과 조회
        DreamResult result = dreamsResultsDao.findByDreamId(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("분석 결과를 찾을 수 없습니다."));
        
        return DreamResultResponse.builder()
                .id(result.getId())
                .dreamId(result.getDreamId())
                .dreamInterpretation(result.getDreamInterpretation())
                .todayFortuneSummary(result.getTodayFortuneSummary())
                .luckyColor(DreamResultResponse.LuckyColorDto.builder()
                        .name(result.getLuckyColorName())
                        .number(result.getLuckyColorNumber())
                        .reason(result.getLuckyColorReason())
                        .build())
                .luckyItem(DreamResultResponse.LuckyItemDto.builder()
                        .name(result.getLuckyItemName())
                        .reason(result.getLuckyItemReason())
                        .build())
                .imageUrl(result.getImageUrl())
                .createdDate(result.getCreatedDate())
                .deletedDate(result.getDeletedDate())
                .build();
    }
    
    // AI 분석 결과 수정 (재해몽 포함)
    public Long updateDreamResult(Long userId, Long dreamId, UpdateDreamResultRequest request) {
        // 꿈 일기 존재 확인
        Dream dream = dreamsDao.findById(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("꿈 일기를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!dream.getUserId().equals(userId)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        
        // 분석 결과 조회
        DreamResult result = dreamsResultsDao.findByDreamId(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("분석 결과를 찾을 수 없습니다."));
        
        // 해몽/운세/행운 정보 업데이트
        if (request.getDreamInterpretation() != null) {
            result.setDreamInterpretation(request.getDreamInterpretation());
        }
        if (request.getTodayFortuneSummary() != null) {
            result.setTodayFortuneSummary(request.getTodayFortuneSummary());
        }
        if (request.getLuckyColor() != null) {
            if (request.getLuckyColor().getName() != null) {
                result.setLuckyColorName(request.getLuckyColor().getName());
            }
            if (request.getLuckyColor().getNumber() != null) {
                result.setLuckyColorNumber(request.getLuckyColor().getNumber());
            }
            if (request.getLuckyColor().getReason() != null) {
                result.setLuckyColorReason(request.getLuckyColor().getReason());
            }
        }
        if (request.getLuckyItem() != null) {
            if (request.getLuckyItem().getName() != null) {
                result.setLuckyItemName(request.getLuckyItem().getName());
            }
            if (request.getLuckyItem().getReason() != null) {
                result.setLuckyItemReason(request.getLuckyItem().getReason());
            }
        }
        
        // 이미지 URL 업데이트 시 이전 이미지 삭제
        if (request.getImageUrl() != null) {
            String newImageUrl = request.getImageUrl();
            String oldImageUrl = result.getImageUrl();
            
            // 빈 문자열이면 이미지 삭제 요청으로 처리
            if (newImageUrl.isEmpty()) {
                // 기존 이미지 파일 삭제
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    try {
                        imageService.deleteImage(oldImageUrl);
                    } catch (IOException e) {
                        // 이미지 삭제 실패해도 계속 진행 (로그만 남김)
                    }
                }
                // DB에서 이미지 URL을 null로 설정
                result.setImageUrl(null);
            } else {
                // 새 이미지 URL로 업데이트
                // 기존 이미지가 있고, 새 이미지와 다르면 기존 이미지 삭제
                if (oldImageUrl != null && !oldImageUrl.isEmpty() && !oldImageUrl.equals(newImageUrl)) {
                    try {
                        imageService.deleteImage(oldImageUrl);
                    } catch (IOException e) {
                        // 이미지 삭제 실패해도 계속 진행 (로그만 남김)
                    }
                }
                result.setImageUrl(newImageUrl);
            }
        }
        dreamsResultsDao.updateDreamResult(result);
        
        return result.getId();
    }
    
    // AI 분석 결과 삭제
    public void deleteDreamResult(Long userId, Long dreamId) {
        // 꿈 일기 존재 확인
        Dream dream = dreamsDao.findById(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("꿈 일기를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!dream.getUserId().equals(userId)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        
        // 기존 결과가 있으면 이미지부터 정리 (하드 삭제)
        dreamsResultsDao.findByDreamId(dreamId).ifPresent(result -> {
            if (result.getImageUrl() != null) {
                try {
                    imageService.deleteImage(result.getImageUrl());
                } catch (IOException e) {
                    throw new RuntimeException("꿈 해몽 이미지 삭제 중 오류가 발생했습니다.", e);
                }
            }
            // 결과 레코드 하드 삭제
            dreamsResultsDao.deleteDreamResult(dreamId);
        });
    }
    
    // 찜 토글
    public Boolean toggleLike(Long userId, Long dreamId) {
        // 꿈 일기 존재 확인
        Dream dream = dreamsDao.findById(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("꿈 일기를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!dream.getUserId().equals(userId)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        
        // 분석 결과 존재 확인
        DreamResult result = dreamsResultsDao.findByDreamId(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("분석 결과를 찾을 수 없습니다."));
        
        // 찜 토글
        dreamsResultsDao.toggleLike(dreamId);
        
        // 토글 후 상태 반환 (기존 값의 반대)
        return result.getIsLiked() == null || !result.getIsLiked();
    }
}

