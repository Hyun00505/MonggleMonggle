<template>
  <div class="gallery-card">
    <div class="card-header">
      <button @click="handleBack" class="back-btn">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
      </button>
      <h2 class="page-title">이미지 갤러리</h2>
      <div class="spacer"></div>
    </div>

    <div class="gallery-content">
      <!-- 필터 및 검색 -->
      <div class="filter-section">
        <div class="search-box">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <input v-model="searchQuery" type="text" placeholder="제목이나 태그로 검색..." class="search-input" />
        </div>

        <div class="filter-buttons">
          <button v-for="filter in filters" :key="filter.id" :class="['filter-btn', { active: activeFilter === filter.id }]" @click="activeFilter = filter.id">
            <span>{{ filter.emoji }}</span>
            <span>{{ filter.label }}</span>
          </button>
        </div>
      </div>

      <!-- 갤러리 통계 -->
      <div class="gallery-stats">
        <div class="stat-item">
          <span class="stat-icon">🖼️</span>
          <span class="stat-text">
            전체
            <strong>{{ filteredImages.length }}</strong>
            개
          </span>
        </div>
        <div class="stat-item">
          <span class="stat-icon">❤️</span>
          <span class="stat-text">
            좋아요
            <strong>{{ totalLikes }}</strong>
            개
          </span>
        </div>
      </div>

      <!-- 뷰 모드 선택 -->
      <div class="view-mode-selector">
        <button :class="['view-mode-btn', { active: viewMode === 'grid' }]" @click="viewMode = 'grid'">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="7" height="7"></rect>
            <rect x="14" y="3" width="7" height="7"></rect>
            <rect x="14" y="14" width="7" height="7"></rect>
            <rect x="3" y="14" width="7" height="7"></rect>
          </svg>
        </button>
        <button :class="['view-mode-btn', { active: viewMode === 'masonry' }]" @click="viewMode = 'masonry'">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="8" y1="6" x2="21" y2="6"></line>
            <line x1="8" y1="12" x2="21" y2="12"></line>
            <line x1="8" y1="18" x2="21" y2="18"></line>
            <line x1="3" y1="6" x2="3.01" y2="6"></line>
            <line x1="3" y1="12" x2="3.01" y2="12"></line>
            <line x1="3" y1="18" x2="3.01" y2="18"></line>
          </svg>
        </button>
      </div>

      <!-- 이미지 그리드 -->
      <div v-if="filteredImages.length > 0" :class="['gallery-grid', viewMode]">
        <div v-for="image in filteredImages" :key="image.id" class="gallery-item" @click="openImageDetail(image)">
          <!-- 실제 이미지가 있는 경우 -->
          <div v-if="image.imageSrc" class="image-container real-image">
            <img :src="resolveImageSrc(image.imageSrc)" :alt="image.caption" class="gallery-image" />
            <div class="image-hover-overlay">
              <span class="hover-icon">🔍</span>
            </div>
          </div>
          <!-- 기존 gradient/emoji 표시 (이전 형식 호환) -->
          <div v-else class="image-container" :style="{ background: image.gradient }">
            <div class="image-overlay">
              <span class="image-emoji">{{ image.emoji }}</span>
            </div>
          </div>
          <div class="image-info">
            <h4 class="image-title">{{ image.title || image.caption }}</h4>
            <div class="image-meta">
              <span class="meta-item date-badge">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                {{ formatDreamDate(image.dreamDate) || formatDate(image.createdAt) }}
              </span>
              <span class="meta-item style-badge">
                {{ image.style }}
              </span>
            </div>
            <div class="image-actions">
              <button @click.stop="toggleLike(image)" :class="['action-btn', { liked: image.liked }]">
                <svg width="16" height="16" viewBox="0 0 24 24" :fill="image.liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                </svg>
                {{ image.likes }}
              </button>
              <button @click.stop="shareImage(image)" class="action-btn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="18" cy="5" r="3"></circle>
                  <circle cx="6" cy="12" r="3"></circle>
                  <circle cx="18" cy="19" r="3"></circle>
                  <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                  <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
                </svg>
              </button>
              <button @click.stop="deleteImage(image)" class="action-btn delete">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-else class="empty-gallery">
        <span class="empty-emoji">🎨</span>
        <h3>아직 생성된 이미지가 없습니다</h3>
        <p>꿈 시각화 페이지에서 꿈을 이미지로 만들어보세요!</p>
        <button @click="goToVisualization" class="create-btn">✨ 이미지 생성하러 가기</button>
      </div>
    </div>

    <!-- 이미지 상세 모달 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="selectedImage" class="modal-overlay" @click="closeImageDetail">
          <!-- 이전 이미지 버튼 -->
          <button v-if="hasPrevImage" @click.stop="goToPrevImage" class="nav-btn nav-prev">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
          </button>

          <div class="modal-content" @click.stop>
            <button @click="closeImageDetail" class="modal-close">
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
                      <div v-if="selectedImage.imageSrc" class="framed-image">
                        <img :src="resolveImageSrc(selectedImage.imageSrc)" :alt="selectedImage.caption" />
                      </div>
                      <div v-else class="framed-placeholder" :style="{ background: selectedImage.gradient }">
                        <span class="placeholder-emoji">{{ selectedImage.emoji }}</span>
                      </div>
                    </div>
                  </div>
                  <!-- 액자 장식 -->
                  <div class="frame-shadow"></div>
                </div>
                <!-- 액자 아래 스타일 뱃지 -->
                <div class="style-label">
                  <span class="style-icon">🎨</span>
                  {{ selectedImage.style }}
                </div>
              </div>

              <!-- 오른쪽: 정보 섹션 -->
              <div class="modal-info-section">
                <!-- 제목과 날짜 -->
                <div class="modal-header-info">
                  <h2>{{ selectedImage.title || selectedImage.caption }}</h2>
                  <div class="modal-meta">
                    <span class="meta-badge date">
                      <span class="badge-icon">📅</span>
                      {{ formatDreamDate(selectedImage.dreamDate) || formatDate(selectedImage.createdAt) }}
                    </span>
                  </div>
                </div>

                <!-- 스크롤 가능한 컨텐츠 영역 -->
                <div class="modal-scrollable">
                  <!-- 꿈 일기 본문 -->
                  <div v-if="selectedImage.content" class="modal-section dream-content-section">
                    <h3 class="section-title">
                      <span class="section-icon">📝</span>
                      꿈 일기 내용
                    </h3>
                    <p class="dream-content-text">{{ selectedImage.content }}</p>
                  </div>

                  <!-- 꿈 해석 -->
                  <div v-if="selectedImage.interpretation" class="modal-section interpretation-section">
                    <h3 class="section-title">
                      <span class="section-icon">🔮</span>
                      꿈 해석
                    </h3>
                    <p class="interpretation-text">{{ selectedImage.interpretation }}</p>
                  </div>

                  <!-- 오늘의 운세 요약 -->
                  <div v-if="selectedImage.fortuneSummary" class="modal-section fortune-section">
                    <h3 class="section-title">
                      <span class="section-icon">✨</span>
                      오늘의 운세
                    </h3>
                    <p class="fortune-text">{{ selectedImage.fortuneSummary }}</p>
                  </div>

                  <!-- 행운 정보 -->
                  <div v-if="selectedImage.luckyColor || selectedImage.luckyItem" class="modal-section lucky-section">
                    <div class="lucky-items">
                      <div v-if="selectedImage.luckyColor" class="lucky-item">
                        <span class="lucky-label">행운의 색상</span>
                        <span class="lucky-value color-value">
                          <span class="color-dot" :style="{ background: getLuckyColorHex(selectedImage.luckyColor.name) }"></span>
                          {{ selectedImage.luckyColor.name }}
                        </span>
                      </div>
                      <div v-if="selectedImage.luckyItem" class="lucky-item">
                        <span class="lucky-label">행운의 아이템</span>
                        <span class="lucky-value">{{ selectedImage.luckyItem.name }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 액션 버튼 -->
                <div class="modal-actions">
                  <button @click="openOriginalImage(selectedImage)" class="modal-action-btn ghost">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="15 3 21 3 21 9"></polyline>
                      <polyline points="9 21 3 21 3 15"></polyline>
                      <line x1="21" y1="3" x2="14" y2="10"></line>
                      <line x1="3" y1="21" x2="10" y2="14"></line>
                    </svg>
                    원본 보기
                  </button>
                  <button @click="downloadImage(selectedImage)" class="modal-action-btn primary">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                      <polyline points="7 10 12 15 17 10"></polyline>
                      <line x1="12" y1="15" x2="12" y2="3"></line>
                    </svg>
                    다운로드
                  </button>
                  <button @click="deleteImage(selectedImage)" class="modal-action-btn danger">
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
          <button v-if="hasNextImage" @click.stop="goToNextImage" class="nav-btn nav-next">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useGalleryStore } from "../stores/galleryStore";
import { imageService } from "../services/imageService";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { dreamResultService } from "../services/dreamResultService";

const router = useRouter();
const galleryStore = useGalleryStore();
const { galleryImages } = storeToRefs(galleryStore);
const dreamEntriesStore = useDreamEntriesStore();
const { posts } = storeToRefs(dreamEntriesStore);

const searchQuery = ref("");
const activeFilter = ref("all");
const viewMode = ref("grid");
const selectedImage = ref(null);
const syncing = ref(false);

const filters = [
  { id: "all", label: "전체", emoji: "🎨" },
  { id: "recent", label: "최근", emoji: "🕐" },
  { id: "liked", label: "좋아요", emoji: "❤️" },
  { id: "dreamy", label: "몽환적", emoji: "🌙" },
];

// 컴포넌트 마운트 시 갤러리 데이터 새로고침
onMounted(async () => {
  galleryStore.hydrateFromLocalStorage();
  await syncFromServer();
  // 키보드 네비게이션 이벤트 추가
  window.addEventListener("keydown", handleKeydown);
});

// 언마운트 시 이벤트 제거
onUnmounted(() => {
  window.removeEventListener("keydown", handleKeydown);
});

// 필터링된 이미지
const filteredImages = computed(() => {
  let result = galleryImages.value;

  // 검색 필터
  if (searchQuery.value) {
    result = result.filter((img) => img.caption.toLowerCase().includes(searchQuery.value.toLowerCase()) || img.style.toLowerCase().includes(searchQuery.value.toLowerCase()));
  }

  // 카테고리 필터
  if (activeFilter.value === "recent") {
    result = [...result].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } else if (activeFilter.value === "liked") {
    result = result.filter((img) => img.liked);
  } else if (activeFilter.value === "dreamy") {
    result = result.filter((img) => img.style === "몽환적");
  }

  return result;
});

const totalLikes = computed(() => {
  return galleryImages.value.reduce((sum, img) => sum + (img.likes || 0), 0);
});

// 이미지 경로를 실제 접근 가능한 URL로 정규화
function resolveImageSrc(src) {
  if (!src) return "";
  if (src.startsWith("http://") || src.startsWith("https://")) return src;
  if (src.startsWith("/")) return src;
  // 슬래시가 없는 상대경로로 온 경우 /uploads/... 형태로 접근할 수 있게 보정
  return `/${src}`;
}

function handleBack() {
  router.push({ name: "calendar" });
}

function goToVisualization() {
  router.push({ name: "visualization" });
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

function toggleLike(image) {
  galleryStore.toggleImageLike(image.id);
}

function shareImage(image) {
  alert("이미지 공유 기능은 준비 중입니다!");
}

async function deleteImage(image) {
  if (!confirm("정말 이 이미지를 삭제하시겠습니까?")) {
    return;
  }

  try {
    // 서버에 저장된 이미지인 경우 백엔드에서도 삭제 (절대/상대 경로 모두 허용)
    if (image.imageSrc && image.imageSrc.includes("/uploads/images/")) {
      try {
        await imageService.deleteImage(image.imageSrc);
        console.log("✅ 서버 이미지 삭제 완료");
      } catch (err) {
        console.warn("⚠️ 서버 이미지 삭제 실패 (로컬만 삭제):", err.message);
      }
    }

    // 갤러리에서 제거
    galleryStore.removeFromGallery(image.id);

    // 모달이 열려있으면 닫기
    if (selectedImage.value?.id === image.id) {
      selectedImage.value = null;
    }

    console.log("🗑️ 이미지 삭제 완료");
  } catch (error) {
    console.error("삭제 실패:", error);
    alert("삭제 중 오류가 발생했습니다.");
  }
}

function downloadImage(image) {
  if (!image.imageSrc) {
    alert("다운로드할 수 있는 이미지가 없습니다.");
    return;
  }

  try {
    const link = document.createElement("a");
    link.href = image.imageSrc;

    // 파일명 생성
    const timestamp = new Date().toISOString().slice(0, 10);
    const ext = image.mimeType?.split("/")[1] || "png";
    link.download = `dream_${timestamp}_${image.style || "image"}.${ext}`;

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error("다운로드 실패:", error);
    alert("다운로드 중 오류가 발생했습니다.");
  }
}

function openImageDetail(image) {
  selectedImage.value = image;
  document.body.style.overflow = "hidden";
}

function closeImageDetail() {
  selectedImage.value = null;
  document.body.style.overflow = "";
}

// 현재 이미지 인덱스
const currentImageIndex = computed(() => {
  if (!selectedImage.value) return -1;
  return filteredImages.value.findIndex((img) => img.id === selectedImage.value.id);
});

// 이전/다음 이미지 존재 여부
const hasPrevImage = computed(() => currentImageIndex.value > 0);
const hasNextImage = computed(() => currentImageIndex.value < filteredImages.value.length - 1);

// 이전 이미지로 이동
function goToPrevImage() {
  if (hasPrevImage.value) {
    selectedImage.value = filteredImages.value[currentImageIndex.value - 1];
  }
}

// 다음 이미지로 이동
function goToNextImage() {
  if (hasNextImage.value) {
    selectedImage.value = filteredImages.value[currentImageIndex.value + 1];
  }
}

// 키보드 네비게이션
function handleKeydown(e) {
  if (!selectedImage.value) return;
  if (e.key === "ArrowLeft") goToPrevImage();
  if (e.key === "ArrowRight") goToNextImage();
  if (e.key === "Escape") closeImageDetail();
}

function openOriginalImage(image) {
  if (!image?.imageSrc) return;
  window.open(image.imageSrc, "_blank");
}

// 서버에 저장된 꿈/이미지로 갤러리 채우기 (최대 최근 6개월)
async function syncFromServer() {
  if (syncing.value) return;
  syncing.value = true;
  try {
    const now = new Date();
    const seenDreamIds = new Set(galleryImages.value.map((img) => img.dreamId).filter(Boolean));

    // 최근 6개월 꿈 데이터 불러오기
    for (let i = 0; i < 6; i++) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const year = d.getFullYear();
      const month = d.getMonth() + 1; // 1-12
      try {
        await dreamEntriesStore.fetchDreamsByMonth(year, month);
      } catch (e) {
        console.warn("월별 꿈 불러오기 실패:", year, month, e?.message);
      }
    }

    // posts에 있는 dreamId로 결과 조회 후 갤러리에 채우기
    const entries = Object.entries(posts.value || {});
    for (const [dateKey, entry] of entries) {
      if (!entry?.dreamId || seenDreamIds.has(entry.dreamId)) continue;
      try {
        const result = await dreamEntriesStore.fetchDreamResult(entry.dreamId);
        if (result?.imageUrl) {
          galleryStore.addToGallery({
            id: result.id ?? entry.dreamId,
            dreamId: entry.dreamId,
            dreamDate: dateKey,
            title: entry.title,
            content: entry.content,
            interpretation: result.dreamInterpretation,
            fortuneSummary: result.todayFortuneSummary,
            luckyColor: result.luckyColor,
            luckyItem: result.luckyItem,
            style: result.imageStyle || "꿈 이미지",
            caption: entry.title || "꿈 이미지",
            imageSrc: result.imageUrl,
            mimeType: "image/png",
            createdAt: result.createdAt || entry.createdAt || new Date().toISOString(),
            savedAt: new Date().toISOString(),
          });
          seenDreamIds.add(entry.dreamId);
        }
      } catch (e) {
        console.warn("꿈 결과 불러오기 실패:", entry.dreamId, e?.message);
      }
    }
  } finally {
    syncing.value = false;
  }
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;600;700&display=swap");

.gallery-card {
  background: white;
  border-radius: 40px;
  padding: 2rem;
  width: 100%;
  max-width: 1200px;
  box-shadow: 0 20px 60px rgba(100, 100, 200, 0.15);
  font-family: "Nunito", sans-serif;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.back-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #888;
  padding: 5px;
  transition: color 0.2s;
}

.back-btn:hover {
  color: #333;
}

.page-title {
  font-family: "Dongle", sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin: 0;
  line-height: 1;
}

.spacer {
  width: 34px;
}

.gallery-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  background: #f8faff;
  border: 2px solid #e8f0fe;
  border-radius: 20px;
  transition: border-color 0.3s;
}

.search-box:focus-within {
  border-color: #a2d2ff;
}

.search-box svg {
  color: #999;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 1rem;
  font-family: "Nunito", sans-serif;
  outline: none;
  color: #333;
}

.filter-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: 2px solid #e8f0fe;
  background: white;
  border-radius: 15px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #a2d2ff;
  background: #f8f9ff;
}

.filter-btn.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.gallery-stats {
  display: flex;
  gap: 2rem;
  padding: 1rem;
  background: #f8faff;
  border-radius: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  color: #666;
}

.stat-icon {
  font-size: 1.5rem;
}

.view-mode-selector {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.view-mode-btn {
  padding: 0.75rem;
  border: 2px solid #e8f0fe;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}

.view-mode-btn:hover {
  border-color: #a2d2ff;
  background: #f8f9ff;
}

.view-mode-btn.active {
  border-color: #667eea;
  background: #667eea;
  color: white;
}

.gallery-grid {
  display: grid;
  gap: 2rem;
}

.gallery-grid.grid {
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.gallery-grid.masonry {
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.gallery-item {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  cursor: pointer;
}

.gallery-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
}

.image-container {
  width: 100%;
  aspect-ratio: 1 / 1;
  position: relative;
  overflow: hidden;
}

.image-container.real-image {
  background: linear-gradient(135deg, #f3e8ff, #e8f4ff);
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f0f23 100%);
  transition: transform 0.3s ease;
}

.gallery-item:hover .gallery-image {
  transform: scale(1.02);
}

.image-hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(102, 126, 234, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.gallery-item:hover .image-hover-overlay {
  opacity: 1;
}

.hover-icon {
  font-size: 2.5rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.image-overlay {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-emoji {
  font-size: 4rem;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.image-info {
  padding: 1.5rem;
}

.image-title {
  margin: 0 0 0.75rem 0;
  font-size: 1.1rem;
  color: #333;
  font-weight: 700;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.image-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
  font-size: 0.85rem;
  color: #999;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.image-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 1rem;
  border: 2px solid #e8f0fe;
  background: white;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: #a2d2ff;
  background: #f8f9ff;
}

.action-btn.liked {
  border-color: #ff8a80;
  color: #ff8a80;
}

.action-btn.delete:hover {
  border-color: #ff8a80;
  color: #ff8a80;
}

.empty-gallery {
  text-align: center;
  padding: 5rem 2rem;
  color: #999;
}

.empty-emoji {
  font-size: 5rem;
  display: block;
  margin-bottom: 1rem;
}

.empty-gallery h3 {
  color: #333;
  margin-bottom: 0.5rem;
}

.create-btn {
  margin-top: 2rem;
  padding: 1rem 2rem;
  border: none;
  border-radius: 20px;
  font-size: 1.1rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #667eea, #764ba2);
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px rgba(102, 126, 234, 0.4);
}

/* 갤러리 아이템 메타 뱃지 */
.meta-item.date-badge {
  color: #667eea;
  font-weight: 600;
}

.meta-item.style-badge {
  background: linear-gradient(135deg, #c77dff, #6fa7ff);
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
}

/* ===== 모달 스타일 - 투명 배경 ===== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 1.5rem;
  gap: 1rem;
}

/* 네비게이션 버튼 (이전/다음) */
.nav-btn {
  position: relative;
  z-index: 10;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.nav-btn:hover {
  background: white;
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.2);
}

.nav-btn:active {
  transform: scale(0.95);
}

.modal-content {
  background: transparent;
  border-radius: 0;
  max-width: 1100px;
  width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  flex-shrink: 1;
}

.modal-close {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 44px;
  height: 44px;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.3s;
  z-index: 10;
}

.modal-close:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

/* 모달 레이아웃 */
.modal-layout {
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  gap: 0;
  border-radius: 20px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
}

/* 왼쪽: 미술관 벽 & 액자 섹션 */
.modal-image-section {
  flex: 0 0 55%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2.5rem 2rem;
  position: relative;
  min-width: 0;
  /* 미술관 벽면 효과 */
  background: linear-gradient(180deg, #2d2d3d 0%, #252535 30%, #1e1e2c 70%, #1a1a28 100%);
  border-radius: 20px 0 0 20px;
}

/* 미술관 스포트라이트 효과 (위에서 비추는 빛) */
.modal-image-section::before {
  content: "";
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 70%;
  height: 60%;
  background: radial-gradient(ellipse at center top, rgba(255, 250, 230, 0.15) 0%, rgba(255, 250, 230, 0.06) 35%, transparent 65%);
  pointer-events: none;
}

/* 바닥 반사광 */
.modal-image-section::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 25%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.25) 0%, transparent 100%);
  pointer-events: none;
  border-radius: 0 0 0 20px;
}

/* 액자 컨테이너 */
.frame-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
}

/* 고급 목재 액자 */
.frame-outer {
  background: linear-gradient(145deg, #c9a66b 0%, #9a7b3c 8%, #6b5a2a 20%, #4a3f20 50%, #6b5a2a 80%, #9a7b3c 92%, #c9a66b 100%);
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 30px 70px rgba(0, 0, 0, 0.7), 0 15px 35px rgba(0, 0, 0, 0.5), inset 0 2px 3px rgba(255, 255, 255, 0.35), inset 0 -2px 3px rgba(0, 0, 0, 0.4), inset 3px 0 4px rgba(0, 0, 0, 0.25),
    inset -3px 0 4px rgba(0, 0, 0, 0.25);
}

/* 액자 내부 금장 테두리 */
.frame-inner {
  background: linear-gradient(145deg, #d4af37 0%, #c4a030 25%, #a8892a 50%, #c4a030 75%, #d4af37 100%);
  padding: 6px;
  border-radius: 2px;
  box-shadow: inset 0 1px 2px rgba(255, 255, 255, 0.5), inset 0 -1px 2px rgba(0, 0, 0, 0.35), 0 0 12px rgba(212, 175, 55, 0.25);
}

.framed-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #0c0c12, #14141c);
  border-radius: 1px;
  overflow: hidden;
  box-shadow: inset 0 0 40px rgba(0, 0, 0, 0.6);
}

.framed-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.framed-placeholder {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 1px;
  background: linear-gradient(145deg, #0c0c12, #14141c);
}

.placeholder-emoji {
  font-size: 5rem;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.5));
}

/* 액자 바닥 그림자 */
.frame-shadow {
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  height: 30px;
  background: radial-gradient(ellipse, rgba(0, 0, 0, 0.5), transparent 70%);
  z-index: 0;
}

/* 작품 정보 플레이트 (미술관 스타일) */
.style-label {
  margin-top: 1.5rem;
  padding: 0.65rem 1.6rem;
  background: linear-gradient(145deg, #28282f, #1c1c22);
  border: 1px solid rgba(212, 175, 55, 0.35);
  border-radius: 3px;
  color: rgba(255, 255, 255, 0.92);
  font-size: 0.85rem;
  font-weight: 500;
  letter-spacing: 0.06em;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4), inset 0 1px 1px rgba(255, 255, 255, 0.08);
}

.style-icon {
  font-size: 0.9rem;
}

/* 오른쪽: 정보 섹션 - 깔끔한 카드 스타일 */
.modal-info-section {
  flex: 0 0 45%;
  display: flex;
  flex-direction: column;
  padding: 2rem;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  background: white;
  border-radius: 0 20px 20px 0;
}

.modal-header-info {
  padding-bottom: 1.25rem;
  border-bottom: 2px solid rgba(102, 78, 50, 0.12);
  margin-bottom: 1.25rem;
  flex-shrink: 0;
}

.modal-header-info h2 {
  margin: 0 0 0.85rem 0;
  font-family: "Playfair Display", serif;
  font-size: 1.6rem;
  font-weight: 600;
  color: #2a2520;
  line-height: 1.35;
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
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.meta-badge.date {
  background: linear-gradient(135deg, #f0ebe4, #e8e2d9);
  color: #6b5a4a;
  border: 1px solid rgba(107, 90, 74, 0.15);
}

.badge-icon {
  font-size: 0.9rem;
}

/* 스크롤 가능 영역 */
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
  background: rgba(102, 126, 234, 0.25);
  border-radius: 5px;
}

.modal-scrollable::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.4);
}

/* 모달 섹션 - 가독성 개선 */
.modal-section {
  margin-bottom: 0.85rem;
  padding: 1rem 1.1rem;
  border-radius: 14px;
  transition: transform 0.2s;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.9rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 0.6rem 0;
}

.section-icon {
  font-size: 1rem;
}

/* 꿈 일기 내용 섹션 */
.dream-content-section {
  background: linear-gradient(135deg, #faf5ff, #f3e8ff);
  border-left: 3px solid #a855f7;
}

.dream-content-text {
  margin: 0;
  color: #444;
  line-height: 1.75;
  font-size: 0.9rem;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 꿈 해석 섹션 */
.interpretation-section {
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  border-left: 3px solid #0ea5e9;
}

.interpretation-text {
  margin: 0;
  color: #0c4a6e;
  line-height: 1.75;
  font-size: 0.9rem;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 오늘의 운세 섹션 */
.fortune-section {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border-left: 3px solid #f59e0b;
}

.fortune-text {
  margin: 0;
  color: #78350f;
  line-height: 1.75;
  font-size: 0.9rem;
  word-break: break-word;
}

/* 행운 정보 섹션 */
.lucky-section {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border-left: 3px solid #22c55e;
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
  color: #16a34a;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.lucky-value {
  font-size: 0.95rem;
  font-weight: 700;
  color: #166534;
}

.lucky-value.color-value {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.color-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 액션 버튼 - 컴팩트하게 */
.modal-actions {
  display: flex;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
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
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-action-btn.ghost {
  background: white;
  color: #4a5568;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.modal-action-btn.ghost:hover {
  background: #f8f9fa;
  border-color: rgba(0, 0, 0, 0.15);
}

.modal-action-btn.primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.25);
}

.modal-action-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35);
}

.modal-action-btn.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  box-shadow: 0 3px 12px rgba(239, 68, 68, 0.25);
}

.modal-action-btn.danger:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(239, 68, 68, 0.35);
}

/* 모달 애니메이션 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.3s ease;
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
  transition: transform 0.3s ease;
}

/* ===== 반응형 - 태블릿 ===== */
@media (max-width: 950px) {
  .modal-overlay {
    padding: 1rem;
    flex-direction: row;
  }

  .nav-btn {
    width: 48px;
    height: 48px;
  }

  .modal-layout {
    flex-direction: column;
    overflow-y: auto;
    border-radius: 20px;
  }

  .modal-image-section {
    flex: none;
    padding: 2rem 1.5rem;
    border-radius: 20px 20px 0 0;
  }

  .modal-image-section::before {
    width: 60%;
    height: 50%;
  }

  .modal-image-section::after {
    border-radius: 0;
  }

  .frame-container {
    max-width: 300px;
  }

  .modal-info-section {
    flex: none;
    padding: 1.5rem;
    max-height: none;
    overflow: visible;
    border-radius: 0 0 20px 20px;
  }

  .modal-scrollable {
    overflow: visible;
    max-height: none;
  }

  .modal-header-info h2 {
    font-size: 1.4rem;
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
    width: 22px;
    height: 22px;
  }

  .modal-content {
    max-height: 90vh;
  }

  .modal-close {
    top: 0.5rem;
    right: 0.5rem;
    width: 36px;
    height: 36px;
  }

  .modal-layout {
    flex-direction: column;
    overflow-y: auto;
    max-height: calc(90vh - 20px);
    border-radius: 16px;
  }

  /* 모바일: 이미지 섹션 - 미술관 느낌 유지 */
  .modal-image-section {
    padding: 1.5rem 1rem;
    flex-shrink: 0;
    border-radius: 16px 16px 0 0;
  }

  .modal-image-section::before {
    width: 80%;
    height: 45%;
  }

  .modal-image-section::after {
    border-radius: 0;
  }

  .frame-container {
    max-width: 220px;
  }

  .frame-outer {
    padding: 10px;
  }

  .frame-inner {
    padding: 4px;
  }

  .placeholder-emoji {
    font-size: 3.5rem;
  }

  .frame-shadow {
    bottom: -18px;
    height: 18px;
  }

  .style-label {
    font-size: 0.75rem;
    padding: 0.45rem 1rem;
    margin-top: 1rem;
  }

  /* 모바일: 정보 섹션 - 가독성 최적화 */
  .modal-info-section {
    padding: 1.25rem;
    border-radius: 0 0 16px 16px;
    background: white;
  }

  .modal-header-info {
    padding-bottom: 0.85rem;
    margin-bottom: 0.85rem;
  }

  .modal-header-info h2 {
    font-size: 1.15rem;
    margin-bottom: 0.6rem;
    line-height: 1.4;
  }

  .meta-badge {
    padding: 0.35rem 0.7rem;
    font-size: 0.8rem;
  }

  .badge-icon {
    font-size: 0.85rem;
  }

  /* 모바일: 섹션 패딩 최적화 */
  .modal-section {
    padding: 0.9rem 1rem;
    margin-bottom: 0.75rem;
    border-radius: 12px;
  }

  .section-title {
    font-size: 0.85rem;
    margin-bottom: 0.5rem;
  }

  .section-icon {
    font-size: 0.9rem;
  }

  .dream-content-text,
  .interpretation-text,
  .fortune-text {
    font-size: 0.85rem;
    line-height: 1.7;
  }

  /* 모바일: 행운 정보 가로 배치 */
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

  /* 모바일: 액션 버튼 가로 유지, 컴팩트 */
  .modal-actions {
    gap: 0.6rem;
    padding-top: 0.85rem;
    flex-wrap: wrap;
  }

  .modal-action-btn {
    padding: 0.75rem 0.85rem;
    font-size: 0.8rem;
    gap: 0.3rem;
    border-radius: 10px;
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
    padding: 8px;
  }

  .frame-inner {
    padding: 3px;
  }

  .modal-info-section {
    padding: 1rem;
  }

  .modal-header-info h2 {
    font-size: 1rem;
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

/* ===== 갤러리 카드 반응형 ===== */
@media (max-width: 480px) {
  .gallery-card {
    padding: 1.5rem;
    border-radius: 30px;
  }

  .page-title {
    font-size: 2rem;
  }

  .gallery-grid {
    gap: 1.5rem;
  }

  .gallery-grid.grid {
    grid-template-columns: 1fr;
  }

  .filter-buttons {
    gap: 0.5rem;
  }

  .filter-btn {
    padding: 0.6rem 1rem;
    font-size: 0.9rem;
  }
}
</style>
