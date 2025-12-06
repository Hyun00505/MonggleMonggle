import api from "./api";

/**
 * 이미지 업로드 관련 API 서비스
 * 백엔드 API: /api/images
 */
export const imageService = {
  /**
   * Base64 이미지를 서버에 업로드
   * @param {string} imageData - Base64 인코딩된 이미지 데이터 (data URI 형식 가능)
   * @param {number} dreamId - 꿈 ID (선택)
   * @returns {Promise<Object>} { success, message, imageUrl }
   */
  async uploadImage(imageData, dreamId = null) {
    const response = await api.post("/images/upload", {
      imageData,
      dreamId,
    });
    return response.data;
  },

  /**
   * 서버에 저장된 이미지 삭제
   * @param {string} imageUrl - 삭제할 이미지 URL
   */
  async deleteImage(imageUrl) {
    const response = await api.delete("/images", {
      params: { imageUrl },
    });
    return response.data;
  },
};

export default imageService;
