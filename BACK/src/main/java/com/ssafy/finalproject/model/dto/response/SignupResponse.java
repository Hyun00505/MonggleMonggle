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
public class SignupResponse {
    private Long userId;
    private String loginId;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String calendarType;
    private Integer coin;
    private String message;
}

