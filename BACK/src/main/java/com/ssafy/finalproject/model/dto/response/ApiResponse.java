package com.ssafy.finalproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    
    // 기존 코드 호환을 위한 생성자
    public ApiResponse(String message) {
        this.success = true;
        this.message = message;
    }
}

