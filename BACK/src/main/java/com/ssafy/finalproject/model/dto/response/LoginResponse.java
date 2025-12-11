package com.ssafy.finalproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String loginId;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String calendarType;
    private Integer coin;
    private String token;
    private String message;
}

