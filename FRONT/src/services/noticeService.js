import api from './api';

export const noticeService = {
  // 전체 공지사항 조회
  async getAllNotices() {
    const response = await api.get('/notices');
    return response.data;
  },

  // 상세 공지사항 조회
  async getNoticeById(noticeId) {
    const response = await api.get(`/notices/${noticeId}`);
    return response.data;
  },

  // 공지사항 등록 (관리자 전용)
  async createNotice({ title, content }) {
    const response = await api.post('/notices', { title, content });
    return response.data;
  },

  // 공지사항 수정 (관리자 전용)
  async updateNotice(noticeId, { title, content }) {
    const response = await api.put(`/notices/${noticeId}`, { title, content });
    return response.data;
  },

  // 공지사항 삭제 (관리자 전용)
  async deleteNotice(noticeId) {
    const response = await api.delete(`/notices/${noticeId}`);
    return response.data;
  },
};

export default noticeService;

