import axios from 'axios';

// FastAPI 전용 axios 인스턴스 (AI 서버)
const fortuneApi = axios.create({
  baseURL: '/ai-api',
  timeout: 120000, // 2분 (LLM 처리 시간 고려)
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * AI 운세 서비스 (FastAPI 연동)
 */
export const fortuneService = {
  /**
   * 통합 운세 조회 (꿈 해몽 + 오늘의 운세)
   * @param {Object} request - 요청 데이터
   * @param {string} request.name - 사용자 이름
   * @param {string} request.dream_content - 꿈 내용
   * @param {string} request.gender - 성별 ('M' 또는 'F')
   * @param {string} request.calendar_type - 달력 타입 ('solar', 'lunarGeneral', 'lunarLeap')
   * @param {string} request.birth_date - 생년월일 (YYYY-MM-DD)
   * @returns {Promise<Object>} 분석 결과
   */
  async getComprehensiveFortune(request) {
    const response = await fortuneApi.post('/api/v1/fortune/comprehensive', request);
    return response.data;
  },

  /**
   * 꿈 이미지 생성
   * @param {Object} request - 요청 데이터
   * @param {string} request.dream_prompt - 꿈 내용 프롬프트
   * @param {string} request.style - 이미지 스타일
   * @returns {Promise<Object>} 생성된 이미지
   */
  async generateDreamImage(request) {
    const response = await fortuneApi.post('/api/v1/dream/generate-image', request);
    return response.data;
  },

  /**
   * 월간 분석 조회
   * @param {Object} request - 요청 데이터
   * @returns {Promise<Object>} 월간 분석 결과
   */
  async getMonthlyAnalysis(request) {
    const response = await fortuneApi.post('/api/v1/fortune/monthly-analysis', request);
    return response.data;
  },
};

export default fortuneService;

