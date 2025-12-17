package com.ssafy.finalproject.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long userId;
    private String loginId;
    private String password;
    private String name;
    private LocalDate birthDate;
    private String gender; // 'M' or 'F'
    private String calendarType; // 'solar', 'lunarGeneral', 'lunarLeap'
    private String role; // 'ADMIN' or 'USER' (기본값: USER)
    private Integer coin;
    private LocalDateTime lastCoinResetAt;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime deletedDate;
}

