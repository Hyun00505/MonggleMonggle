import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { useGalleryStore } from "./galleryStore";
import { useMonthlyMemoStore } from "./monthlyMemoStore";
import { luckyColorPalette, getLuckyColorById } from "../constants/luckyColors";
import { dreamService } from "../services/dreamService";
import { fortuneService } from "../services/fortuneService";
import { dreamResultService } from "../services/dreamResultService";

const STORAGE_KEY = "dreamEntriesStore";
const LEGACY_KEY = "dreamStore";

function formatDateKey(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function getRandomLuckyColorId() {
  return Math.floor(Math.random() * luckyColorPalette.length);
}

// 색상 이름을 HEX 코드로 변환
function getColorHex(colorName) {
  const colorMap = {
    회색: "#9E9E9E",
    갈색: "#8D6E63",
    주황색: "#FF9800",
    노란색: "#FFEB3B",
    초록색: "#4CAF50",
    파란색: "#2196F3",
    보라색: "#9C27B0",
    분홍색: "#E91E63",
    빨간색: "#F44336",
    하늘색: "#03A9F4",
    청록색: "#00BCD4",
    금색: "#FFD700",
    은색: "#C0C0C0",
    검정색: "#424242",
    흰색: "#FFFFFF",
  };
  return colorMap[colorName] || "#CDB4DB"; // 기본값: 연보라색
}

export const useDreamEntriesStore = defineStore("dreamEntries", () => {
  const selectedDate = ref(null);
  const dreamTitle = ref("");
  const dreamContent = ref("");
  const selectedEmotion = ref(null);
  const posts = ref({});
  const showAnalysisOption = ref(false);
  const currentLuckyColorId = ref(getRandomLuckyColorId());
  const loading = ref(false);
  const error = ref(null);
  const currentDreamId = ref(null); // 서버에서 받은 꿈 ID

  // AI 분석 결과 상태
  const analysisResult = ref(null);
  const analysisLoading = ref(false);
  const analysisError = ref(null);
  const analysisDate = ref(null); // 분석 요청한 날짜
  const hasExistingResult = ref(false); // 기존 해몽 결과 존재 여부
  const reinterpretCount = ref(0); // 재해몽 횟수 (최대 2회)
  const MAX_REINTERPRET = 2; // 최대 재해몽 횟수

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
      // 기존 해몽 결과 여부 및 재해몽 횟수
      hasExistingResult.value = existingPost.hasResult ?? false;
      reinterpretCount.value = existingPost.reinterpretCount ?? 0;
      if (typeof existingPost.luckyColorId === "number") {
        currentLuckyColorId.value = existingPost.luckyColorId;
      }
    } else {
      resetWriteFields();
      showAnalysisOption.value = false;
      hasExistingResult.value = false;
      reinterpretCount.value = 0;
      currentLuckyColorId.value = getRandomLuckyColorId();
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
      console.log("해몽 결과 없음:", err.response?.status);
      return null;
    }
  }

  // 날짜 선택 시 해몽 결과도 함께 조회
  async function setSelectedDateWithResult(date) {
    setSelectedDate(date);

    if (!date) return;

    const dateKey = formatDateKey(date);
    const existingPost = posts.value[dateKey];

    if (existingPost?.dreamId) {
      const result = await fetchDreamResult(existingPost.dreamId);
      if (result) {
        hasExistingResult.value = true;
        reinterpretCount.value = result.reinterpretCount ?? 0;
        // 분석 결과도 저장
        analysisResult.value = {
          dreamInterpretation: result.dreamInterpretation,
          todayFortuneSummary: result.todayFortuneSummary,
          luckyColor: result.luckyColor,
          luckyItem: result.luckyItem,
          date: dateKey,
          dreamTitle: existingPost.title,
          dreamContent: existingPost.content,
          resultId: result.id,
        };
        // 로컬 상태 업데이트 - 행운의 색상도 함께 업데이트
        posts.value[dateKey] = {
          ...existingPost,
          hasResult: true,
          reinterpretCount: result.reinterpretCount ?? 0,
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

  // API 연동 꿈 저장
  async function saveDream() {
    if (!selectedDate.value) return false;

    const dateKey = formatDateKey(selectedDate.value);
    const luckyColor = getLuckyColorById(currentLuckyColorId.value);

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
          emotionId: selectedEmotion.value || 3,
        };

        let response;
        if (existingDreamId) {
          // 수정 (기존 dreamId가 있으면 업데이트)
          response = await dreamService.updateDream(existingDreamId, dreamData);
          currentDreamId.value = existingDreamId;
        } else {
          // 새로 생성
          response = await dreamService.createDream(dreamData);
          currentDreamId.value = response.dreamId;
        }
      }

      // 로컬 상태 업데이트 (해몽 전에는 흰색, 해몽 후 색상 업데이트됨)
      const existingPost = posts.value[dateKey];
      posts.value = {
        ...posts.value,
        [dateKey]: {
          dreamId: currentDreamId.value,
          title: dreamTitle.value,
          content: dreamContent.value,
          emotion: selectedEmotion.value,
          // 기존에 해몽 결과가 있으면 그 색상 유지, 없으면 흰색
          color: existingPost?.hasResult ? existingPost.color : "#FFFFFF",
          luckyColorId: existingPost?.hasResult ? existingPost.luckyColorId : null,
          luckyColorName: existingPost?.hasResult ? existingPost.luckyColorName : null,
          hasResult: existingPost?.hasResult ?? false,
          reinterpretCount: existingPost?.reinterpretCount ?? 0,
        },
      };

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
          posts.value[dateKey] = {
            dreamId: dream.dreamId,
            title: dream.title,
            content: dream.content,
            emotion: dream.emotionId,
            color: dream.luckyColor || getLuckyColorById(getRandomLuckyColorId()).hex,
            luckyColorId: dream.luckyColorId || getRandomLuckyColorId(),
            luckyColorName: dream.luckyColorName || "",
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

    console.log("📤 FastAPI 요청 시작...");

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

      console.log("📤 FastAPI 요청 데이터:", request);

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
            // 재해몽: 기존 결과 업데이트
            dbResult = await dreamResultService.updateDreamResult(dreamId, saveRequest);
            reinterpretCount.value += 1;
            console.log("✅ AI 분석 결과가 업데이트되었습니다 (재해몽 횟수:", reinterpretCount.value, ")");
          } else {
            // 최초 해몽: 새로 저장
            dbResult = await dreamResultService.saveDreamResult(dreamId, saveRequest);
            console.log("✅ AI 분석 결과가 DB에 저장되었습니다:", dbResult);
          }

          analysisResult.value.resultId = dbResult.resultId;
          hasExistingResult.value = true;

          // 로컬 상태 업데이트 - 행운의 색상도 함께 업데이트
          posts.value[dateKey] = {
            ...posts.value[dateKey],
            hasResult: true,
            reinterpretCount: reinterpretCount.value,
            // 해몽 결과의 행운의 색상으로 별 색상 업데이트
            color: getColorHex(result.lucky_color.name),
            luckyColorName: result.lucky_color.name,
            luckyColorNumber: result.lucky_color.number,
          };
          persistEntries();
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
    currentLuckyColorId.value = getRandomLuckyColorId();
  }

  function resetAll() {
    clearSelectedDate();
    posts.value = {};
    currentLuckyColorId.value = getRandomLuckyColorId();
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
  const currentLuckyColor = computed(() => getLuckyColorById(currentLuckyColorId.value));

  function setLuckyColorId(id) {
    if (typeof id === "number" && luckyColorPalette.some((color) => color.id === id)) {
      currentLuckyColorId.value = id;
    } else {
      currentLuckyColorId.value = getRandomLuckyColorId();
    }

    persistEntries();
  }

  function persistEntries() {
    if (typeof window === "undefined") return;
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        posts: posts.value,
        luckyColorId: currentLuckyColorId.value,
      })
    );
  }

  function hydrateFromLocalStorage() {
    if (typeof window === "undefined") return;

    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const data = JSON.parse(saved);
      posts.value = data.posts || {};
      currentLuckyColorId.value = typeof data.luckyColorId === "number" ? data.luckyColorId : 0;
      return;
    }

    const legacy = localStorage.getItem(LEGACY_KEY);
    if (legacy) {
      const data = JSON.parse(legacy);
      posts.value = data.posts || {};
    }
  }

  hydrateFromLocalStorage();

  // 재해몽 가능 여부 확인
  const canReinterpret = computed(() => reinterpretCount.value < MAX_REINTERPRET);
  const remainingReinterprets = computed(() => MAX_REINTERPRET - reinterpretCount.value);

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
    reinterpretCount,
    canReinterpret,
    remainingReinterprets,
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
    setLuckyColorId,
    getMonthlyStats,
    persistEntries,
    fetchDreamsByMonth,
    // AI 분석 함수
    requestDreamAnalysis,
    clearAnalysisResult,
  };
});
