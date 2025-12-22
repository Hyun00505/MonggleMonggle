<template>
  <div class="analysis-wrapper" :class="{ 'with-visualization': imageGeneratorRef?.showVisualization || imageGeneratorRef?.isExpanding }">
    <!-- Analysis Card -->
    <div class="analysis-card">
      <div class="card-close">
        <button class="icon-btn" @click="handleClose" aria-label="뒤로가기">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
        </button>
        <h2 class="analysis-title">
          AI 해몽 & 운세
          <span class="title-badge">Today's Pick</span>
        </h2>
        <button @click="handleClose" class="icon-btn" aria-label="닫기">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
        </button>
      </div>
      <div class="analysis-content">
        <div class="section dream-section">
          <h3>
            <span class="title-cloud" aria-hidden="true"></span>
            꿈 해몽
          </h3>
          <p class="result-text">
            {{ analysisResult?.dreamInterpretation || "분석 결과를 불러오는 중..." }}
          </p>
        </div>

        <div class="divider"></div>

        <div class="section fortune-section">
          <h3>
            <span class="title-cloud" aria-hidden="true"></span>
            오늘의 운세
          </h3>
          <p v-if="analysisResult?.todayFortuneSummary" class="result-text fortune-summary">
            {{ analysisResult.todayFortuneSummary }}
          </p>
          <div class="fortune-grid">
            <div class="fortune-card color-card" :style="{ '--fortune-color': displayLuckyColor.hex }">
              <div class="fortune-card-header">
                <span class="fortune-label">행운의 색</span>
                <span class="fortune-pill">Lucky Color</span>
              </div>
              <div class="color-highlight">
                <div class="color-swatch" :style="{ background: displayLuckyColor.hex }"></div>
                <div class="color-text">
                  <strong class="fortune-value">{{ displayLuckyColor.name }}</strong>
                </div>
              </div>
              <p class="fortune-reason">{{ displayLuckyColor.reason || analysisResult?.luckyColor?.reason }}</p>
            </div>

            <div class="fortune-card item-card">
              <div class="fortune-card-header">
                <span class="fortune-label">행운의 아이템</span>
                <span class="fortune-pill soft">Lucky Item</span>
              </div>
              <div class="item-highlight">
                <svg width="48" height="48" viewBox="0 0 64 64" class="clover-icon">
                  <path
                    d="M32 34c4 4 10 4 14 0s4-10 0-14-10-4-14 0c-4-4-10-4-14 0s-4 10 0 14c-4 4-4 10 0 14s10 4 14 0c4 4 10 4 14 0s4-10 0-14"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="3"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path d="M32 34l-4 20" stroke="currentColor" stroke-width="3" stroke-linecap="round" />
                </svg>
                <strong class="fortune-value">{{ analysisResult?.luckyItem?.name || "행운의 아이템" }}</strong>
              </div>
              <p class="fortune-reason">{{ analysisResult?.luckyItem?.reason || "분석 결과를 불러오는 중..." }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 꿈 그리기 컴포넌트 -->
    <DreamImageGenerator ref="imageGeneratorRef" :analysis-result="analysisResult" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useRouter, useRoute, onBeforeRouteLeave } from "vue-router";
import { storeToRefs } from "pinia";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { getColorHex } from "../constants/luckyColors";
import { useConfirm } from "../composables/useConfirm";
import DreamImageGenerator from "../components/image/DreamImageGenerator.vue";

const router = useRouter();
const route = useRoute();
const dreamEntriesStore = useDreamEntriesStore();
const { currentLuckyColor, postedDates, analysisResult, analysisDate } = storeToRefs(dreamEntriesStore);
const { setSelectedDateWithResult, fetchDreamsByMonth } = dreamEntriesStore;
const { confirm } = useConfirm();

// 꿈 그리기 컴포넌트 ref
const imageGeneratorRef = ref(null);

// 분석 결과에서 행운의 색상 정보 가져오기
const displayLuckyColor = computed(() => {
  if (analysisResult.value?.luckyColor) {
    return {
      name: analysisResult.value.luckyColor.name,
      hex: getColorHex(analysisResult.value.luckyColor.name),
      reason: analysisResult.value.luckyColor.reason,
    };
  }
  return currentLuckyColor.value;
});

// URL에서 날짜 복원 및 새로고침 시 결과 복구
onMounted(async () => {
  const dateKey = route.query.date?.toString();

  // 브라우저 새로고침/닫기 경고 이벤트 등록
  window.addEventListener('beforeunload', handleBeforeUnload);

  // 기존 결과가 없다면 스토어나 서버에서 복구 시도
  if (!analysisResult.value && dateKey) {
    const parsed = new Date(dateKey);

    if (!Number.isNaN(parsed.getTime())) {
      // 월 데이터가 비어있다면 서버에서 해당 달 꿈 목록을 가져와서 dreamId 확보
      if (!postedDates.value[dateKey]) {
        await fetchDreamsByMonth(parsed.getFullYear(), parsed.getMonth() + 1);
      }

      await setSelectedDateWithResult(parsed);
    }
  }

  // 그래도 결과가 없으면 달력으로 이동
  if (!analysisResult.value && dateKey) {
    router.replace({ name: "calendar" });
  }
});

function handleClose() {
  // 캘린더 페이지로 이동
  router.push({ name: "calendar" });
}

// 페이지 이탈 시 이미지 생성 중이면 경고
onBeforeRouteLeave(async (to, from, next) => {
  if (imageGeneratorRef.value?.isGenerating) {
    const confirmed = await confirm({
      title: '이미지 생성 중',
      message: '이미지 생성이 진행 중입니다.\n페이지를 떠나면 생성이 취소됩니다.',
      subMessage: '정말 나가시겠습니까?',
      type: 'warning',
      confirmText: '나가기',
      cancelText: '계속 기다리기'
    });
    
    if (confirmed) {
      // 이미지 생성 취소
      imageGeneratorRef.value?.cancelImageGeneration?.();
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
  if (imageGeneratorRef.value?.isGenerating) {
    e.preventDefault();
    e.returnValue = '이미지 생성이 진행 중입니다. 정말 나가시겠습니까?';
    return e.returnValue;
  }
}

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);
});
</script>

<style scoped>
/* ===== Analysis Card ===== */
.analysis-card {
  background: white;
  border-radius: 32px;
  width: 100%;
  max-width: 680px;
  box-shadow: 0 16px 48px rgba(100, 100, 200, 0.12);
  animation: fadeSlideUp 0.5s ease;
}

.card-close {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.75rem;
}

.analysis-title {
  font-family: "Dongle", sans-serif;
  font-size: 2.2rem;
  font-weight: 700;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1.25rem;
  border-radius: 999px;
  background: var(--gradient-title-badge);
  color: #4c2b7b;
}

.title-badge {
  font-size: 0.7rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: 0.3rem 0.8rem;
  border-radius: 999px;
  background: white;
  color: #7447ff;
  font-weight: 600;
}

.analysis-content {
  padding: 0 2rem 2rem;
}

.section h3 {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-family: "Dongle", sans-serif;
  font-size: 1.6rem;
  font-weight: 600;
  color: #444;
  margin: 0 0 0.75rem;
}

.title-cloud {
  position: relative;
  display: inline-block;
  width: 22px;
  height: 9px;
  background: var(--color-purple);
  border-radius: 999px;
  transform: translateY(1px);
}

.title-cloud::before,
.title-cloud::after {
  content: "";
  position: absolute;
  background: var(--color-purple);
  border-radius: 999px;
}

.title-cloud::before {
  width: 12px;
  height: 12px;
  top: -6px;
  left: 2px;
}

.title-cloud::after {
  width: 14px;
  height: 14px;
  top: -4px;
  right: 0;
}

.result-text {
  font-size: 0.95rem;
  line-height: 1.7;
  color: #555;
  background: #f8f9fb;
  padding: 1rem 1.25rem;
  border-radius: 16px;
  margin: 0;
}

.fortune-summary {
  margin-bottom: 1.25rem;
}

.divider {
  border: none;
  border-top: 1px dashed #e5e5e5;
  margin: 1.25rem 0;
}

.fortune-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.fortune-card {
  background: #fff;
  border-radius: 20px;
  padding: 1.25rem;
  border: 1px solid #f0f0f0;
}

.fortune-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.fortune-label {
  font-family: "Dongle", sans-serif;
  font-size: 1.6rem;
  font-weight: 600;
  color: #666;
}

.fortune-pill {
  font-family: "Dongle", sans-serif;
  font-size: 0.95rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  background: #f0f0f0;
  color: #666;
}

.fortune-pill.soft {
  background: rgba(76, 175, 80, 0.1);
  color: #2e7d32;
}

.color-card {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.7)), var(--fortune-color, #f5f5f5);
}

.color-highlight,
.item-highlight {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.color-swatch {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.fortune-value {
  font-size: 1.2rem;
  color: #333;
}

.item-card {
  background: linear-gradient(145deg, #f0fdf4, #ecfdf5);
}

.clover-icon {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.1);
  padding: 6px;
  border-radius: 14px;
}

.fortune-reason {
  font-size: 0.95rem;
  line-height: 1.7;
  color: #666;
  margin: 0;
}

/* ===== Analysis Wrapper ===== */
.analysis-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  min-height: 80vh;
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== Responsive ===== */
@media (max-width: 1400px) {
  .analysis-wrapper {
    flex-direction: column;
    align-items: center;
    padding-bottom: 2rem;
  }
}

@media (max-width: 768px) {
  .analysis-card {
    border-radius: 24px;
  }

  .card-close {
    padding: 1rem 1.25rem;
  }

  .analysis-title {
    font-size: 1.6rem;
    padding: 0.4rem 1rem;
  }

  .title-badge {
    display: none;
  }

  .analysis-content {
    padding: 0 1.25rem 1.5rem;
  }

  .fortune-grid {
    grid-template-columns: 1fr;
    gap: 1.25rem;
  }

  .fortune-card {
    padding: 1rem;
  }
}
</style>
