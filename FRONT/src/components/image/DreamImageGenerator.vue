<template>
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
    </div>
    <div class="bubble-shine" v-if="!isExpanding && !showVisualization && !isCollapsing"></div>

    <!-- Visualization Card -->
    <div v-if="showVisualization" class="vis-card" ref="visCardRef">
      <div class="vis-card-header">
        <button @click="handleBackToAnalysis" class="icon-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
        </button>
        <h2 class="vis-title">꿈 그리기</h2>
      </div>

      <div class="vis-body">
        <!-- 꿈 정보 -->
        <div class="vis-preview" v-if="selectedDream">
          <div class="preview-header">
            <span class="preview-tag">꿈 내용</span>
            <h3 class="preview-title">{{ selectedDream.title }}</h3>
          </div>
          <div class="preview-content-wrapper">
            <p class="preview-text">{{ selectedDream.content }}</p>
          </div>
        </div>

        <!-- 스타일 선택 -->
        <div class="vis-section">
          <label class="vis-label">스타일 선택</label>
          <div class="style-chips">
            <button v-for="style in imageStyles" :key="style.id" :class="['style-chip', { active: selectedStyle === style.id }]" @click="selectedStyle = style.id">
              <!-- 달 아이콘 (몽환적) -->
              <svg v-if="style.icon === 'moon'" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
              </svg>
              <!-- 팔레트 아이콘 (수채화) -->
              <svg v-else-if="style.icon === 'palette'" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.555C21.965 6.012 17.461 2 12 2z"></path>
                <circle cx="13.5" cy="6.5" r="1.5"></circle>
                <circle cx="17.5" cy="10.5" r="1.5"></circle>
                <circle cx="8.5" cy="7.5" r="1.5"></circle>
                <circle cx="6.5" cy="12.5" r="1.5"></circle>
              </svg>
              <!-- 하트 아이콘 (애니) -->
              <svg v-else-if="style.icon === 'heart'" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
              <!-- 별 아이콘 (판타지) -->
              <svg v-else-if="style.icon === 'unicorn'" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path>
              </svg>
              <span>{{ style.name }}</span>
            </button>
          </div>
        </div>

        <!-- 생성 버튼 -->
        <button class="vis-generate-btn" :disabled="!selectedDream || isGenerating || !hasEnoughCoins" @click="handleGenerateClick">
          <span v-if="!isGenerating" class="btn-content">
            <svg class="sparkle-icon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0L14.59 8.41L23 11L14.59 13.59L12 22L9.41 13.59L1 11L9.41 8.41L12 0Z"/>
            </svg>
            <template v-if="!hasEnoughCoins">AI 티켓이 부족해요</template>
            <template v-else-if="hasExistingImage">이미지 다시 생성하기</template>
            <template v-else>이미지 생성하기</template>
          </span>
          <span v-else class="generating">
            <span class="dot-pulse"></span>
            생성 중...
          </span>
        </button>

        <!-- 기존 저장된 이미지 (props에서 직접) -->
        <div v-if="analysisResult?.imageUrl && generatedImages.length === 0" class="vis-results">
          <div class="result-card">
            <div class="result-image-wrapper">
              <img :src="analysisResult.imageUrl" :alt="selectedDream?.title" class="result-image-actual" />
            </div>
            <div class="result-info">
              <div class="result-actions">
                <button @click="downloadExistingImage" class="result-action-btn download-btn">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="7 10 12 15 17 10"></polyline>
                    <line x1="12" y1="15" x2="12" y2="3"></line>
                  </svg>
                  다운로드
                </button>
                <button @click="goToGallery" class="result-action-btn gallery-btn">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                    <circle cx="8.5" cy="8.5" r="1.5"></circle>
                    <polyline points="21 15 16 10 5 21"></polyline>
                  </svg>
                  갤러리로 가기
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 새로 생성된 이미지 -->
        <div v-if="generatedImages.length > 0" class="vis-results">
          <div v-for="(img, index) in generatedImages" :key="img.id || index" class="result-card">
            <div class="result-image-wrapper">
              <img :src="img.imageSrc" :alt="img.title" class="result-image-actual" @error="handleImageError($event, img)" />
            </div>
            <div class="result-info">
              <div class="result-actions">
                <button @click="downloadImage(img)" class="result-action-btn download-btn">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="7 10 12 15 17 10"></polyline>
                    <line x1="12" y1="15" x2="12" y2="3"></line>
                  </svg>
                  다운로드
                </button>
                <button @click="goToGallery" class="result-action-btn gallery-btn">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                    <circle cx="8.5" cy="8.5" r="1.5"></circle>
                    <polyline points="21 15 16 10 5 21"></polyline>
                  </svg>
                  갤러리로 가기
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, nextTick, computed, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useDreamEntriesStore } from "../../stores/dreamEntriesStore";
import { useAuthStore } from "../../stores/authStore";
import { useGalleryStore } from "../../stores/galleryStore";
import { fortuneService } from "../../services/fortuneService";
import { dreamResultService } from "../../services/dreamResultService";
import { imageService } from "../../services/imageService";
import { useConfirm } from "../../composables/useConfirm";

// 이미지 생성 취소용 AbortController
let imageAbortController = null;

const props = defineProps({
  analysisResult: {
    type: Object,
    default: null,
  },
});

const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const galleryStore = useGalleryStore();
const authStore = useAuthStore();
const { postedDates } = storeToRefs(dreamEntriesStore);
const { confirm } = useConfirm();

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

// 이미지 스타일 정의 (프론트엔드 ID -> AI API 스타일명 매핑)
const imageStyles = [
  { id: "dreamy", name: "몽환적", icon: "moon", apiStyle: "몽환적" },
  { id: "watercolor", name: "수채화", icon: "palette", apiStyle: "수채화" },
  { id: "anime", name: "애니", icon: "heart", apiStyle: "애니메이션" },
  { id: "fantasy", name: "판타지", icon: "unicorn", apiStyle: "판타지" },
];

// 코인 충분 여부 확인 (이미지 생성에 2코인 필요)
const hasEnoughCoins = computed(() => {
  const userCoin = authStore.currentUser?.coin ?? 0;
  return userCoin >= 2;
});

// 기존 이미지 존재 여부 확인
const hasExistingImage = computed(() => {
  return !!(props.analysisResult?.imageUrl || generatedImages.value.length > 0);
});

// 생성 버튼 클릭 핸들러 (재생성 시 경고)
async function handleGenerateClick() {
  if (hasExistingImage.value) {
    const confirmed = await confirm({
      title: '이미지 재생성',
      message: '이미지를 재생성하시면\n기존 이미지는 사라집니다.',
      subMessage: '재생성 하시겠습니까?',
      type: 'warning'
    });
    if (!confirmed) return;
  }
  generateImage();
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
    
    // 기존 이미지가 있으면 불러오기
    loadExistingImage();
    
    // 모바일에서 자동 스크롤
    nextTick(() => {
      if (bubbleRef.value && window.innerWidth <= 1400) {
        bubbleRef.value.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    });
  }, 500);
}

// 기존에 생성된 이미지가 있으면 props에서 불러오기
function loadExistingImage() {
  // props.analysisResult에 imageUrl이 있으면 표시
  if (props.analysisResult?.imageUrl && selectedDream.value) {
    generatedImages.value = [{
      id: selectedDream.value.dreamId,
      dreamId: selectedDream.value.dreamId,
      dreamKey: selectedDreamKey.value,
      dreamDate: selectedDreamKey.value,
      title: selectedDream.value.title,
      content: selectedDream.value.content,
      style: "저장된 이미지",
      imageSrc: props.analysisResult.imageUrl,
      mimeType: "image/png",
      caption: selectedDream.value.title,
      createdAt: new Date().toISOString(),
    }];
  } else {
    generatedImages.value = [];
  }
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

// 이미지 생성 취소
function cancelImageGeneration() {
  if (imageAbortController) {
    imageAbortController.abort();
    imageAbortController = null;
    isGenerating.value = false;
    return true;
  }
  return false;
}

async function generateImage() {
  if (!selectedDream.value || !hasEnoughCoins.value) return;

  // 기존 요청이 있으면 취소
  if (imageAbortController) {
    imageAbortController.abort();
  }
  
  // 새 AbortController 생성
  imageAbortController = new AbortController();

  isGenerating.value = true;
  
  // 기존 이미지 초기화 (재생성 시 새 이미지만 표시)
  generatedImages.value = [];

  try {
    // 선택된 스타일 정보 가져오기
    const styleInfo = imageStyles.find((s) => s.id === selectedStyle.value);

    // 꿈 내용으로 프롬프트 구성 (제목 + 내용)
    const dreamPrompt = `${selectedDream.value.title}. ${selectedDream.value.content}`;

    // AI API 호출 (코인 차감 포함, 취소 가능)
    const response = await fortuneService.generateDreamImage({
      dream_prompt: dreamPrompt,
      style: styleInfo.apiStyle,
    }, imageAbortController.signal);

    if (response.success && response.images && response.images.length > 0) {
      // 생성된 이미지를 목록에 추가하고 자동 저장
      for (const img of response.images) {
        const imageEntry = {
          id: selectedDream.value.dreamId, // dreamId를 id로 사용하여 갤러리 중복 방지
          dreamId: selectedDream.value.dreamId,
          dreamKey: selectedDreamKey.value,
          dreamDate: selectedDreamKey.value,
          title: selectedDream.value.title,
          content: selectedDream.value.content,
          interpretation: props.analysisResult?.dreamInterpretation || null,
          fortuneSummary: props.analysisResult?.todayFortuneSummary || null,
          luckyColor: props.analysisResult?.luckyColor || null,
          luckyItem: props.analysisResult?.luckyItem || null,
          style: styleInfo.name,
          // Base64 이미지 데이터
          imageData: img.image_data,
          mimeType: img.mime_type,
          // 이미지 소스 URL (data URI)
          imageSrc: `data:${img.mime_type};base64,${img.image_data}`,
          caption: `${selectedDream.value.title} - ${styleInfo.name}`,
          createdAt: new Date().toISOString(),
          modelText: response.model_text || null,
        };

        generatedImages.value.unshift(imageEntry);

        // 자동으로 갤러리에 저장
        await saveToGallery(imageEntry, false);
      }

      // 이미지 생성 성공 후 코인 정보 갱신 (UI 반영)
      await authStore.fetchCurrentUser();
    } else {
      console.error("❌ 이미지 생성 실패:", response.message);
    }
  } catch (error) {
    // 요청이 취소된 경우
    if (error.name === 'CanceledError' || error.name === 'AbortError') {
      console.log("ℹ️ 이미지 생성 요청이 사용자에 의해 취소됨");
      return;
    }
    console.error("❌ 이미지 생성 에러:", error);
  } finally {
    isGenerating.value = false;
    imageAbortController = null;
  }
}

async function saveToGallery(image, showAlert = true) {
  try {
    let savedImageUrl = image.imageSrc;

    // 1. 백엔드에 이미지 업로드 (Base64 -> 파일 저장 -> URL 반환)
    try {
      const uploadResponse = await imageService.uploadImage(image.imageSrc, image.dreamId);

      if (uploadResponse.success && uploadResponse.imageUrl) {
        savedImageUrl = uploadResponse.imageUrl;

        // 2. dream_results 테이블에 이미지 URL 업데이트
        if (image.dreamId) {
          await dreamResultService.updateDreamResult(image.dreamId, {
            imageUrl: savedImageUrl,
          });
        }
      }
    } catch (uploadError) {
      console.warn("⚠️ 서버 업로드 실패, 로컬에만 저장:", uploadError.message);
      // 업로드 실패 시 기존 Base64 데이터로 저장 (폴백)
    }

    // 3. 갤러리 스토어에 저장 (꿈 내용과 해석 정보 포함)
    galleryStore.addToGallery({
      id: image.id,
      dreamId: image.dreamId,
      dreamDate: image.dreamDate,
      title: image.title,
      content: image.content,
      interpretation: image.interpretation,
      fortuneSummary: image.fortuneSummary,
      luckyColor: image.luckyColor,
      luckyItem: image.luckyItem,
      style: image.style,
      caption: image.caption,
      imageSrc: savedImageUrl,
      mimeType: image.mimeType,
      createdAt: image.createdAt,
      savedAt: new Date().toISOString(),
    });

    if (showAlert) {
      alert("갤러리에 저장되었습니다! 🎉");
    }
  } catch (error) {
    console.error("갤러리 저장 실패:", error);
    if (showAlert) {
      alert("저장 중 오류가 발생했습니다.");
    }
  }
}

// 이미지 로드 에러 처리
function handleImageError(event, img) {
  console.error("이미지 로드 실패:", img.caption);
  event.target.src =
    "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'%3E%3Crect fill='%23f0f0f0' width='200' height='200'/%3E%3Ctext fill='%23999' font-family='sans-serif' font-size='14' text-anchor='middle' x='100' y='100'%3E이미지 로드 실패%3C/text%3E%3C/svg%3E";
}

// 이미지 다운로드
function downloadImage(image) {
  try {
    const link = document.createElement("a");
    link.href = image.imageSrc;

    // 파일명 생성
    const timestamp = new Date().toISOString().slice(0, 10);
    const ext = image.mimeType?.split("/")[1] || "png";
    link.download = `dream_${timestamp}_${image.style}.${ext}`;

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error("다운로드 실패:", error);
    alert("다운로드 중 오류가 발생했습니다.");
  }
}

// 기존 이미지 다운로드 (props에서)
function downloadExistingImage() {
  if (!props.analysisResult?.imageUrl) return;
  
  try {
    const link = document.createElement("a");
    link.href = props.analysisResult.imageUrl;

    const timestamp = new Date().toISOString().slice(0, 10);
    link.download = `dream_${timestamp}.png`;

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error("다운로드 실패:", error);
    alert("다운로드 중 오류가 발생했습니다.");
  }
}

function goToGallery() {
  router.push("/gallery");
}

// 브라우저 새로고침/닫기 시 경고 (beforeunload는 부모 컴포넌트에서 처리)
onUnmounted(() => {
  // 생성 중이면 취소
  if (isGenerating.value) {
    cancelImageGeneration();
  }
});

// 외부에서 확장 상태 및 생성 상태 확인용
defineExpose({
  isExpanding,
  showVisualization,
  isGenerating,
  cancelImageGeneration,
});
</script>

<style scoped>
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
  box-shadow: 0 20px 50px var(--shadow-purple);
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
  box-shadow: 0 20px 50px var(--shadow-purple);
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1), height 0.5s cubic-bezier(0.4, 0, 0.2, 1), min-height 0.5s cubic-bezier(0.4, 0, 0.2, 1), border-radius 0.4s cubic-bezier(0.4, 0, 0.2, 1),
    background 0.3s ease, box-shadow 0.4s ease;
}

/* Collapsing - 카드에서 비눗방울로 morphing */
.bubble-container.collapsing {
  position: relative;
  bottom: auto;
  right: auto;
  animation: collapseWidthHeight 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  background: linear-gradient(135deg, #c77dff, #6fa7ff);
  box-shadow: 0 20px 40px rgba(99, 70, 171, 0.3), inset 0 0 25px rgba(255, 255, 255, 0.5);
  cursor: default;
  overflow: hidden;
  z-index: 100;
  transform-origin: center center;
  pointer-events: none;
}

/* 축소 애니메이션 (Width & Height) */
@keyframes collapseWidthHeight {
  0% {
    width: 420px;
    height: auto;
    min-height: 400px;
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
    opacity: 0;
    transform: scale(0.8);
  }
  100% {
    width: 130px;
    height: 130px;
    min-height: 130px;
    border-radius: 50%;
    opacity: 0;
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
  border-bottom: 1px dashed var(--border-dashed-purple);
}

.vis-title {
  font-family: 'Dongle', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text-primary);
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
  font-family: 'Dongle', sans-serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.vis-preview {
  background: var(--gradient-bg-15);
  padding: 1rem;
  border-radius: 16px;
  max-height: 200px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-bottom: 0.5rem;
  flex-shrink: 0;
}

.preview-tag {
  font-family: 'Dongle', sans-serif;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--color-purple-dark);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.preview-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.3;
  word-break: keep-all;
}

.preview-content-wrapper {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.preview-content-wrapper::-webkit-scrollbar {
  width: 4px;
}

.preview-content-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.preview-content-wrapper::-webkit-scrollbar-thumb {
  background: var(--color-purple-40);
  border-radius: 4px;
}

.preview-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: var(--color-purple-60);
}

.preview-text {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
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
  border: 2px solid var(--border-purple);
  border-radius: 14px;
  background: white;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-text-muted);
  transition: all 0.2s;
}

.style-chip svg {
  flex-shrink: 0;
  stroke: var(--color-text-muted);
}

.style-chip svg[fill="currentColor"] {
  fill: var(--color-text-muted);
  stroke: none;
}

.style-chip.active svg {
  stroke: var(--color-text-primary);
}

.style-chip.active svg[fill="currentColor"] {
  fill: var(--color-text-primary);
  stroke: none;
}

.style-chip:hover {
  border-color: var(--color-purple);
  background: var(--color-purple-light);
}

.style-chip.active {
  border-color: var(--color-purple-dark);
  background: var(--gradient-bg-15);
  color: var(--color-text-primary);
}

.vis-generate-btn {
  width: 100%;
  padding: 0.6rem 1rem;
  border: none;
  border-radius: 14px;
  font-size: 1.4rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #d9a8ff, #a3c7ff);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 8px 20px rgba(99, 70, 171, 0.25);
  font-family: 'Dongle', sans-serif !important;
}

.vis-generate-btn .btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.vis-generate-btn .sparkle-icon {
  flex-shrink: 0;
}

.vis-generate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(99, 70, 171, 0.4);
}

.vis-generate-btn:disabled {
  opacity: 0.6;
  cursor: default;
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
  flex-direction: column;
  gap: 0.75rem;
  padding: 1rem;
  background: var(--color-purple-light);
  border-radius: 16px;
  border: 1px solid var(--border-purple);
}

.result-image-wrapper {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 12px;
  overflow: hidden;
  background: var(--gradient-bg-15);
}

.result-image-actual {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.result-image-actual:hover {
  transform: scale(1.02);
}

.result-info {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.result-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.result-action-btn {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: none;
  border-radius: 8px;
  font-size: 1.2rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Dongle', sans-serif !important;
}

.result-action-btn.download-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  background: var(--color-purple-15);
  color: var(--color-text-secondary);
}

.result-action-btn.download-btn svg {
  flex-shrink: 0;
}

.result-action-btn.download-btn:hover {
  background: var(--color-purple-20);
  color: var(--color-text-primary);
}

.result-action-btn.gallery-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  background: linear-gradient(135deg, #d9a8ff, #a3c7ff);
  color: white;
}

.result-action-btn.gallery-btn svg {
  flex-shrink: 0;
}

.result-action-btn.gallery-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 70, 171, 0.25);
}

/* ===== Responsive ===== */
@media (max-width: 1400px) {
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
    width: 100%;
    max-width: 500px;
    margin: 1.5rem auto;
    position: relative;
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
    }
  }
}

@media (max-width: 768px) {
  .bubble-container {
    width: 100px;
    height: 100px;
  }

  .bubble-btn {
    font-size: 0.95rem;
  }

  .bubble-container.expanding,
  .bubble-container.expanded {
    width: 100%;
    max-width: none;
    margin: 1rem 0;
    border-radius: 24px;
  }

  .bubble-container.collapsing {
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

  .vis-preview {
    padding: 0.875rem 1rem;
    max-height: 180px;
  }

  .preview-title {
    font-size: 0.95rem;
  }

  .preview-text {
    font-size: 0.85rem;
    line-height: 1.5;
  }
}
</style>



