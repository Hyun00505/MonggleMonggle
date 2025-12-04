import api from './api';

/**
 * AI 분석 결과 관련 API 서비스
 */
export const dreamResultService = {
  /**
   * AI 분석 결과 저장
   * @param {Object} resultData - AI 분석 결과 데이터
   */
  async saveDreamResult(resultData) {
    const response = await api.post('/dream-results', resultData);
    return response.data;
  },

  /**
   * 특정 꿈의 AI 분석 결과 조회
   * @param {number} dreamId - 꿈 일기 ID
   */
  async getDreamResult(dreamId) {
    const response = await api.get(`/dream-results/${dreamId}`);
    return response.data;
  },

  /**
   * AI 분석 결과 수정
   * @param {number} resultId - 분석 결과 ID
   * @param {Object} updateData - 수정할 데이터
   */
  async updateDreamResult(resultId, updateData) {
    const response = await api.put(`/dream-results/${resultId}`, updateData);
    return response.data;
  },

  /**
   * AI 분석 결과 삭제
   * @param {number} resultId - 분석 결과 ID
   */
  async deleteDreamResult(resultId) {
    const response = await api.delete(`/dream-results/${resultId}`);
    return response.data;
  },

  /**
   * 꿈 이미지 생성 (갤러리용)
   * @param {number} resultId - 분석 결과 ID
   * @param {Object} imageData - 이미지 생성 데이터
   */
  async generateDreamImage(resultId, imageData) {
    const response = await api.post(`/dream-results/${resultId}/images`, imageData);
    return response.data;
  },

  /**
   * 꿈 이미지 목록 조회
   * @param {number} resultId - 분석 결과 ID
   */
  async getDreamImages(resultId) {
    const response = await api.get(`/dream-results/${resultId}/images`);
    return response.data;
  },
};

export default dreamResultService;

