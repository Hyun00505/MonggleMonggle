package com.ssafy.finalproject.service;

import com.ssafy.finalproject.exception.ConflictException;
import com.ssafy.finalproject.exception.ResourceNotFoundException;
import com.ssafy.finalproject.exception.UnauthorizedException;
import com.ssafy.finalproject.model.dao.UserDao;
import com.ssafy.finalproject.model.dto.request.LoginRequest;
import com.ssafy.finalproject.model.dto.request.SignupRequest;
import com.ssafy.finalproject.model.dto.request.UpdateUserRequest;
import com.ssafy.finalproject.model.dto.response.LoginResponse;
import com.ssafy.finalproject.model.dto.response.SignupResponse;
import com.ssafy.finalproject.model.dto.response.UserInfoResponse;
import com.ssafy.finalproject.model.entity.User;
import com.ssafy.finalproject.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    // 회원가입
    public SignupResponse signup(SignupRequest request) {
        // 로그인 아이디 중복 체크
        if (userDao.countByLoginId(request.getLoginId()) > 0) {
            throw new ConflictException("이미 존재하는 아이디입니다.");
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // User 엔티티 생성
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender().toUpperCase())
                .calendarType(request.getCalendarType())
                .coin(5)
                .lastCoinResetAt(java.time.LocalDateTime.now(KST))
                .build();
        
        // 저장
        userDao.insertUser(user);
        
        // 응답 생성
        return SignupResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .calendarType(user.getCalendarType())
                .coin(user.getCoin())
                .message("회원가입이 완료되었습니다.")
                .build();
    }
    
    // 로그인
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userDao.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getUserId(), user.getLoginId());
        
        // 응답 생성
        return LoginResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .calendarType(user.getCalendarType())
                .coin(user.getCoin())
                .token(token)
                .message("로그인 성공")
                .build();
    }
    
    // 사용자 정보 조회
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 조회 시에도 날짜가 바뀌었으면 코인 리셋 (로그인 시 자동 리셋은 없음)
        int reset = userDao.resetDailyCoin(userId, java.time.LocalDate.now(KST));
        if (reset > 0) {
            user.setCoin(5);
            user.setLastCoinResetAt(LocalDateTime.now(KST));
        }
        
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .calendarType(user.getCalendarType())
                .coin(user.getCoin())
                .createdDate(user.getCreatedDate())
                .build();
    }
    
    // 사용자 정보 수정
    public void updateUser(Long userId, UpdateUserRequest request) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        
        user.setName(request.getName());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        
        // 달력 유형이 제공된 경우에만 변경
        if (request.getCalendarType() != null && !request.getCalendarType().isEmpty()) {
            user.setCalendarType(request.getCalendarType());
        }
        
        // 비밀번호가 제공된 경우에만 변경
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        userDao.updateUser(user);
    }
    
    // 회원 탈퇴
    public void deleteUser(Long userId) {
        userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        
        userDao.deleteUser(userId);
    }
}

