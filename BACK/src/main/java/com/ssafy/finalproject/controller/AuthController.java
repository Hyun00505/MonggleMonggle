package com.ssafy.finalproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.finalproject.model.dto.request.LoginRequest;
import com.ssafy.finalproject.model.dto.request.SignupRequest;
import com.ssafy.finalproject.model.dto.request.UpdateUserRequest;
import com.ssafy.finalproject.model.dto.response.ApiResponse;
import com.ssafy.finalproject.model.dto.response.LoginResponse;
import com.ssafy.finalproject.model.dto.response.SignupResponse;
import com.ssafy.finalproject.model.dto.response.UserInfoResponse;
import com.ssafy.finalproject.service.AuthService;
import com.ssafy.finalproject.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "01. 인증 API", description = "회원가입, 로그인, 사용자 정보 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "01-1. 회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 아이디")
    })
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "01-2. 로그인", description = "사용자 로그인 및 JWT 토큰 발급")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "01-3. 로그아웃", description = "사용자 로그아웃 (클라이언트에서 토큰 제거)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("로그아웃 성공")
                .build());
    }
    
    @Operation(summary = "01-4. 사용자 정보 조회", description = "현재 로그인한 사용자 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        authService.resetDailyCoinIfNeeded(userId);
        UserInfoResponse response = authService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "01-5. 사용자 정보 수정", description = "사용자 정보를 수정합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UpdateUserRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        authService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("사용자 정보가 수정되었습니다.")
                .build());
    }
    
    @Operation(summary = "01-6. 회원 탈퇴", description = "회원 탈퇴 처리 (Soft Delete)")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse> deleteUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        authService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("회원 탈퇴가 완료되었습니다.")
                .build());
    }
}

