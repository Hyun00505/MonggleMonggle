package com.ssafy.finalproject.service;

import com.ssafy.finalproject.exception.ForbiddenException;
import com.ssafy.finalproject.exception.ResourceNotFoundException;
import com.ssafy.finalproject.model.dao.DreamsDao;
import com.ssafy.finalproject.model.dao.DreamsResultsDao;
import com.ssafy.finalproject.model.dao.EmotionDao;
import com.ssafy.finalproject.model.dto.request.CreateDreamRequest;
import com.ssafy.finalproject.model.dto.request.UpdateDreamRequest;
import com.ssafy.finalproject.model.dto.response.DreamListResponse;
import com.ssafy.finalproject.model.dto.response.DreamResponse;
import com.ssafy.finalproject.model.entity.Dream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DreamService {
    
    private final DreamsDao dreamsDao;
    private final DreamsResultsDao dreamsResultsDao;
    
    // 꿈 일기 작성 (같은 날짜에 이미 꿈이 있으면 업데이트)
    public DreamResponse createDream(Long userId, CreateDreamRequest request) {
        // 같은 날짜에 이미 꿈이 있는지 확인
        Dream existingDream = dreamsDao.findByUserIdAndDreamDate(userId, request.getDreamDate())
                .orElse(null);
        
        Dream dream;
        if (existingDream != null) {
            // 이미 존재하면 업데이트
            existingDream.setEmotionId(request.getEmotionId());
            existingDream.setTitle(request.getTitle());
            existingDream.setContent(request.getContent());
            dreamsDao.updateDream(existingDream);
            dream = existingDream;
        } else {
            // 없으면 새로 생성
            dream = Dream.builder()
                    .userId(userId)
                    .emotionId(request.getEmotionId())
                    .dreamDate(request.getDreamDate())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .build();
            dreamsDao.insertDream(dream);
        }
        
        return DreamResponse.builder()
                .dreamId(dream.getDreamId())
                .userId(dream.getUserId())
                .emotionId(dream.getEmotionId())
                .dreamDate(dream.getDreamDate())
                .title(dream.getTitle())
                .content(dream.getContent())
                .createdDate(dream.getCreatedDate())
                .build();
    }
    
    // 월별 꿈 일기 목록 조회
    @Transactional(readOnly = true)
    public DreamListResponse getDreamsByMonth(Long userId, Integer year, Integer month) {
        List<Dream> dreams = dreamsDao.findByUserIdAndYearMonth(userId, year, month);
        
        List<DreamListResponse.DreamSummary> summaries = dreams.stream()
                .map(dream -> {
                    // 해몽 결과 조회
                    var dreamResultOpt = dreamsResultsDao.findByDreamId(dream.getDreamId());
                    boolean hasResult = dreamResultOpt.isPresent();
                    
                    // 해몽 결과가 있으면 색상 정보도 함께 반환
                    String luckyColorName = null;
                    Integer luckyColorNumber = null;
                    if (hasResult) {
                        var dreamResult = dreamResultOpt.get();
                        luckyColorName = dreamResult.getLuckyColorName();
                        luckyColorNumber = dreamResult.getLuckyColorNumber();
                    }
                    
                    return DreamListResponse.DreamSummary.builder()
                            .dreamId(dream.getDreamId())
                            .title(dream.getTitle())
                            .content(dream.getContent())
                            .dreamDate(dream.getDreamDate().toString())
                            .emotionId(dream.getEmotionId())
                            .emotionName(null) // XML에서 조인하여 가져올 수 있음
                            .hasResult(hasResult)
                            .luckyColorName(luckyColorName)
                            .luckyColorNumber(luckyColorNumber)
                            .build();
                })
                .collect(Collectors.toList());
        
        return DreamListResponse.builder()
                .year(year)
                .month(month)
                .dreams(summaries)
                .build();
    }
    
    // 꿈 일기 상세 조회
    @Transactional(readOnly = true)
    public DreamResponse getDream(Long userId, Long dreamId) {
        Dream dream = dreamsDao.findById(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("꿈 일기를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!dream.getUserId().equals(userId)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        
        return DreamResponse.builder()
                .dreamId(dream.getDreamId())
                .userId(dream.getUserId())
                .emotionId(dream.getEmotionId())
                .dreamDate(dream.getDreamDate())
                .title(dream.getTitle())
                .content(dream.getContent())
                .createdDate(dream.getCreatedDate())
                .build();
    }
    
    // 꿈 일기 수정
    public void updateDream(Long userId, Long dreamId, UpdateDreamRequest request) {
        Dream dream = dreamsDao.findById(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("꿈 일기를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!dream.getUserId().equals(userId)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        
        dream.setEmotionId(request.getEmotionId());
        dream.setTitle(request.getTitle());
        dream.setContent(request.getContent());
        
        dreamsDao.updateDream(dream);
    }
    
    // 꿈 일기 삭제
    public void deleteDream(Long userId, Long dreamId) {
        Dream dream = dreamsDao.findById(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("꿈 일기를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!dream.getUserId().equals(userId)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        
        dreamsDao.deleteDream(dreamId);
    }
}

