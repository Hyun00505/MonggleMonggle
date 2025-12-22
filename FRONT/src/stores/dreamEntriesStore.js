import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { useGalleryStore } from "./galleryStore";
import { useMonthlyMemoStore } from "./monthlyMemoStore";
import { useAuthStore } from "./authStore";
import { getColorHex } from "../constants/luckyColors";
import { dreamService } from "../services/dreamService";
import { fortuneService } from "../services/fortuneService";
import { dreamResultService } from "../services/dreamResultService";
import { formatDateKey } from "../utils/dateUtils";

const STORAGE_KEY = "dreamEntriesStore";
const LEGACY_KEY = "dreamStore";

export const useDreamEntriesStore = defineStore("dreamEntries", () => {
  const authStore = useAuthStore();
  const galleryStore = useGalleryStore();
  const selectedDate = ref(null);
  const dreamTitle = ref("");
  const dreamContent = ref("");
  const selectedEmotion = ref(null);
  const posts = ref({});
  const showAnalysisOption = ref(false);
  const loading = ref(false);
  const error = ref(null);
  const currentDreamId = ref(null); // 서버에서 받은 꿈 ID

  // AI 분석 결과 상태
  const analysisResult = ref(null);
  const analysisLoading = ref(false);
  const analysisError = ref(null);
  const analysisDate = ref(null); // 분석 요청한 날짜
  const hasExistingResult = ref(false); // 기존 해몽 결과 존재 여부

  function setSelectedDate(date) {
    selectedDate.value = date;
    if (!date) {
      resetWriteFields();
      return;
    }

    const dateKey = formatDateKey(date);
    const existingPost = posts.value[dateKey];

    if (existingPost) {
      dreamTitle.value = existingPost.title;
      dreamContent.value = existingPost.content;
      selectedEmotion.value = existingPost.emotion ?? null;
      currentDreamId.value = existingPost.dreamId ?? null;
      showAnalysisOption.value = true;
      // 기존 해몽 결과 여부
      hasExistingResult.value = existingPost.hasResult ?? false;
    } else {
      resetWriteFields();
      showAnalysisOption.value = false;
      hasExistingResult.value = false;
    }
  }

  // DB에서 해몽 결과 조회
  async function fetchDreamResult(dreamId) {
    if (!dreamId) return null;

    try {
      const token = localStorage.getItem("accessToken");
      if (!token) return null;

      const result = await dreamResultService.getDreamResult(dreamId);
      return result;
    } catch (err) {
      // 결과가 없는 경우 404 에러
      return null;
    }
  }

  // 날짜 선택 시 해몽 결과도 함께 조회
  async function setSelectedDateWithResult(date) {
    setSelectedDate(date);

    if (!date) return;

    const dateKey = formatDateKey(date);
    const existingPost = posts.value[dateKey];

    // 해몽 결과가 없다고 표시된 경우에는 조회 요청을 보내지 않아 404를 피한다.
    // (달력 목록이나 해몽 완료 시 hasResult가 true로 동기화되므로 필요한 경우에만 호출)
    if (existingPost?.dreamId && existingPost?.hasResult) {
      const result = await fetchDreamResult(existingPost.dreamId);
      if (result) {
        hasExistingResult.value = true;
        // 분석 결과도 저장
        analysisResult.value = {
          dreamInterpretation: result.dreamInterpretation,
          todayFortuneSummary: result.todayFortuneSummary,
          luckyColor: result.luckyColor,
          luckyColorHex: getColorHex(result.luckyColor?.name),
          luckyItem: result.luckyItem,
          imageUrl: result.imageUrl,  // 기존 이미지 URL 추가
          date: dateKey,
          dreamTitle: existingPost.title,
          dreamContent: existingPost.content,
          resultId: result.id,
        };
        // 로컬 상태 업데이트 - 행운의 색상도 함께 업데이트
        posts.value[dateKey] = {
          ...existingPost,
          hasResult: true,
          // 해몽 결과의 행운의 색상으로 별 색상 업데이트
          color: result.luckyColor?.name ? getColorHex(result.luckyColor.name) : existingPost.color,
          luckyColorName: result.luckyColor?.name || existingPost.luckyColorName,
        };
        persistEntries();
      }
    }
  }

  function resetWriteFields() {
    dreamTitle.value = "";
    dreamContent.value = "";
    selectedEmotion.value = null;
    currentDreamId.value = null;
  }

  function validateRequiredFields() {
    const hasTitle = !!dreamTitle.value?.trim();
    const hasContent = !!dreamContent.value?.trim();
    const hasEmotion = selectedEmotion.value !== null && selectedEmotion.value !== undefined;

    if (!hasTitle || !hasContent || !hasEmotion) {
      return { valid: false, message: "제목, 내용, 감정을 모두 입력해주세요." };
    }

    return { valid: true };
  }

  // API 연동 꿈 저장
  async function saveDream() {
    if (!selectedDate.value) return false;

    const validation = validateRequiredFields();
    if (!validation.valid) {
      error.value = validation.message;
      return false;
    }

    const dateKey = formatDateKey(selectedDate.value);
    // 기존 게시물에서 dreamId 확인 (새로고침 후에도 유지되도록)
    const existingDreamId = currentDreamId.value || posts.value[dateKey]?.dreamId;

    loading.value = true;
    error.value = null;

    try {
      // 로그인 상태일 때만 API 호출
      const token = localStorage.getItem("accessToken");
      if (token) {
        const dreamData = {
          title: dreamTitle.value,
          content: dreamContent.value,
          dreamDate: dateKey,
          emotionId: selectedEmotion.value,
        };

        let response;
        if (existingDreamId) {
          // 수정 (기존 dreamId가 있으면 업데이트)
          response = await dreamService.updateDream(existingDreamId, dreamData);
          currentDreamId.value = existingDreamId;

          // 갤러리에서 해당 dreamId를 가진 항목 제거 (백엔드에서 이미지가 삭제되므로)
          const imageToRemove = galleryStore.galleryImages.find((img) => img.dreamId === existingDreamId);
          if (imageToRemove) {
            galleryStore.removeFromGallery(imageToRemove.id);
          }
        } else {
          // 새로 생성
          response = await dreamService.createDream(dreamData);
          currentDreamId.value = response.dreamId;
        }
      }

      // 로컬 상태 업데이트 (수정 시 기존 해몽 결과 초기화)
      posts.value = {
        ...posts.value,
        [dateKey]: {
          dreamId: currentDreamId.value,
          title: dreamTitle.value,
          content: dreamContent.value,
          emotion: selectedEmotion.value,
          color: "#FFFFFF", // 해몽 결과 초기화
          luckyColorName: null,
          hasResult: false,
        },
      };

      // 해몽 결과 상태 초기화
      hasExistingResult.value = false;
      analysisResult.value = null;

      showAnalysisOption.value = true;
      persistEntries();

      return true;
    } catch (err) {
      error.value = err.response?.data?.message || "꿈 일기 저장에 실패했습니다.";
      console.error("Dream save error:", err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  // API 연동 꿈 삭제
  async function deleteDream() {
    if (!selectedDate.value) return;
    const dateKey = formatDateKey(selectedDate.value);

    if (!posts.value[dateKey]) return;

    loading.value = true;
    error.value = null;

    try {
      // 로그인 상태이고 dreamId가 있으면 API 호출
      const token = localStorage.getItem("accessToken");
      const dreamId = posts.value[dateKey]?.dreamId;

      if (token && dreamId) {
        await dreamService.deleteDream(dreamId);
      }

      const updatedPosts = { ...posts.value };
      delete updatedPosts[dateKey];
      posts.value = updatedPosts;

      resetWriteFields();
      selectedEmotion.value = null;
      showAnalysisOption.value = false;
      persistEntries();
    } catch (err) {
      error.value = err.response?.data?.message || "꿈 일기 삭제에 실패했습니다.";
      console.error("Dream delete error:", err);
    } finally {
      loading.value = false;
    }
  }

  // 서버에서 월별 꿈 목록 가져오기
  async function fetchDreamsByMonth(year, month) {
    const token = localStorage.getItem("accessToken");
    if (!token) return;

    loading.value = true;
    error.value = null;

    try {
      const response = await dreamService.getDreamsByMonth(year, month);

      // 서버 데이터를 로컬 상태에 병합
      if (response.dreams && Array.isArray(response.dreams)) {
        response.dreams.forEach((dream) => {
          const dateKey = dream.dreamDate;

          // 해몽 결과가 있으면 서버에서 받은 색상 사용, 없으면 흰색
          const hasResult = dream.hasResult || false;
          let starColor = "#FFFFFF"; // 기본값: 흰색 (해몽 안함)

          if (hasResult && dream.luckyColorName) {
            // 서버에서 받은 색상 이름을 HEX로 변환
            starColor = getColorHex(dream.luckyColorName);
          }

          posts.value[dateKey] = {
            dreamId: dream.dreamId,
            title: dream.title,
            content: dream.content,
            emotion: dream.emotionId,
            // 해몽 결과 여부와 색상 정보
            hasResult: hasResult,
            color: starColor,
            luckyColorName: dream.luckyColorName || "",
            luckyColorNumber: dream.luckyColorNumber || null,
          };
        });
        persistEntries();
      }
    } catch (err) {
      error.value = err.response?.data?.message || "꿈 목록을 가져오는데 실패했습니다.";
      console.error("Fetch dreams error:", err);
    } finally {
      loading.value = false;
    }
  }

  // AI 꿈 해몽 요청
  async function requestDreamAnalysis(userInfo) {
    if (!selectedDate.value || !dreamContent.value) {
      analysisError.value = "꿈 내용이 필요합니다.";
      return false;
    }

    const dateKey = formatDateKey(selectedDate.value);
    const dreamId = currentDreamId.value || posts.value[dateKey]?.dreamId;

    // dreamId가 없어도 분석은 진행 (DB 저장만 건너뜀)
    if (!dreamId) {
      console.warn("⚠️ dreamId가 없습니다. 분석은 진행하되 DB 저장은 건너뜁니다.");
    }

    analysisLoading.value = true;
    analysisError.value = null;
    analysisDate.value = dateKey;

    // gender 변환 (male/female → M/F)
    const convertGender = (gender) => {
      if (!gender) return "M";
      const g = gender.toLowerCase();
      if (g === "male" || g === "m") return "M";
      if (g === "female" || g === "f") return "F";
      return "M";
    };

    try {
      const request = {
        name: userInfo.name || userInfo.userName || "사용자",
        dream_content: dreamContent.value,
        gender: convertGender(userInfo.gender),
        calendar_type: userInfo.calendarType || "solar",
        birth_date: userInfo.birthDate || "1990-01-01",
      };

      // 1. FastAPI에서 AI 분석 결과 받기
      const result = await fortuneService.getComprehensiveFortune(request);

      // 2. 분석 결과를 스토어에 저장
      analysisResult.value = {
        dreamInterpretation: result.dream_interpretation,
        todayFortuneSummary: result.today_fortune_summary,
        luckyColor: {
          name: result.lucky_color.name,
          number: result.lucky_color.number,
          reason: result.lucky_color.reason,
        },
        luckyColorHex: getColorHex(result.lucky_color.name),
        luckyItem: {
          name: result.lucky_item.name,
          reason: result.lucky_item.reason,
        },
        date: dateKey,
        dreamTitle: dreamTitle.value,
        dreamContent: dreamContent.value,
      };

      // 3. 분석 결과를 Spring Boot 백엔드 DB에 저장/업데이트
      const token = localStorage.getItem("accessToken");
      if (token && dreamId) {
        try {
          const saveRequest = {
            dreamInterpretation: result.dream_interpretation,
            todayFortuneSummary: result.today_fortune_summary,
            luckyColor: {
              name: result.lucky_color.name,
              number: result.lucky_color.number,
              reason: result.lucky_color.reason,
            },
            luckyItem: {
              name: result.lucky_item.name,
              reason: result.lucky_item.reason,
            },
            imageUrl: null,
          };

          let dbResult;
          if (hasExistingResult.value) {
            // 기존 결과 업데이트
            dbResult = await dreamResultService.updateDreamResult(dreamId, saveRequest);
          } else {
            // 최초 해몽: 새로 저장
            dbResult = await dreamResultService.saveDreamResult(dreamId, saveRequest);
          }

          analysisResult.value.resultId = dbResult.resultId;
          hasExistingResult.value = true;

          // 로컬 상태 업데이트 - 행운의 색상도 함께 업데이트
          posts.value[dateKey] = {
            ...posts.value[dateKey],
            hasResult: true,
            // 해몽 결과의 행운의 색상으로 별 색상 업데이트
            color: getColorHex(result.lucky_color.name),
            luckyColorName: result.lucky_color.name,
            luckyColorNumber: result.lucky_color.number,
          };
          persistEntries();

          // 코인 차감 후 최신 사용자 정보 동기화
          try {
            await authStore.fetchCurrentUser();
          } catch (syncErr) {
            console.warn("⚠️ 코인 동기화 실패:", syncErr?.message || syncErr);
          }
        } catch (dbErr) {
          // DB 저장 실패해도 분석 결과는 표시 (이미 분석 결과가 있는 경우 등)
          console.warn("⚠️ DB 저장 실패 (이미 존재할 수 있음):", dbErr.response?.data?.message || dbErr.message);
        }
      }

      return true;
    } catch (err) {
      analysisError.value = err.response?.data?.detail || err.message || "AI 분석에 실패했습니다.";
      console.error("Dream analysis error:", err);
      return false;
    } finally {
      analysisLoading.value = false;
    }
  }

  // 분석 결과 초기화
  function clearAnalysisResult() {
    analysisResult.value = null;
    analysisError.value = null;
    analysisDate.value = null;
  }

  function enableEditMode() {
    showAnalysisOption.value = false;
  }

  function resetWriteState() {
    showAnalysisOption.value = false;
  }

  function clearSelectedDate() {
    selectedDate.value = null;
    resetWriteFields();
    showAnalysisOption.value = false;
  }

  function resetAll() {
    clearSelectedDate();
    posts.value = {};
    persistEntries();

    // cascade reset to other domains
    const galleryStore = useGalleryStore();
    galleryStore.resetGallery();

    const memoStore = useMonthlyMemoStore();
    memoStore.resetMemos();
  }

  function setEmotion(emotion) {
    selectedEmotion.value = emotion;
  }

  function getMonthlyStats(year, month) {
    const yearMonth = `${year}-${String(month).padStart(2, "0")}`;
    const monthlyDreams = Object.entries(posts.value).filter(([dateKey]) => dateKey.startsWith(yearMonth));

    return {
      totalDreams: monthlyDreams.length,
      dreams: monthlyDreams.map(([dateKey, dream]) => ({
        date: dateKey,
        ...dream,
      })),
    };
  }

  const formattedSelectedDate = computed(() => {
    if (!selectedDate.value) return "";
    return selectedDate.value.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  });

  const postedDates = computed(() => posts.value);
  const currentLuckyColor = computed(() => ({
    name: "",
    hex: "#FFFFFF",
    reason: "",
  }));

  function persistEntries() {
    if (typeof window === "undefined") return;
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        posts: posts.value,
      })
    );
  }

  function hydrateFromLocalStorage() {
    if (typeof window === "undefined") return;

    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const data = JSON.parse(saved);
      posts.value = data.posts || {};
      return;
    }

    const legacy = localStorage.getItem(LEGACY_KEY);
    if (legacy) {
      const data = JSON.parse(legacy);
      posts.value = data.posts || {};
    }
  }

  hydrateFromLocalStorage();

  return {
    selectedDate,
    dreamTitle,
    dreamContent,
    selectedEmotion,
    posts,
    showAnalysisOption,
    formattedSelectedDate,
    postedDates,
    currentLuckyColor,
    loading,
    error,
    currentDreamId,
    // AI 분석 관련
    analysisResult,
    analysisLoading,
    analysisError,
    analysisDate,
    hasExistingResult,
    setSelectedDate,
    setSelectedDateWithResult,
    fetchDreamResult,
    saveDream,
    deleteDream,
    setEmotion,
    enableEditMode,
    resetWriteState,
    clearSelectedDate,
    resetAll,
    getMonthlyStats,
    persistEntries,
    fetchDreamsByMonth,
    validateRequiredFields,
    // AI 분석 함수
    requestDreamAnalysis,
    clearAnalysisResult,
  };
});
