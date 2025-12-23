package com.ssafy.finalproject.model.dto.fastapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FastAPI 통합 운세 요청 DTO
 * 꿈 해몽 + 오늘의 운세 조회
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveFortuneRequestDto {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("dream_content")
    private String dreamContent;
    
    @JsonProperty("gender")
    private String gender;  // "M" 또는 "F"
    
    @JsonProperty("calendar_type")
    private String calendarType;  // "solar", "lunarGeneral", "lunarLeap"
    
    @JsonProperty("birth_date")
    private String birthDate;  // "YYYY-MM-DD" 형식
}

