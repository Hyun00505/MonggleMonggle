import axios from "axios";

// axios 인스턴스 생성
const api = axios.create({
  baseURL: "/api",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터 - JWT 토큰 자동 추가
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 - 에러 처리
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status } = error.response;

      // 401 Unauthorized - 토큰 만료 또는 인증 실패
      if (status === 401) {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("currentUser");
        window.location.href = "/auth";
      }

      // 403 Forbidden - 권한 없음
      if (status === 403) {
        console.error("접근 권한이 없습니다.");
      }
    }
    return Promise.reject(error);
  }
);

export default api;
