package com.ssafy.finalproject.model.dto.fastapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FastAPI 통합 운세 응답 DTO
 * 꿈 해몽 + 오늘의 운세 결과
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveFortuneResponseDto {
    
    @JsonProperty("dream_interpretation")
    private String dreamInterpretation;
    
    @JsonProperty("today_fortune_summary")
    private String todayFortuneSummary;
    
    @JsonProperty("lucky_color")
    private LuckyColor luckyColor;
    
    @JsonProperty("lucky_item")
    private LuckyItem luckyItem;
    
    /**
     * 행운의 색상 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LuckyColor {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("number")
        private Integer number;
        
        @JsonProperty("reason")
        private String reason;
    }
    
    /**
     * 행운의 아이템 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LuckyItem {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("reason")
        private String reason;
    }
}

