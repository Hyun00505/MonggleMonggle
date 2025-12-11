package com.ssafy.finalproject.model.dao;

import com.ssafy.finalproject.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Optional;

@Mapper
public interface UserDao {
    
    // 회원가입
    void insertUser(User user);
    
    // 로그인 아이디로 조회
    Optional<User> findByLoginId(@Param("loginId") String loginId);
    
    // 사용자 ID로 조회
    Optional<User> findById(@Param("userId") Long userId);
    
    // 사용자 정보 수정
    void updateUser(User user);

    // 하루 코인 리셋 (조건부)
    int resetDailyCoin(@Param("userId") Long userId, @Param("today") LocalDate today);

    // 코인 차감 (조건부)
    int consumeCoin(@Param("userId") Long userId, @Param("cost") int cost);
    
    // 회원 탈퇴 (Soft Delete)
    void deleteUser(@Param("userId") Long userId);
    
    // 로그인 아이디 중복 체크
    int countByLoginId(@Param("loginId") String loginId);
}
