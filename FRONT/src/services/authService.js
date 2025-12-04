import api from "./api";

/**
 * 인증 관련 API 서비스
 */
export const authService = {
  /**
   * 회원가입
   * @param {Object} userData - { loginId, password, name, birthDate, gender }
   */
  async signup(userData) {
    const response = await api.post("/auth/signup", userData);
    return response.data;
  },

  /**
   * 로그인
   * @param {Object} credentials - { loginId, password }
   */
  async login(credentials) {
    const response = await api.post("/auth/login", credentials);

    // JWT 토큰 저장 (백엔드에서 'token'으로 반환)
    if (response.data.token) {
      localStorage.setItem("accessToken", response.data.token);
    }

    // 사용자 정보 저장 (응답 데이터 자체가 user 정보 포함)
    const userData = {
      userId: response.data.userId,
      name: response.data.name,
      birthDate: response.data.birthDate,
      gender: response.data.gender,
      calendarType: response.data.calendarType,
    };
    localStorage.setItem("currentUser", JSON.stringify(userData));

    return response.data;
  },

  /**
   * 로그아웃
   */
  async logout() {
    try {
      await api.post("/auth/logout");
    } finally {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("currentUser");
    }
  },

  /**
   * 현재 사용자 정보 조회
   */
  async getCurrentUser() {
    const response = await api.get("/auth/me");
    return response.data;
  },

  /**
   * 사용자 정보 수정
   * @param {Object} updateData - { name, birthDate, gender, password }
   */
  async updateUser(updateData) {
    const response = await api.put("/auth/me", updateData);
    return response.data;
  },

  /**
   * 회원 탈퇴
   */
  async deleteUser() {
    const response = await api.delete("/auth/me");
    localStorage.removeItem("accessToken");
    localStorage.removeItem("currentUser");
    return response.data;
  },

  /**
   * 로그인 상태 확인
   */
  isLoggedIn() {
    return !!localStorage.getItem("accessToken");
  },

  /**
   * 저장된 사용자 정보 가져오기
   */
  getStoredUser() {
    const userStr = localStorage.getItem("currentUser");
    return userStr ? JSON.parse(userStr) : null;
  },
};

export default authService;
