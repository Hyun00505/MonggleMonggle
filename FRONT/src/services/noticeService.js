import api from './api';

export const noticeService = {
  // ==================== 공지사항 ====================
  
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

  // ==================== 좋아요 ====================
  
  // 좋아요 토글 (좋아요/좋아요 취소)
  async toggleLike(noticeId) {
    const response = await api.post(`/notices/likes/${noticeId}`);
    return response.data;
  },

  // 좋아요 상태 조회 (좋아요 수 + 현재 사용자 좋아요 여부)
  async getLikeStatus(noticeId) {
    const response = await api.get(`/notices/likes/${noticeId}`);
    return response.data;
  },

  // ==================== 댓글 ====================
  
  // 댓글 작성
  async createComment(noticeId, content) {
    const response = await api.post('/notices/comments', { noticeId, content });
    return response.data;
  },

  // 공지사항별 댓글 목록 조회
  async getComments(noticeId) {
    const response = await api.get(`/notices/comments/${noticeId}`);
    return response.data;
  },

  // 댓글 수정
  async updateComment(commentId, content) {
    const response = await api.put(`/notices/comments/${commentId}`, { content });
    return response.data;
  },

  // 댓글 삭제
  async deleteComment(commentId) {
    const response = await api.delete(`/notices/comments/${commentId}`);
    return response.data;
  },
};

export default noticeService;

