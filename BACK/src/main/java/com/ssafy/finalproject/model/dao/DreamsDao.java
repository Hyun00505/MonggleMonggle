package com.ssafy.finalproject.model.dao;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.finalproject.model.entity.Dream;

@Mapper
public interface DreamsDao {
    
    // 꿈 일기 작성
    void insertDream(Dream dream);
    
    // 꿈 일기 ID로 조회
    Optional<Dream> findById(@Param("dreamId") Long dreamId);
    
    // 월별 꿈 일기 목록 조회 (감정 이름 포함)
    List<Dream> findByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );
    
    // 꿈 일기 수정
    void updateDream(Dream dream);
    
    // 꿈 일기 삭제 (Soft Delete)
    void deleteDream(@Param("dreamId") Long dreamId);
    
    // 사용자의 월별 꿈 개수 조회
    int countByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );
    
    // 사용자의 특정 날짜 꿈 조회 (중복 방지용)
    Optional<Dream> findByUserIdAndDreamDate(
            @Param("userId") Long userId,
            @Param("dreamDate") java.time.LocalDate dreamDate
    );
}
