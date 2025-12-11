<template>
  <div class="loading-container">
    <div class="animation-wrapper">
      <div class="cloud cloud-1">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M17,10c-0.6,0-1.1,0.1-1.6,0.3c-0.7-2.3-2.8-3.9-5.4-3.9c-3.1,0-5.6,2.3-6,5.3C1.8,12.2,0.3,14,0.3,16 c0,2.8,2.2,5,5,5h11.7c3.9,0,7-3.1,7-7C24,10.6,20.9,7.8,17,10z" />
        </svg>
      </div>
      <div class="moon">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454z" />
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
import { useRouter, useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { useUserStorage } from "../composables/useUserStorage";

const router = useRouter();
const route = useRoute();
const dreamEntriesStore = useDreamEntriesStore();
const { analysisError, analysisDate } = storeToRefs(dreamEntriesStore);
const { getSessionUser } = useUserStorage();

// 로딩 바 진행률
const progress = ref(0);

// 꿈에 관한 흥미로운 팩트들 (50개)
const dreamFacts = [
  "사람은 평생 약 6년이라는 시간을 꿈꾸는 데 보냅니다. 🌙",
  "평균적으로 하룻밤에 4개에서 6개의 꿈을 꿉니다. 💭",
  "꿈의 95%는 잠에서 깬 후 10분 이내에 영구히 잊혀집니다. ⏰",
  "시각장애인도 꿈을 꾸며, 청각, 후각, 촉각, 미각이 매우 생생하게 나타납니다. 👁️",
  "선천적 시각장애인의 꿈에서 촉각을 느낄 확률은 70%입니다. ✋",
  "약 12%의 사람들은 흑백 꿈을 꿉니다. 🎬",
  "과거 흑백 TV 시청 세대는 컬러 TV 세대보다 흑백 꿈을 꿀 확률이 훨씬 높습니다. 📺",
  "남성의 꿈에는 남성 캐릭터가 여성보다 2배 더 많이 등장합니다. 👨",
  "여성의 꿈에는 남녀 캐릭터 비율이 거의 비슷하게 나타납니다. 👩",
  "남성의 꿈은 여성의 꿈보다 신체적 공격성이 더 많이 나타납니다. 💥",
  "꿈꾸는 도중에 이것이 꿈이라는 사실을 스스로 깨닫는 현상을 자각몽이라고 합니다. 🧠",
  "자각몽을 꿀 때는 평소 잠잘 때 쉬고 있던 뇌의 논리적인 영역이 활성화됩니다. 💡",
  "꿈을 꾸는 깊은 잠의 단계에서는 뇌가 근육을 마비시킵니다. 😴",
  "수면 중 근육 마비가 풀려 꿈속 행동을 실제로 하는 수면 행동 장애가 있습니다. 🛏️",
  "수면 행동 장애는 파킨슨병이나 치매의 강력한 전조 증상일 수 있습니다. ⚠️",
  "동물들도 사람처럼 꿈을 꿀 가능성이 매우 높습니다. 🐾",
  "고양이는 꿈속에서 쥐를 사냥하는 듯한 행동을 보였습니다. 🐱",
  "쥐는 꿈속에서 낮에 배웠던 미로 길 찾기를 뇌 속에서 다시 연습합니다. 🐭",
  "문어는 잠을 잘 때 사냥할 때처럼 몸 색깔을 바꾸며 꿈을 꾸는 듯합니다. 🐙",
  "링컨 대통령은 암살당하기 며칠 전 자신의 죽음을 암시하는 꿈을 꾸었습니다. 📜",
  "재봉틀의 바늘 구조는 꿈에서 창에 찔리는 경험을 통해 발명되었습니다. 🪡",
  "주기율표는 멘델레예프가 꿈에서 원소들의 배열을 본 후 완성되었습니다. 🔬",
  "비틀즈의 'Yesterday' 멜로디는 폴 매카트니의 꿈에서 탄생했습니다. 🎵",
  "살바도르 달리는 숟가락을 들고 선잠을 자며 창의적인 환각 상태를 포착했습니다. 🎨",
  "'가위눌림'은 정신은 깨어났으나 근육의 마비가 풀리지 않은 수면 마비 현상입니다. 😱",
  "가위눌림 중에는 '침입자'나 '귀신' 같은 헛것을 보는 경우가 많습니다. 👻",
  "'폭발하는 머리 증후군'은 잠들 때 머릿속에서 굉음이 들리는 것 같은 증상입니다. 💣",
  "몽유병은 유전적 영향이 강해서, 부모 모두 몽유병이면 자녀도 62% 확률입니다. 🚶",
  "코를 골면서도 꿈을 꿀 수 있습니다. 😪",
  "꿈은 짧은 기억을 오래가는 기억으로 저장하는 과정의 일부입니다. 🗃️",
  "꿈을 꾸는 동안 뇌는 불필요한 정보를 삭제하여 기억을 정리합니다. 🗂️",
  "잠들 때 몸이 갑자기 움찔하는 것은 '수면 놀람' 현상입니다. ⚡",
  "수면 놀람은 뇌가 떨어지는 것으로 착각하여 몸을 보호하려는 반응입니다. 🪂",
  "가장 흔한 꿈의 주제는 쫓기거나, 떨어지거나, 시험을 보는 것입니다. 🏃",
  "이빨 빠지는 꿈은 실제로 치아에 자극이 갈 때 자주 꿉니다. 🦷",
  "자각몽을 통해 악몽을 치료하거나 운동 기술을 연습할 수 있습니다. 🎯",
  "자는 사람에게 물을 뿌리면 꿈속에서 비가 오는 내용으로 바뀝니다. 🌧️",
  "금연 패치 같은 보조제는 생생한 꿈이나 악몽을 유발할 수 있습니다. 💊",
  "꿈속의 시간 흐름은 현실과 거의 비슷하거나 절반 정도 느리게 갑니다. ⏱️",
  "세계에서 가장 길게 이어진 꿈의 기록은 3시간 8분입니다. 🏆",
  "꿈 하나는 보통 5분에서 20분 정도 지속됩니다. ⌛",
  "꿈은 부정적인 감정을 진정시키고 조절하는 역할을 합니다. 💆",
  "고대 이집트인들은 '수면 사원'에서 꿈을 통해 질병을 치료하려고 했습니다. 🏛️",
  "5일에서 7일 전의 기억이 뒤늦게 꿈에 다시 나타나는 경향이 있습니다. 📅",
  "태아도 엄마 뱃속에서 꿈꾸는 뇌 상태로 대부분의 시간을 보냅니다. 👶",
  "미래를 맞추는 꿈은 우연히 맞아떨어진 것일 가능성이 높습니다. 🔮",
  "잠들기 전에 '나는 꿈을 꿀 것이다'라고 되뇌면 자각몽을 꿀 수 있습니다. 💬",
  "꿈 내용을 잊지 않으려면 잠에서 깨자마자 기록하는 것이 유일한 방법입니다. 📝",
  "술은 꿈꾸는 잠을 방해하지만, 술이 깰 때는 악몽을 꾸게 할 수 있습니다. 🍷",
  "우리는 모두 매일 밤 꿈을 꾸지만, 단지 기억하지 못할 뿐입니다. ✨",
];

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
  console.log("🚀 분석 시작...");

  // 사용자 정보 가져오기
  const userInfo = getSessionUser() || {};
  const currentUser = JSON.parse(localStorage.getItem("currentUser") || "{}");

  console.log("📋 사용자 정보:", { userInfo, currentUser });

  const mergedUserInfo = {
    name: userInfo.name || currentUser.name || "사용자",
    gender: userInfo.gender || currentUser.gender || "M",
    calendarType: userInfo.calendarType || currentUser.calendarType || "solar",
    birthDate: userInfo.birthDate || currentUser.birthDate || "1990-01-01",
  };

  console.log("📤 요청할 사용자 정보:", mergedUserInfo);

  try {
    // 스토어에서 직접 함수 호출
    const success = await dreamEntriesStore.requestDreamAnalysis(mergedUserInfo);

    console.log("📥 분석 결과:", success);

    // 진행률 완료
    completeProgress();

    // 잠시 대기 후 페이지 이동 (100% 표시를 보여주기 위해)
    await new Promise((resolve) => setTimeout(resolve, 500));

    if (success) {
      // 분석 성공 시 날짜와 함께 결과 페이지로 이동
      router.push({
        name: "analysis",
        query: { date: analysisDate.value },
      });
    } else {
      // 분석 실패 시 에러 메시지와 함께 이전 페이지로
      alert(analysisError.value || "AI 분석에 실패했습니다. 다시 시도해주세요.");
      router.push({
        name: "write",
        query: { date: route.query.date },
      });
    }
  } catch (err) {
    console.error("❌ 분석 오류:", err);
    completeProgress();
    alert("AI 분석 중 오류가 발생했습니다: " + err.message);
    router.push({
      name: "write",
      query: { date: route.query.date },
    });
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

  // 실제 AI 분석 요청
  performAnalysis();
});

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId);
  if (progressIntervalId) clearInterval(progressIntervalId);
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

.moon {
  position: absolute;
  color: #ffeb3b;
  width: 60px;
  height: 60px;
  top: 0;
  right: 0;
  filter: drop-shadow(0 0 15px rgba(255, 235, 59, 0.6));
  animation: floatMoon 4s ease-in-out infinite alternate;
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
  font-size: 0.9rem;
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
  font-size: 1.4rem;
  font-weight: 600;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  min-height: 2em;
  font-family: "Nunito", sans-serif;
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

@keyframes floatMoon {
  0% {
    transform: rotate(-5deg) translateY(0);
  }
  100% {
    transform: rotate(5deg) translateY(-15px);
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
