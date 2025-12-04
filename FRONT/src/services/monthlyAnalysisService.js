import api from './api';

/**
 * 월간 분석 관련 API 서비스
 */
export const monthlyAnalysisService = {
  /**
   * 월간 AI 리포트 생성 요청
   * @param {number} year - 연도
   * @param {number} month - 월
   */
  async generateMonthlyAnalysis(year, month) {
    const response = await api.post('/monthly-analysis', { year, month });
    return response.data;
  },

  /**
   * 월간 분석 결과 조회
   * @param {number} year - 연도
   * @param {number} month - 월
   */
  async getMonthlyAnalysis(year, month) {
    const response = await api.get('/monthly-analysis', {
      params: { year, month },
    });
    return response.data;
  },

  /**
   * 월별 메모 저장
   * @param {Object} memoData - { year, month, memo }
   */
  async saveMemo(memoData) {
    const response = await api.post('/monthly-memo', memoData);
    return response.data;
  },

  /**
   * 월별 메모 조회
   * @param {number} year - 연도
   * @param {number} month - 월
   */
  async getMemo(year, month) {
    const response = await api.get('/monthly-memo', {
      params: { year, month },
    });
    return response.data;
  },

  /**
   * 월별 메모 삭제
   * @param {number} year - 연도
   * @param {number} month - 월
   */
  async deleteMemo(year, month) {
    const response = await api.delete('/monthly-memo', {
      params: { year, month },
    });
    return response.data;
  },

  /**
   * 감정 통계 조회
   * @param {number} year - 연도
   * @param {number} month - 월
   */
  async getEmotionStats(year, month) {
    const response = await api.get('/emotions/stats', {
      params: { year, month },
    });
    return response.data;
  },
};

export default monthlyAnalysisService;

