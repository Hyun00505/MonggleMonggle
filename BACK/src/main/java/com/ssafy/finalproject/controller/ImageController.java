package com.ssafy.finalproject.controller;

import com.ssafy.finalproject.model.dto.response.ApiResponse;
import com.ssafy.finalproject.service.CoinService;
import com.ssafy.finalproject.service.ImageService;
import com.ssafy.finalproject.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "5. Image", description = "이미지 업로드 API")
public class ImageController {

    private final ImageService imageService;
    private final CoinService coinService;

    @Operation(summary = "이미지 업로드", description = "Base64 인코딩된 이미지를 서버에 저장하고 URL을 반환합니다.")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestBody Map<String, Object> request) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        String base64Image = (String) request.get("imageData");
        
        if (base64Image == null || base64Image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이미지 데이터가 필요합니다."
            ));
        }
        
        try {
            // 코인 차감 (꿈 시각화 1회 = 2코인)
            coinService.consumeForImageVisualization(userId);

            String imageUrl = imageService.saveBase64Image(base64Image, userId);
            
            log.info("이미지 업로드 성공 - userId: {}, url: {}", userId, imageUrl);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이미지가 성공적으로 업로드되었습니다.",
                    "imageUrl", imageUrl
            ));
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "이미지 업로드에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "이미지 삭제", description = "서버에 저장된 이미지를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteImage(@RequestParam String imageUrl) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        try {
            // URL에서 userId 검증 (본인 이미지만 삭제 가능)
            if (!imageUrl.contains("/dream/" + userId + "/")) {
                return ResponseEntity.status(403).body(
                        new ApiResponse(false, "본인의 이미지만 삭제할 수 있습니다.")
                );
            }
            
            imageService.deleteImage(imageUrl);
            
            return ResponseEntity.ok(new ApiResponse(true, "이미지가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("이미지 삭제 실패", e);
            return ResponseEntity.internalServerError().body(
                    new ApiResponse(false, "이미지 삭제에 실패했습니다.")
            );
        }
    }
}
