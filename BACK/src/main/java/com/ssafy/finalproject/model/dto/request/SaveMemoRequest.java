package com.ssafy.finalproject.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveMemoRequest {
    
    // 수정 시 사용 (신규 작성 시 null)
    private Long memoId;
    
    @NotNull(message = "연도는 필수입니다.")
    private Integer year;
    
    @NotNull(message = "월은 필수입니다.")
    private Integer month;
    
    @NotBlank(message = "메모 내용은 필수입니다.")
    private String memoContent;
}

