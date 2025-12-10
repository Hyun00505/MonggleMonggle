package com.ssafy.finalproject.service;

import com.ssafy.finalproject.exception.ForbiddenException;
import com.ssafy.finalproject.exception.ResourceNotFoundException;
import com.ssafy.finalproject.model.dao.MonthlyMemoDao;
import com.ssafy.finalproject.model.dto.request.SaveMemoRequest;
import com.ssafy.finalproject.model.dto.response.MonthlyMemoResponse;
import com.ssafy.finalproject.model.entity.MonthlyMemo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyMemoService {
    
    private final MonthlyMemoDao monthlyMemoDao;
    
    // 월별 메모 조회
    @Transactional(readOnly = true)
    public List<MonthlyMemoResponse> getMemo(Long userId, Integer year, Integer month) {
        List<MonthlyMemo> memos = monthlyMemoDao.findByUserIdAndYearMonth(userId, year, month);
        return memos.stream()
                .map(memo -> MonthlyMemoResponse.builder()
                        .memoId(memo.getMemoId())
                        .year(memo.getYear())
                        .month(memo.getMonth())
                        .memoContent(memo.getMemoContent())
                        .createdDate(memo.getCreatedDate())
                        .updatedDate(memo.getUpdatedDate())
                        .build())
                .toList();
    }
    
    // 월별 메모 작성/수정
    public Long saveMemo(Long userId, SaveMemoRequest request) {
        // 메모 ID가 있으면 해당 메모 수정
        if (request.getMemoId() != null) {
            MonthlyMemo target = monthlyMemoDao.findByMemoId(request.getMemoId())
                    .orElseThrow(() -> new ResourceNotFoundException("메모를 찾을 수 없습니다."));
            
            // 본인 메모인지 확인
            if (!target.getUserId().equals(userId)) {
                throw new ForbiddenException("수정 권한이 없습니다.");
            }
            
            target.setMemoContent(request.getMemoContent());
            monthlyMemoDao.updateMemo(target);
            return target.getMemoId();
        }
        
        // 신규 메모 생성
        MonthlyMemo memo = MonthlyMemo.builder()
                .userId(userId)
                .year(request.getYear())
                .month(request.getMonth())
                .memoContent(request.getMemoContent())
                .build();
        monthlyMemoDao.insertMemo(memo);
        return memo.getMemoId();
    }
    
    // 월별 메모 삭제
    public void deleteMemo(Long userId, Long memoId) {
        MonthlyMemo memo = monthlyMemoDao.findByMemoId(memoId)
                .orElseThrow(() -> new ResourceNotFoundException("메모를 찾을 수 없습니다."));

        // 본인 메모인지 확인
        if (!memo.getUserId().equals(userId)) {
            throw new ForbiddenException("삭제 권한이 없습니다.");
        }
        
        monthlyMemoDao.deleteMemo(memoId);
    }
}
