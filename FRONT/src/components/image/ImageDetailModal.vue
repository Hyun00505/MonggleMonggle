<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="image" class="modal-overlay" @click="$emit('close')">
        <!-- 이전 이미지 버튼 -->
        <button v-if="hasPrev" @click.stop="$emit('prev')" class="nav-btn nav-prev">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </button>

        <div class="modal-content" @click.stop>
          <button @click="$emit('close')" class="icon-btn icon-btn-absolute" aria-label="닫기">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>

          <div class="modal-layout">
            <!-- 왼쪽: 액자에 담긴 이미지 -->
            <div class="modal-image-section">
              <div class="frame-container">
                <div class="frame-outer">
                  <div class="frame-inner">
                    <div v-if="image.imageSrc" class="framed-image">
                      <img :src="resolveImageSrc(image.imageSrc)" :alt="image.caption" />
                    </div>
                    <div v-else class="framed-placeholder" :style="{ background: image.gradient }">
                      <span class="placeholder-emoji">{{ image.emoji }}</span>
                    </div>
                  </div>
                </div>
                <!-- 액자 장식 -->
                <div class="frame-shadow"></div>
              </div>
            </div>

            <!-- 오른쪽: 정보 섹션 -->
            <div class="modal-info-section">
              <!-- 제목과 날짜 -->
              <div class="modal-header-info">
                <h2>{{ image.title || image.caption }}</h2>
                <div class="modal-meta">
                  <span class="meta-badge date">
                    <span class="badge-icon">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                      </svg>
                    </span>
                    {{ formatDreamDate(image.dreamDate) || formatDate(image.createdAt) }}
                  </span>
                </div>
              </div>

              <!-- 스크롤 가능한 컨텐츠 영역 -->
              <div class="modal-scrollable">
                <!-- 꿈 일기 본문 -->
                <div v-if="image.content" class="modal-section dream-content-section">
                  <h3 class="section-title">
                    <span class="section-icon">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                        <polyline points="10 9 9 9 8 9"></polyline>
                      </svg>
                    </span>
                    꿈 일기 내용
                  </h3>
                  <p class="dream-content-text">{{ image.content }}</p>
                </div>
              </div>

              <!-- 액션 버튼 -->
              <div class="modal-actions">
                <button @click="openOriginalImage" class="modal-action-btn ghost">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="15 3 21 3 21 9"></polyline>
                    <polyline points="9 21 3 21 3 15"></polyline>
                    <line x1="21" y1="3" x2="14" y2="10"></line>
                    <line x1="3" y1="21" x2="10" y2="14"></line>
                  </svg>
                  원본 보기
                </button>
                <button @click="handleDownload" class="modal-action-btn primary">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="7 10 12 15 17 10"></polyline>
                    <line x1="12" y1="15" x2="12" y2="3"></line>
                  </svg>
                  다운로드
                </button>
                <button @click="$emit('delete', image)" class="modal-action-btn danger">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 다음 이미지 버튼 -->
        <button v-if="hasNext" @click.stop="$emit('next')" class="nav-btn nav-next">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </button>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { watch, onMounted, onUnmounted } from "vue";

const props = defineProps({
  image: {
    type: Object,
    default: null,
  },
  hasPrev: {
    type: Boolean,
    default: false,
  },
  hasNext: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["close", "prev", "next", "delete"]);

// 이미지 경로를 실제 접근 가능한 URL로 정규화
function resolveImageSrc(src) {
  if (!src) return "";
  // Base64 Data URI는 그대로 반환
  if (src.startsWith("data:")) return src;
  if (src.startsWith("http://") || src.startsWith("https://")) return src;
  if (src.startsWith("/")) return src;
  return `/${src}`;
}

function formatDate(dateString) {
  if (!dateString) return "";
  const date = new Date(dateString);
  return `${date.getMonth() + 1}월 ${date.getDate()}일`;
}

function formatDreamDate(dateKey) {
  if (!dateKey) return "";
  const [year, month, day] = dateKey.split("-");
  return `${month}월 ${day}일`;
}

// 색상 이름을 HEX 코드로 변환
function getLuckyColorHex(colorName) {
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
  return colorMap[colorName] || "#CDB4DB";
}

function openOriginalImage() {
  if (!props.image?.imageSrc) return;
  
  const src = props.image.imageSrc;
  
  // Base64 Data URI인 경우 Blob URL로 변환하여 열기
  if (src.startsWith("data:")) {
    try {
      // Data URI에서 base64 데이터 추출
      const [header, base64Data] = src.split(",");
      const mimeType = header.match(/data:(.*?);/)?.[1] || "image/png";
      
      // Base64를 Blob으로 변환
      const byteCharacters = atob(base64Data);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: mimeType });
      
      // Blob URL 생성 및 새 창에서 열기
      const blobUrl = URL.createObjectURL(blob);
      window.open(blobUrl, "_blank");
    } catch (error) {
      console.error("원본 이미지 열기 실패:", error);
      alert("이미지를 열 수 없습니다.");
    }
  } else {
    // 일반 URL인 경우 그대로 열기
    window.open(src, "_blank");
  }
}

function handleDownload() {
  if (!props.image?.imageSrc) {
    alert("다운로드할 수 있는 이미지가 없습니다.");
    return;
  }

  try {
    const link = document.createElement("a");
    link.href = props.image.imageSrc;

    // 파일명 생성
    const timestamp = new Date().toISOString().slice(0, 10);
    const ext = props.image.mimeType?.split("/")[1] || "png";
    link.download = `dream_${timestamp}_image.${ext}`;

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error("다운로드 실패:", error);
    alert("다운로드 중 오류가 발생했습니다.");
  }
}

// 키보드 네비게이션
function handleKeydown(e) {
  if (!props.image) return;
  if (e.key === "ArrowLeft") emit("prev");
  if (e.key === "ArrowRight") emit("next");
  if (e.key === "Escape") emit("close");
}

// body 스크롤 제어
watch(
  () => props.image,
  (newVal) => {
    if (newVal) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
  },
  { immediate: true }
);

onMounted(() => {
  window.addEventListener("keydown", handleKeydown);
});

onUnmounted(() => {
  window.removeEventListener("keydown", handleKeydown);
  document.body.style.overflow = "";
});
</script>

<style scoped>
/* ===== 모달 ===== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 1rem;
}

/* 네비게이션 버튼 */
.nav-btn {
  position: relative;
  z-index: 10;
  width: 52px;
  height: 52px;
  border: none;
  border-radius: 50%;
  background: white;
  color: var(--color-purple);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  box-shadow: 0 4px 20px var(--shadow-purple);
  flex-shrink: 0;
}

.nav-btn:hover {
  background: var(--color-purple);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 6px 25px var(--shadow-purple-md);
}

.nav-btn:active {
  transform: scale(0.95);
}

.modal-content {
  background: transparent;
  border-radius: 0;
  max-width: 1000px;
  width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  flex-shrink: 1;
}

/* 모달 레이아웃 */
.modal-layout {
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  gap: 0;
  border-radius: 32px;
  box-shadow: 0 25px 80px rgba(100, 100, 200, 0.4);
}

/* 왼쪽: 이미지 섹션 - 밤하늘 테마 */
.modal-image-section {
  flex: 0 0 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2.5rem 2rem;
  position: relative;
  min-width: 0;
  background: linear-gradient(180deg, #454278 0%, #7C78B8 60%, #C4B6DC 100%);
  border-radius: 32px 0 0 32px;
  overflow: hidden;
}

/* 별 효과 */
.modal-image-section::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(2px 2px at 20px 30px, white, transparent),
    radial-gradient(2px 2px at 40px 70px, rgba(255,255,255,0.8), transparent),
    radial-gradient(1px 1px at 90px 40px, white, transparent),
    radial-gradient(2px 2px at 130px 80px, rgba(255,255,255,0.6), transparent),
    radial-gradient(1px 1px at 160px 30px, white, transparent),
    radial-gradient(2px 2px at 200px 60px, rgba(255,255,255,0.7), transparent),
    radial-gradient(1px 1px at 50px 120px, white, transparent),
    radial-gradient(2px 2px at 100px 150px, rgba(255,255,255,0.5), transparent),
    radial-gradient(1px 1px at 180px 140px, white, transparent);
  pointer-events: none;
  animation: twinkle-stars 4s ease-in-out infinite alternate;
}

@keyframes twinkle-stars {
  0% { opacity: 0.5; }
  100% { opacity: 1; }
}

/* 액자 컨테이너 - 부드러운 스타일 */
.frame-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 380px;
}

/* 부드러운 파스텔 액자 */
.frame-outer {
  background: linear-gradient(145deg, rgba(255,255,255,0.9), rgba(255,255,255,0.7));
  padding: 12px;
  border-radius: 20px;
  box-shadow: 
    0 20px 50px rgba(0, 0, 0, 0.2),
    0 8px 20px rgba(0, 0, 0, 0.1),
    inset 0 1px 2px rgba(255, 255, 255, 0.8);
}

/* 액자 내부 그라데이션 테두리 */
.frame-inner {
  background: var(--gradient-primary);
  padding: 4px;
  border-radius: 16px;
}

.framed-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 14px;
  overflow: hidden;
}

.framed-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.framed-placeholder {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--color-purple-15), var(--color-pink-15));
}

.placeholder-emoji {
  font-size: 5rem;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
}

/* 액자 그림자 제거 (새 스타일에서 불필요) */
.frame-shadow {
  display: none;
}


/* 오른쪽: 정보 섹션 */
.modal-info-section {
  flex: 0 0 50%;
  display: flex;
  flex-direction: column;
  padding: 2rem;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  background: white;
  border-radius: 0 32px 32px 0;
}

.modal-header-info {
  padding-bottom: 1.25rem;
  border-bottom: 2px dashed var(--border-purple);
  margin-bottom: 1.25rem;
  flex-shrink: 0;
}

.modal-header-info h2 {
  margin: 0 0 0.85rem 0;
  font-family: "Dongle", sans-serif;
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
  word-break: keep-all;
}

.modal-meta {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.meta-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.45rem 0.95rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 700;
}

.meta-badge.date {
  background: var(--gradient-bg-15);
  color: var(--color-text-secondary);
  border: 1px solid var(--border-purple);
  font-family: "Dongle", sans-serif;
  font-size: 1.2rem;
  font-weight: 600;
}

.badge-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge-icon svg {
  width: 14px;
  height: 14px;
}

/* 스크롤 영역 */
.modal-scrollable {
  flex: 1;
  overflow-y: auto;
  padding-right: 0.5rem;
  margin-bottom: 1rem;
  min-height: 0;
}

.modal-scrollable::-webkit-scrollbar {
  width: 5px;
}

.modal-scrollable::-webkit-scrollbar-track {
  background: transparent;
}

.modal-scrollable::-webkit-scrollbar-thumb {
  background: var(--color-purple-40);
  border-radius: 5px;
}

.modal-scrollable::-webkit-scrollbar-thumb:hover {
  background: var(--color-purple-60);
}

/* 모달 섹션 */
.modal-section {
  margin-bottom: 0.85rem;
  padding: 1rem 1.1rem;
  border-radius: 16px;
  transition: transform 0.2s;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 0.6rem 0;
  flex-shrink: 0;
}

.section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: inherit;
}

.section-icon svg {
  width: 16px;
  height: 16px;
}

/* 꿈 일기 내용 섹션 */
.dream-content-section {
  background: var(--gradient-bg-15);
  border-radius: 16px;
  max-height: 230px;
  display: flex;
  flex-direction: column;
}

.dream-content-text {
  margin: 0;
  color: #555;
  line-height: 1.75;
  font-size: 0.9rem;
  white-space: pre-wrap;
  word-break: break-word;
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.dream-content-text::-webkit-scrollbar {
  width: 4px;
}

.dream-content-text::-webkit-scrollbar-track {
  background: transparent;
}

.dream-content-text::-webkit-scrollbar-thumb {
  background: var(--color-purple-40);
  border-radius: 4px;
}

.dream-content-text::-webkit-scrollbar-thumb:hover {
  background: var(--color-purple-60);
}

/* 꿈 해석 섹션 */
.interpretation-section {
  background: linear-gradient(135deg, var(--color-blue-15), var(--color-purple-15));
  border-left: 3px solid var(--color-blue);
}

.interpretation-text {
  margin: 0;
  color: #4a6a8a;
  line-height: 1.75;
  font-size: 0.9rem;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 오늘의 운세 섹션 */
.fortune-section {
  background: linear-gradient(135deg, var(--color-pink-15), rgba(255, 224, 130, 0.15));
  border-left: 3px solid var(--color-pink);
}

.fortune-text {
  margin: 0;
  color: #8a5a6a;
  line-height: 1.75;
  font-size: 0.9rem;
  word-break: break-word;
}

/* 행운 정보 섹션 */
.lucky-section {
  background: var(--gradient-bg-15);
  border-left: 3px solid var(--color-blue);
  padding: 0.85rem 1.1rem;
}

.lucky-items {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.lucky-item {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.lucky-label {
  font-size: 0.7rem;
  color: var(--color-text-muted);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.lucky-value {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.lucky-value.color-value {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.color-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 액션 버튼 */
.modal-actions {
  display: flex;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 2px dashed var(--border-purple);
  flex-shrink: 0;
}

.modal-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.85rem 1rem;
  flex: 1;
  border: none;
  border-radius: 14px;
  font-weight: 700;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-action-btn.ghost {
  background: var(--color-purple-light);
  color: var(--color-text-secondary);
  border: 1px solid var(--border-purple);
}

.modal-action-btn.ghost:hover {
  background: var(--color-purple-20);
  border-color: var(--color-purple);
}

.modal-action-btn.primary {
  background: var(--gradient-purple-blue);
  color: white;
  box-shadow: 0 4px 15px var(--shadow-purple);
}

.modal-action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--shadow-purple-md);
}

.modal-action-btn.danger {
  background: linear-gradient(135deg, #9a96b8, #7c78a8);
  color: white;
  box-shadow: 0 4px 15px rgba(124, 120, 168, 0.25);
}

.modal-action-btn.danger:hover {
  background: linear-gradient(135deg, #8a86a8, #6c6898);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 120, 168, 0.35);
}

/* 모달 애니메이션 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.35s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-content,
.modal-fade-leave-to .modal-content {
  transform: scale(0.95) translateY(20px);
}

.modal-fade-enter-active .modal-content,
.modal-fade-leave-active .modal-content {
  transition: transform 0.35s ease;
}

/* ===== 반응형 - 태블릿 ===== */
@media (max-width: 950px) {
  .modal-overlay {
    padding: 1rem;
    flex-direction: row;
  }

  .nav-btn {
    width: 46px;
    height: 46px;
  }

  .modal-layout {
    flex-direction: column;
    overflow-y: auto;
    border-radius: 28px;
  }

  .modal-image-section {
    flex: none;
    padding: 2rem 1.5rem;
    border-radius: 28px 28px 0 0;
  }

  .frame-container {
    max-width: 280px;
  }

  .modal-info-section {
    flex: none;
    padding: 1.5rem;
    max-height: none;
    overflow: visible;
    border-radius: 0 0 28px 28px;
  }

  .modal-scrollable {
    overflow: visible;
    max-height: none;
  }

  .modal-header-info h2 {
    font-size: 1.75rem;
  }
}

/* ===== 반응형 - 모바일 ===== */
@media (max-width: 600px) {
  .modal-overlay {
    padding: 0.75rem;
    flex-direction: row;
    gap: 0.5rem;
  }

  .nav-btn {
    width: 40px;
    height: 40px;
  }

  .nav-btn svg {
    width: 20px;
    height: 20px;
  }

  .modal-content {
    max-height: 90vh;
  }

  .modal-layout {
    flex-direction: column;
    overflow-y: auto;
    max-height: calc(90vh - 20px);
    border-radius: 24px;
  }

  .modal-image-section {
    padding: 1.5rem 1rem;
    flex-shrink: 0;
    border-radius: 24px 24px 0 0;
  }

  .frame-container {
    max-width: 220px;
  }

  .frame-outer {
    padding: 8px;
    border-radius: 16px;
  }

  .frame-inner {
    padding: 3px;
    border-radius: 14px;
  }

  .framed-image {
    border-radius: 12px;
  }

  .placeholder-emoji {
    font-size: 3.5rem;
  }

  .modal-info-section {
    padding: 1.25rem;
    border-radius: 0 0 24px 24px;
  }

  .modal-header-info {
    padding-bottom: 0.85rem;
    margin-bottom: 0.85rem;
  }

  .modal-header-info h2 {
    font-size: 1.5rem;
    margin-bottom: 0.6rem;
  }

  .meta-badge {
    padding: 0.35rem 0.7rem;
    font-size: 0.8rem;
  }

  .modal-section {
    padding: 0.9rem 1rem;
    margin-bottom: 0.75rem;
    border-radius: 14px;
  }

  .section-title {
    font-size: 0.85rem;
    margin-bottom: 0.5rem;
  }

  .dream-content-text,
  .interpretation-text,
  .fortune-text {
    font-size: 0.85rem;
    line-height: 1.7;
  }

  .lucky-section {
    padding: 0.75rem 1rem;
  }

  .lucky-items {
    gap: 1.25rem;
  }

  .lucky-label {
    font-size: 0.65rem;
  }

  .lucky-value {
    font-size: 0.9rem;
  }

  .color-dot {
    width: 14px;
    height: 14px;
  }

  .modal-actions {
    gap: 0.6rem;
    padding-top: 0.85rem;
    flex-wrap: wrap;
  }

  .modal-action-btn {
    padding: 0.75rem 0.85rem;
    font-size: 0.8rem;
    gap: 0.3rem;
    border-radius: 12px;
  }

  .modal-action-btn svg {
    width: 16px;
    height: 16px;
  }
}

/* ===== 반응형 - 작은 모바일 ===== */
@media (max-width: 400px) {
  .nav-btn {
    width: 36px;
    height: 36px;
  }

  .nav-btn svg {
    width: 18px;
    height: 18px;
  }

  .frame-container {
    max-width: 180px;
  }

  .frame-outer {
    padding: 6px;
  }

  .frame-inner {
    padding: 2px;
  }

  .modal-info-section {
    padding: 1rem;
  }

  .modal-header-info h2 {
    font-size: 1.3rem;
  }

  .modal-section {
    padding: 0.75rem 0.85rem;
  }

  .dream-content-text,
  .interpretation-text,
  .fortune-text {
    font-size: 0.8rem;
  }

  .modal-actions {
    flex-direction: column;
    gap: 0.5rem;
  }

  .modal-action-btn {
    width: 100%;
    justify-content: center;
    padding: 0.7rem 0.75rem;
    font-size: 0.78rem;
  }
}
</style>

