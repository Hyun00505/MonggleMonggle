package com.ssafy.finalproject.model.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    
    @NotBlank(message = "로그인 아이디는 필수입니다.")
    @JsonProperty("loginId")
    private String loginId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    @JsonProperty("password")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다.")
    @JsonProperty("name")
    private String name;
    
    @NotNull(message = "생년월일은 필수입니다.")
    @JsonProperty("birthDate")
    private LocalDate birthDate;
    
    @NotBlank(message = "성별은 필수입니다.")
    @Pattern(regexp = "^[MF]$", message = "성별은 M 또는 F여야 합니다.")
    @JsonProperty("gender")
    private String gender;
    
    @NotBlank(message = "달력 유형은 필수입니다.")
    @Pattern(regexp = "^(solar|lunarGeneral|lunarLeap)$", message = "달력 유형은 solar, lunarGeneral, lunarLeap 중 하나여야 합니다.")
    @JsonProperty("calendarType")
    private String calendarType;
}

