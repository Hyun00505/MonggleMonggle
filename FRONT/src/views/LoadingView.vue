<template>
  <div class="loading-container">
    <div class="animation-wrapper">
      <div class="cloud cloud-1">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M17,10c-0.6,0-1.1,0.1-1.6,0.3c-0.7-2.3-2.8-3.9-5.4-3.9c-3.1,0-5.6,2.3-6,5.3C1.8,12.2,0.3,14,0.3,16 c0,2.8,2.2,5,5,5h11.7c3.9,0,7-3.1,7-7C24,10.6,20.9,7.8,17,10z" />
        </svg>
      </div>
      <div class="star">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
        </svg>
      </div>
    </div>

    <!-- 로딩 바 추가 -->
    <div class="progress-container">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progress + '%' }"></div>
      </div>
      <span class="progress-text">{{ progress }}%</span>
    </div>

    <div class="text-container">
      <transition name="fade" mode="out-in">
        <p :key="currentMessageIndex" class="loading-message">
          {{ currentMessage }}
        </p>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute, onBeforeRouteLeave } from "vue-router";
import { storeToRefs } from "pinia";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { useUserStorage } from "../composables/useUserStorage";
import { useConfirm } from "../composables/useConfirm";
import dreamFactsData from "../data/dream_facts.json";

const router = useRouter();
const route = useRoute();
const dreamEntriesStore = useDreamEntriesStore();
const { analysisError, analysisDate, analysisLoading } = storeToRefs(dreamEntriesStore);
const { getSessionUser } = useUserStorage();
const { confirm } = useConfirm();

// 분석 진행 중 여부
const isAnalyzing = ref(false);

// 로딩 바 진행률
const progress = ref(0);

// 꿈에 관한 흥미로운 팩트들 (JSON에서 불러옴)
const dreamFacts = Object.values(dreamFactsData);

// 랜덤하게 섞인 메시지 배열
const shuffledFacts = ref([]);
const currentMessageIndex = ref(0);
let intervalId = null;
let progressIntervalId = null;

// 배열 섞기 함수
function shuffleArray(array) {
  const shuffled = [...array];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
}

// 현재 표시할 메시지
const currentMessage = computed(() => {
  if (shuffledFacts.value.length === 0) return dreamFacts[0];
  return shuffledFacts.value[currentMessageIndex.value % shuffledFacts.value.length];
});

// 진행률 증가 함수 (API 완료 전까지 90%까지만)
function startProgressSimulation() {
  let currentProgress = 0;
  progressIntervalId = setInterval(() => {
    if (currentProgress < 90) {
      // 처음에는 빠르게, 후반에는 천천히
      const increment = currentProgress < 30 ? 3 : currentProgress < 60 ? 2 : 1;
      currentProgress += increment;
      progress.value = Math.min(currentProgress, 90);
    }
  }, 500);
}

// 진행률 100%로 완료
function completeProgress() {
  if (progressIntervalId) clearInterval(progressIntervalId);
  progress.value = 100;
}

async function performAnalysis() {
  // 사용자 정보 가져오기
  const userInfo = getSessionUser() || {};
  const currentUser = JSON.parse(localStorage.getItem("currentUser") || "{}");

  const mergedUserInfo = {
    name: userInfo.name || currentUser.name || "사용자",
    gender: userInfo.gender || currentUser.gender || "M",
    calendarType: userInfo.calendarType || currentUser.calendarType || "solar",
    birthDate: userInfo.birthDate || currentUser.birthDate || "1990-01-01",
  };

  isAnalyzing.value = true;

  try {
    // 스토어에서 직접 함수 호출
    const success = await dreamEntriesStore.requestDreamAnalysis(mergedUserInfo);

    // 진행률 완료
    completeProgress();

    // 잠시 대기 후 페이지 이동 (100% 표시를 보여주기 위해)
    await new Promise((resolve) => setTimeout(resolve, 500));

    isAnalyzing.value = false;

    if (success) {
      // 분석 성공 시 날짜와 함께 결과 페이지로 이동
      router.push({
        name: "analysis",
        query: { date: analysisDate.value },
      });
    } else {
      // 분석 실패 시 에러 메시지와 함께 이전 페이지로
      // 취소된 경우에는 alert 표시하지 않음
      if (analysisError.value && !analysisError.value.includes("취소")) {
        alert(analysisError.value || "AI 분석에 실패했습니다. 다시 시도해주세요.");
      }
      router.push({
        name: "write",
        query: { date: route.query.date },
      });
    }
  } catch (err) {
    console.error("❌ 분석 오류:", err);
    completeProgress();
    isAnalyzing.value = false;
    // 취소된 요청은 alert 표시하지 않음
    if (err.name !== 'CanceledError' && err.name !== 'AbortError') {
      alert("AI 분석 중 오류가 발생했습니다: " + err.message);
    }
    router.push({
      name: "write",
      query: { date: route.query.date },
    });
  }
}

// 페이지 이탈 시 경고
onBeforeRouteLeave(async (to, from, next) => {
  if (isAnalyzing.value) {
    const confirmed = await confirm({
      title: '해몽 분석 중',
      message: '해몽 분석이 진행 중입니다.\n페이지를 떠나면 분석이 취소됩니다.',
      subMessage: '정말 나가시겠습니까?',
      type: 'warning',
      confirmText: '나가기',
      cancelText: '취소'
    });
    
    if (confirmed) {
      // 분석 취소
      dreamEntriesStore.cancelDreamAnalysis();
      isAnalyzing.value = false;
      next();
    } else {
      next(false);
    }
  } else {
    next();
  }
});

// 브라우저 새로고침/닫기 시 경고
function handleBeforeUnload(e) {
  if (isAnalyzing.value) {
    e.preventDefault();
    e.returnValue = '해몽 분석이 진행 중입니다. 정말 나가시겠습니까?';
    return e.returnValue;
  }
}

onMounted(() => {
  const targetDate = route.query.date?.toString();
  const requestedDate = sessionStorage.getItem("analysisRequestedDate");

  // 버튼을 통한 정상 접근이 아니면 즉시 차단
  if (!requestedDate || !targetDate || requestedDate !== targetDate) {
    alert("올바른 경로로 접근해주세요. 꿈 작성 화면에서 해몽을 다시 요청해주세요.");
    router.replace({
      name: "write",
      query: targetDate ? { date: targetDate } : {},
    });
    return;
  }

  // 한 번만 사용하도록 바로 제거
  sessionStorage.removeItem("analysisRequestedDate");

  // 꿈 팩트 배열 랜덤 섞기
  shuffledFacts.value = shuffleArray(dreamFacts);

  // 메시지 변경 인터벌 (4초마다)
  intervalId = setInterval(() => {
    currentMessageIndex.value = (currentMessageIndex.value + 1) % shuffledFacts.value.length;
  }, 4000);

  // 진행률 시뮬레이션 시작
  startProgressSimulation();

  // 브라우저 새로고침/닫기 경고 이벤트 등록
  window.addEventListener('beforeunload', handleBeforeUnload);

  // 실제 AI 분석 요청
  performAnalysis();
});

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId);
  if (progressIntervalId) clearInterval(progressIntervalId);
  
  // 브라우저 새로고침/닫기 경고 이벤트 해제
  window.removeEventListener('beforeunload', handleBeforeUnload);
  
  // 분석 중이면 취소
  if (isAnalyzing.value) {
    dreamEntriesStore.cancelDreamAnalysis();
  }
});
</script>

<style scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  color: white;
  text-align: center;
}

.animation-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin-bottom: 2rem;
}

.cloud {
  position: absolute;
  color: rgba(255, 255, 255, 0.8);
  width: 80px;
  height: 80px;
  bottom: 0;
  left: 0;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
  animation: floatCloud 3s ease-in-out infinite;
}

.star {
  position: absolute;
  color: #ffeb3b;
  width: 50px;
  height: 50px;
  top: 0;
  right: 0;
  filter: drop-shadow(0 0 15px rgba(255, 235, 59, 0.6));
  animation: floatStar 2s ease-in-out infinite alternate;
}

/* 로딩 바 스타일 */
.progress-container {
  width: 280px;
  margin-bottom: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.2);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #c77dff, #6fa7ff, #c77dff);
  background-size: 200% 100%;
  border-radius: 10px;
  transition: width 0.3s ease-out;
  animation: shimmer 2s linear infinite;
}

.progress-text {
  font-family: "Dongle", sans-serif;
  font-size: 2rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.loading-message {
  font-size: 2rem;
  font-weight: 600;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  min-height: 2em;
  font-family: "Dongle", sans-serif;
  padding: 0 1rem;
}

@keyframes floatCloud {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes floatStar {
  0% {
    transform: rotate(-10deg) scale(1);
  }
  100% {
    transform: rotate(10deg) scale(1.1);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
