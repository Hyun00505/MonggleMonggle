package com.ssafy.finalproject.model.dao;

import com.ssafy.finalproject.model.entity.MonthlyMemo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MonthlyMemoDao {
    
    // 월별 메모 저장
    void insertMemo(MonthlyMemo memo);
    
    // 사용자 ID, 연도, 월로 메모 목록 조회
    List<MonthlyMemo> findByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );
    
    // 메모 ID로 조회
    Optional<MonthlyMemo> findByMemoId(@Param("memoId") Long memoId);
    
    // 월별 메모 수정
    void updateMemo(MonthlyMemo memo);
    
    // 월별 메모 삭제 (Soft Delete)
    void deleteMemo(@Param("memoId") Long memoId);
}
