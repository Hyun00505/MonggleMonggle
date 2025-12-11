import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { authService } from "../services/authService";

export const useAuthStore = defineStore("auth", () => {
  // 상태
  const user = ref(null);
  const token = ref(null);
  const loading = ref(false);
  const error = ref(null);

  // Getters
  const isLoggedIn = computed(() => !!token.value);
  const currentUser = computed(() => user.value);

  // 초기화 - localStorage에서 토큰 복원
  function initialize() {
    const storedToken = localStorage.getItem("accessToken");
    const storedUser = localStorage.getItem("currentUser");

    if (storedToken) {
      token.value = storedToken;
    }
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser);
      } catch (e) {
        console.error("Failed to parse stored user:", e);
      }
    }
  }

  // 회원가입
  async function signup(userData) {
    loading.value = true;
    error.value = null;

    try {
      const requestData = {
        loginId: userData.loginId,
        password: userData.password,
        name: userData.name,
        birthDate: userData.birthDate,
        gender: userData.gender === "male" ? "M" : "F",
        calendarType: userData.calendarType,
      };
      console.log("API로 전송되는 데이터:", requestData);
      const response = await authService.signup(requestData);

      // 회원가입 후 자동 로그인
      await login({
        loginId: userData.loginId,
        password: userData.password,
      });

      return response;
    } catch (err) {
      error.value = err.response?.data?.message || "회원가입에 실패했습니다.";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 로그인
  async function login(credentials) {
    loading.value = true;
    error.value = null;

    try {
      const response = await authService.login(credentials);

      // 백엔드에서 'token'으로 반환
      token.value = response.token;
      // 응답 데이터 자체가 user 정보 포함
      user.value = {
        userId: response.userId,
        loginId: response.loginId,
        name: response.name,
        birthDate: response.birthDate,
        gender: response.gender,
        calendarType: response.calendarType,
        coin: response.coin, // ai 꿈해몽 할 수 있는 하루 횟수
      };

      return response;
    } catch (err) {
      error.value = err.response?.data?.message || "로그인에 실패했습니다.";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 로그아웃
  async function logout() {
    try {
      await authService.logout();
    } catch (err) {
      console.error("Logout error:", err);
    } finally {
      token.value = null;
      user.value = null;
    }
  }

  // 사용자 정보 조회
  async function fetchCurrentUser() {
    if (!token.value) return null;

    loading.value = true;
    try {
      const response = await authService.getCurrentUser();
      user.value = response;
      localStorage.setItem("currentUser", JSON.stringify(response));
      return response;
    } catch (err) {
      error.value = err.response?.data?.message || "사용자 정보 조회에 실패했습니다.";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 사용자 정보 수정
  async function updateUser(updateData) {
    loading.value = true;
    error.value = null;

    try {
      const response = await authService.updateUser(updateData);
      await fetchCurrentUser(); // 수정 후 최신 정보 다시 가져오기
      return response;
    } catch (err) {
      error.value = err.response?.data?.message || "정보 수정에 실패했습니다.";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 회원 탈퇴
  async function deleteAccount() {
    loading.value = true;
    error.value = null;

    try {
      await authService.deleteUser();
      token.value = null;
      user.value = null;
    } catch (err) {
      error.value = err.response?.data?.message || "회원 탈퇴에 실패했습니다.";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 에러 초기화
  function clearError() {
    error.value = null;
  }

  // 초기화 실행
  initialize();

  return {
    // State
    user,
    token,
    loading,
    error,

    // Getters
    isLoggedIn,
    currentUser,

    // Actions
    initialize,
    signup,
    login,
    logout,
    fetchCurrentUser,
    updateUser,
    deleteAccount,
    clearError,
  };
});
