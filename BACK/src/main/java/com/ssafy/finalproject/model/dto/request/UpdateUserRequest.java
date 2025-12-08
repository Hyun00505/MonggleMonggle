package com.ssafy.finalproject.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String calendarType; // 양력(solar) / 음력(lunar)
    private String password; // optional
}

