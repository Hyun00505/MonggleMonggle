import api from './api';

/**
 * AI 분석 결과 관련 API 서비스
 * 백엔드 API: /api/dreams/{dreamId}/result
 */
export const dreamResultService = {
  /**
   * AI 분석 결과 저장
   * @param {number} dreamId - 꿈 일기 ID
   * @param {Object} resultData - AI 분석 결과 데이터
   * @param {string} resultData.dreamInterpretation - 꿈 해몽
   * @param {string} resultData.todayFortuneSummary - 오늘의 운세 요약
   * @param {Object} resultData.luckyColor - 행운의 색 { name, number, reason }
   * @param {Object} resultData.luckyItem - 행운의 아이템 { name, reason }
   * @param {string} resultData.imageUrl - 이미지 URL (선택)
   */
  async saveDreamResult(dreamId, resultData) {
    const response = await api.post(`/dreams/${dreamId}/result`, resultData);
    return response.data;
  },

  /**
   * 특정 꿈의 AI 분석 결과 조회
   * @param {number} dreamId - 꿈 일기 ID
   */
  async getDreamResult(dreamId) {
    const response = await api.get(`/dreams/${dreamId}/result`);
    return response.data;
  },

  /**
   * AI 분석 결과 수정
   * @param {number} dreamId - 꿈 일기 ID
   * @param {Object} updateData - 수정할 데이터
   */
  async updateDreamResult(dreamId, updateData) {
    const response = await api.put(`/dreams/${dreamId}/result`, updateData);
    return response.data;
  },

  /**
   * AI 분석 결과 삭제
   * @param {number} dreamId - 꿈 일기 ID
   */
  async deleteDreamResult(dreamId) {
    const response = await api.delete(`/dreams/${dreamId}/result`);
    return response.data;
  },
};

export default dreamResultService;

