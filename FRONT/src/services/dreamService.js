import api from './api';

/**
 * 꿈 일기 관련 API 서비스
 */
export const dreamService = {
  /**
   * 꿈 일기 작성
   * @param {Object} dreamData - { title, content, dreamDate, emotionScore }
   */
  async createDream(dreamData) {
    const response = await api.post('/dreams', dreamData);
    return response.data;
  },

  /**
   * 월별 꿈 일기 목록 조회
   * @param {number} year - 연도
   * @param {number} month - 월
   */
  async getDreamsByMonth(year, month) {
    const response = await api.get('/dreams', {
      params: { year, month },
    });
    return response.data;
  },

  /**
   * 꿈 일기 상세 조회
   * @param {number} dreamId - 꿈 일기 ID
   */
  async getDream(dreamId) {
    const response = await api.get(`/dreams/${dreamId}`);
    return response.data;
  },

  /**
   * 꿈 일기 수정
   * @param {number} dreamId - 꿈 일기 ID
   * @param {Object} updateData - { title, content, emotionScore }
   */
  async updateDream(dreamId, updateData) {
    const response = await api.put(`/dreams/${dreamId}`, updateData);
    return response.data;
  },

  /**
   * 꿈 일기 삭제
   * @param {number} dreamId - 꿈 일기 ID
   */
  async deleteDream(dreamId) {
    const response = await api.delete(`/dreams/${dreamId}`);
    return response.data;
  },
};

export default dreamService;

