package com.ssafy.finalproject.model.dao;

import com.ssafy.finalproject.model.entity.MonthlyAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface MonthlyAnalysisDao {
    
    // 월별 분석 저장
    void insertMonthlyAnalysis(MonthlyAnalysis analysis);
    
    // 월별 분석 조회
    Optional<MonthlyAnalysis> findByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );
    
    // 분석 ID로 조회
    Optional<MonthlyAnalysis> findById(@Param("analysisId") Long analysisId);
    
    // 월별 분석 수정
    void updateMonthlyAnalysis(MonthlyAnalysis analysis);
}
