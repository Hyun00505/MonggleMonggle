<template>
  <div class="analysis-wrapper" :class="{ 'with-visualization': showVisualization || isExpanding }">
    <!-- Analysis Card -->
    <div class="analysis-card">
      <div class="card-close">
        <button class="fortune-back-btn" @click="handleClose" aria-label="뒤로가기">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
        </button>
        <h2 class="analysis-title">
          AI 해몽 & 운세
          <span class="title-badge">Today's Pick</span>
        </h2>
        <button @click="handleClose" class="close-btn" aria-label="닫기">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
        </button>
      </div>
      <div class="analysis-content">
        <div class="section dream-section">
          <h3>🌌 꿈 해몽</h3>
          <p class="result-text">
            {{ analysisResult?.dreamInterpretation || '분석 결과를 불러오는 중...' }}
          </p>
        </div>

        <div class="divider"></div>

        <div class="section fortune-section">
          <h3>🍀 오늘의 운세</h3>
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
                <strong class="fortune-value">{{ analysisResult?.luckyItem?.name || '행운의 아이템' }}</strong>
              </div>
              <p class="fortune-reason">{{ analysisResult?.luckyItem?.reason || '분석 결과를 불러오는 중...' }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bubble Button -->
    <div
      ref="bubbleRef"
      class="bubble-container"
      :class="{
        expanding: isExpanding && !isCollapsing,
        expanded: showVisualization && !isCollapsing,
        collapsing: isCollapsing,
      }"
      @click="!isExpanding && !showVisualization && !isCollapsing && handleGenerateImage()"
    >
      <div class="bubble-btn" v-if="!isExpanding && !showVisualization && !isCollapsing">
        <span class="bubble-text">꿈 그리기</span>
        <span class="bubble-emoji">🎨</span>
      </div>
      <div class="bubble-shine" v-if="!isExpanding && !showVisualization && !isCollapsing"></div>

      <!-- Visualization Card -->
      <div v-if="showVisualization" class="vis-card" ref="visCardRef">
        <div class="vis-card-header">
          <button @click="handleBackToAnalysis" class="vis-back-btn">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M19 12H5M12 19l-7-7 7-7" />
            </svg>
          </button>
          <h2 class="vis-title">🎨 꿈 그리기</h2>
        </div>

        <div class="vis-body">
          <!-- 꿈 정보 -->
          <div class="vis-preview" v-if="selectedDream">
            <span class="preview-tag">꿈 내용</span>
            <p class="preview-text">{{ selectedDream.title }}</p>
          </div>

          <!-- 스타일 선택 -->
          <div class="vis-section">
            <label class="vis-label">스타일 선택</label>
            <div class="style-chips">
              <button v-for="style in imageStyles" :key="style.id" :class="['style-chip', { active: selectedStyle === style.id }]" @click="selectedStyle = style.id">
                <span>{{ style.emoji }}</span>
                <span>{{ style.name }}</span>
              </button>
            </div>
          </div>

          <!-- 생성 버튼 -->
          <button class="vis-generate-btn" :disabled="!selectedDream || isGenerating" @click="generateImage">
            <span v-if="!isGenerating">✨ 이미지 생성하기</span>
            <span v-else class="generating">
              <span class="dot-pulse"></span>
              생성 중...
            </span>
          </button>

          <!-- 생성된 이미지 -->
          <div v-if="generatedImages.length > 0" class="vis-results">
            <div v-for="(img, index) in generatedImages" :key="index" class="result-card">
              <div class="result-image" :style="{ background: img.gradient }">
                <span class="result-emoji">{{ img.emoji }}</span>
              </div>
              <div class="result-info">
                <p class="result-caption">{{ img.caption }}</p>
                <div class="result-actions">
                  <button @click="saveToGallery(img)" class="result-action-btn">💾 저장</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { useGalleryStore } from "../stores/galleryStore";

const router = useRouter();
const route = useRoute();
const dreamEntriesStore = useDreamEntriesStore();
const galleryStore = useGalleryStore();
const { currentLuckyColor, postedDates, analysisResult, analysisDate } = storeToRefs(dreamEntriesStore);

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

// 색상 이름을 HEX 코드로 변환
function getColorHex(colorName) {
  const colorMap = {
    '빨간색': '#FF4444',
    '주황색': '#FF8C00',
    '노란색': '#FFD700',
    '초록색': '#32CD32',
    '파란색': '#4169E1',
    '남색': '#191970',
    '보라색': '#9370DB',
    '분홍색': '#FFB6C1',
    '하늘색': '#87CEEB',
    '청록색': '#40E0D0',
    '갈색': '#8B4513',
    '회색': '#808080',
    '검정색': '#333333',
    '흰색': '#FFFFFF',
    '금색': '#FFD700',
    '은색': '#C0C0C0',
  };
  return colorMap[colorName] || '#CDB4DB';
}

// URL에서 날짜 복원
onMounted(() => {
  if (!analysisResult.value && route.query.date) {
    // 분석 결과가 없으면 다시 write 페이지로
    router.replace({ name: 'write', query: { date: route.query.date } });
  }
});

// Bubble expansion state
const isExpanding = ref(false);
const isCollapsing = ref(false);
const showVisualization = ref(false);
const bubbleRef = ref(null);
const visCardRef = ref(null);

// Visualization state
const selectedDreamKey = ref("");
const selectedDream = ref(null);
const selectedStyle = ref("dreamy");
const isGenerating = ref(false);
const generatedImages = ref([]);

const imageStyles = [
  { id: "dreamy", name: "몽환적", emoji: "🌙" },
  { id: "watercolor", name: "수채화", emoji: "🎨" },
  { id: "anime", name: "애니", emoji: "✨" },
  { id: "fantasy", name: "판타지", emoji: "🦄" },
];

const gradients = [
  "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
  "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)",
  "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)",
  "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)",
  "linear-gradient(135deg, #fa709a 0%, #fee140 100%)",
];

const emojis = ["🌟", "✨", "💫", "🌙", "⭐", "🦋", "🌸"];

function handleClose() {
  // 날짜 정보와 함께 write 페이지로 돌아가기
  const date = route.query.date || analysisDate.value;
  if (date) {
    router.push({ name: "write", query: { date } });
  } else {
    router.push({ name: "calendar" });
  }
}

function handleGenerateImage() {
  // 자동으로 최신 꿈 선택 (또는 현재 페이지에 해당하는 꿈)
  const dates = Object.keys(postedDates.value).sort();
  if (dates.length > 0) {
    const latestDate = dates[dates.length - 1];
    selectedDreamKey.value = latestDate;
    selectedDream.value = postedDates.value[latestDate];
  }

  isExpanding.value = true;
  isCollapsing.value = false;

  setTimeout(() => {
    showVisualization.value = true;
    // 모바일에서 자동 스크롤
    nextTick(() => {
      if (bubbleRef.value && window.innerWidth <= 1400) {
        bubbleRef.value.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    });
  }, 500);
}

function handleBackToAnalysis() {
  // 1단계: 축소 애니메이션 시작 (내용과 동시에 축소)
  isCollapsing.value = true;

  // 2단계: 약간의 딜레이 후 내용 숨기기 (fade out 효과를 위해)
  setTimeout(() => {
    showVisualization.value = false;
  }, 150);

  // 3단계: 축소 애니메이션 완료 후 상태 리셋
  setTimeout(() => {
    isExpanding.value = false;
    isCollapsing.value = false;
  }, 500);
}

async function generateImage() {
  if (!selectedDream.value) return;

  isGenerating.value = true;
  await new Promise((resolve) => setTimeout(resolve, 2000));

  const styleInfo = imageStyles.find((s) => s.id === selectedStyle.value);
  const randomGradient = gradients[Math.floor(Math.random() * gradients.length)];
  const randomEmoji = emojis[Math.floor(Math.random() * emojis.length)];

  generatedImages.value.unshift({
    id: Date.now(),
    dreamKey: selectedDreamKey.value,
    style: styleInfo.name,
    gradient: randomGradient,
    emoji: randomEmoji,
    caption: `${selectedDream.value.title} - ${styleInfo.name}`,
    createdAt: new Date().toISOString(),
  });

  isGenerating.value = false;
}

function saveToGallery(image) {
  galleryStore.addToGallery(image);
  alert("갤러리에 저장되었습니다!");
}
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

.fortune-back-btn,
.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.2s;
}

.fortune-back-btn:hover,
.close-btn:hover {
  background: #f5f5f5;
  color: #333;
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
  background: linear-gradient(135deg, rgba(205, 180, 219, 0.3), rgba(162, 210, 255, 0.3));
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
  font-size: 1.1rem;
  font-weight: 700;
  color: #444;
  margin: 0 0 0.75rem;
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
  margin-bottom: 1rem;
  background: linear-gradient(135deg, #f0fdf4, #ecfdf5);
  border-left: 3px solid #22c55e;
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
  font-size: 0.85rem;
  font-weight: 600;
  color: #666;
}

.fortune-pill {
  font-size: 0.65rem;
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
  font-size: 0.8rem;
  color: #666;
  margin: 0;
  line-height: 1.5;
}

/* ===== Analysis Wrapper ===== */
.analysis-wrapper {
  display: flex;
  align-items: center; /* 수직 중앙 정렬 */
  justify-content: center;
  gap: 2rem;
  padding: 1rem;
  min-height: 80vh; /* 화면 중앙에 오도록 최소 높이 설정 */
}

/* ===== Bubble Container ===== */
.bubble-container {
  position: absolute;
  bottom: 40%;
  right: -180px;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background: linear-gradient(135deg, #c77dff, #6fa7ff);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 20px 40px rgba(99, 70, 171, 0.3), inset 0 0 25px rgba(255, 255, 255, 0.5);
  animation: floaty 3s ease-in-out infinite;
  z-index: 100;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.5s ease;
  opacity: 1;
}

.bubble-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
  font-size: 1.1rem;
  font-weight: 700;
  text-align: center;
  gap: 0.25rem;
}

.bubble-emoji {
  font-size: 1.4rem;
}

.bubble-shine {
  position: absolute;
  top: 18px;
  left: 22px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  filter: blur(3px);
}

.bubble-container:hover:not(.expanding):not(.expanded):not(.collapsing) {
  transform: scale(1.08);
  box-shadow: 0 30px 50px rgba(99, 70, 171, 0.45);
}

/* Expanding */
.bubble-container.expanding {
  position: relative;
  bottom: auto;
  right: auto;
  width: 420px;
  height: auto;
  min-height: 400px;
  border-radius: 28px;
  background: white;
  cursor: default;
  animation: expandIn 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  box-shadow: 0 20px 50px rgba(100, 100, 200, 0.15);
}

/* Expanded */
.bubble-container.expanded {
  position: relative;
  bottom: auto;
  right: auto;
  width: 420px;
  height: auto;
  min-height: 400px;
  border-radius: 28px;
  background: white;
  cursor: default;
  animation: none;
  box-shadow: 0 20px 50px rgba(100, 100, 200, 0.15);
  /* collapsing 전환을 위한 transition */
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1), height 0.5s cubic-bezier(0.4, 0, 0.2, 1), min-height 0.5s cubic-bezier(0.4, 0, 0.2, 1), border-radius 0.4s cubic-bezier(0.4, 0, 0.2, 1),
    background 0.3s ease, box-shadow 0.4s ease;
}

/* Collapsing - 카드에서 비눗방울로 morphing */
.bubble-container.collapsing {
  position: relative; /* 확장 상태와 동일하게 유지 */
  bottom: auto;
  right: auto;

  /* 애니메이션 */
  animation: collapseWidthHeight 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;

  /* 스타일 */
  background: linear-gradient(135deg, #c77dff, #6fa7ff);
  box-shadow: 0 20px 40px rgba(99, 70, 171, 0.3), inset 0 0 25px rgba(255, 255, 255, 0.5);
  cursor: default;
  overflow: hidden; /* 내부 콘텐츠 잘리도록 */
  z-index: 100;
  transform-origin: center center;
  pointer-events: none; /* 축소 중 클릭 방지 */
}

/* 축소 애니메이션 (Width & Height) */
@keyframes collapseWidthHeight {
  0% {
    width: 420px;
    height: auto;
    min-height: 400px; /* 확장된 높이 */
    border-radius: 28px;
    opacity: 1;
    transform: scale(1);
  }
  60% {
    width: 110px;
    min-height: 110px;
    height: 110px;
    border-radius: 45%;
    opacity: 1;
    transform: scale(1);
  }
  90% {
    width: 130px;
    height: 130px;
    min-height: 130px;
    border-radius: 50%;
    opacity: 0; /* 거의 다 줄어들었을 때 사라짐 */
    transform: scale(0.8);
  }
  100% {
    width: 130px;
    height: 130px;
    min-height: 130px;
    border-radius: 50%;
    opacity: 0; /* 완전히 사라짐 */
    transform: scale(0.5);
  }
}

@keyframes expandIn {
  0% {
    opacity: 0.8;
    transform: scale(0.2);
    border-radius: 50%;
  }
  60% {
    opacity: 1;
    transform: scale(1.02);
  }
  100% {
    opacity: 1;
    transform: scale(1);
    border-radius: 28px;
  }
}

@keyframes floaty {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
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

/* ===== Visualization Card ===== */
.vis-card {
  width: 100%;
  padding: 1.5rem;
  animation: fadeIn 0.3s ease;
}

/* collapsing 상태일 때 vis-card 페이드 아웃 */
.bubble-container.collapsing .vis-card {
  animation: fadeOutScale 0.15s ease forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeOutScale {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.9);
  }
}

.vis-card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f0f0;
}

.vis-back-btn {
  background: #f5f5f5;
  border: none;
  cursor: pointer;
  color: #666;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vis-back-btn:hover {
  background: #eee;
  color: #333;
}

.vis-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.vis-body {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.vis-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.vis-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #555;
}

.vis-select {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 2px solid #e8e8e8;
  border-radius: 14px;
  font-size: 0.95rem;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}

.vis-select:focus {
  outline: none;
  border-color: #a855f7;
  background: white;
}

.vis-preview {
  background: linear-gradient(135deg, #faf5ff, #f3e8ff);
  padding: 1rem;
  border-radius: 14px;
  border-left: 3px solid #a855f7;
}

.preview-tag {
  font-size: 0.7rem;
  font-weight: 600;
  color: #a855f7;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.preview-text {
  font-size: 0.95rem;
  color: #333;
  margin: 0.25rem 0 0;
  font-weight: 500;
}

.style-chips {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.5rem;
}

.style-chip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.7rem 1rem;
  border: 2px solid #e8e8e8;
  border-radius: 14px;
  background: white;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 500;
  color: #666;
  transition: all 0.2s;
}

.style-chip:hover {
  border-color: #c9b0ff;
  background: #f8f4ff;
}

.style-chip.active {
  border-color: #c77dff;
  background: linear-gradient(135deg, #f3e8ff, #e8f4ff);
  color: #6c4ab6;
}

.vis-generate-btn {
  width: 100%;
  padding: 1rem;
  border: none;
  border-radius: 14px;
  font-size: 1rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #c77dff, #6fa7ff);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 8px 20px rgba(99, 70, 171, 0.3);
}

.vis-generate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(99, 70, 171, 0.4);
}

.vis-generate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.generating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.dot-pulse {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.4;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.vis-results {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 0.5rem;
}

.result-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #fafafa;
  border-radius: 16px;
  border: 1px solid #f0f0f0;
}

.result-image {
  width: 80px;
  height: 80px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.result-emoji {
  font-size: 2.5rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.5rem;
}

.result-caption {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.result-actions {
  display: flex;
  gap: 0.5rem;
}

.result-action-btn {
  padding: 0.4rem 0.75rem;
  border: none;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 500;
  background: #e8e8e8;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}

.result-action-btn:hover {
  background: #ddd;
}

/* ===== Responsive ===== */
@media (max-width: 1400px) {
  .analysis-wrapper {
    flex-direction: column;
    align-items: center;
    padding-bottom: 2rem;
  }

  .bubble-container {
    position: static;
    margin: 1.5rem auto 0;
    width: 110px;
    height: 110px;
  }

  .bubble-container.expanding,
  .bubble-container.expanded {
    width: 100%;
    max-width: 500px;
    margin: 1.5rem auto;
  }

  .bubble-container.collapsing {
    /* expanded와 동일한 레이아웃 유지 */
    width: 100%;
    max-width: 500px;
    margin: 1.5rem auto;
    position: relative; /* 중요: static/absolute로 돌아가지 않도록 */

    animation: collapseWidthHeightMd 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  }

  @keyframes collapseWidthHeightMd {
    0% {
      width: 100%;
      max-width: 500px;
      height: auto;
      min-height: 400px;
      border-radius: 28px;
    }
    60% {
      width: 90px;
      height: 110px;
      min-height: 110px;
      border-radius: 45%;
    }
    100% {
      width: 110px;
      height: 110px;
      min-height: 110px;
      border-radius: 50%;
      /* 애니메이션 끝난 후 margin 조정은 불가능하므로, 
         JS에서 isExpanding = false 될 때 margin: 1.5rem auto 0 으로 바뀜 */
    }
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
    gap: 0.75rem;
  }

  .fortune-card {
    padding: 1rem;
  }

  .bubble-container {
    width: 100px;
    height: 100px;
  }

  .bubble-btn {
    font-size: 0.95rem;
  }

  .bubble-emoji {
    font-size: 1.2rem;
  }

  .bubble-container.expanding,
  .bubble-container.expanded {
    width: 100%;
    max-width: none;
    margin: 1rem 0;
    border-radius: 24px;
  }

  .bubble-container.collapsing {
    /* expanded와 동일한 레이아웃 유지 */
    width: 100%;
    max-width: none;
    margin: 1rem 0;
    position: relative;

    animation: collapseWidthHeightSm 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  }

  @keyframes collapseWidthHeightSm {
    0% {
      width: 100%;
      height: auto;
      min-height: 400px;
      border-radius: 24px;
    }
    60% {
      width: 80px;
      height: 100px;
      min-height: 100px;
      border-radius: 45%;
    }
    100% {
      width: 100px;
      height: 100px;
      min-height: 100px;
      border-radius: 50%;
    }
  }

  .vis-card {
    padding: 1.25rem;
  }
}
</style>
