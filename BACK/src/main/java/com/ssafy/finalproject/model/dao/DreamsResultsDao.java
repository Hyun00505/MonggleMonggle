package com.ssafy.finalproject.model.dao;

import com.ssafy.finalproject.model.entity.DreamResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface DreamsResultsDao {
    
    // AI 분석 결과 저장
    void insertDreamResult(DreamResult dreamResult);
    
    // 꿈 ID로 분석 결과 조회
    Optional<DreamResult> findByDreamId(@Param("dreamId") Long dreamId);
    
    // AI 분석 결과 수정
    void updateDreamResult(DreamResult dreamResult);
    
    // AI 분석 결과 삭제
    void deleteDreamResult(@Param("dreamId") Long dreamId);
    
    // 꿈 ID로 분석 결과 존재 여부 확인
    boolean existsByDreamId(@Param("dreamId") Long dreamId);
    
    // 찜 토글
    void toggleLike(@Param("dreamId") Long dreamId);
}
