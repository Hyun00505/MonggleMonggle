<template>
  <div class="gallery-card">
    <div class="card-header">
      <button @click="handleBack" class="icon-btn">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
      </button>
      <h2 class="page-title">
        이미지 갤러리
        <span class="title-badge">Gallery</span>
      </h2>
      <div class="header-actions">
        <button @click="handleClose" class="icon-btn" aria-label="닫기">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
        </button>
      </div>
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
            <span class="filter-icon" v-html="filter.icon"></span>
            <span>{{ filter.label }}</span>
          </button>
          <!-- 갤러리 통계 -->
          <div class="gallery-stats">
            <div class="stat-item">
              <span class="stat-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                  <circle cx="8.5" cy="8.5" r="1.5"></circle>
                  <polyline points="21 15 16 10 5 21"></polyline>
                </svg>
              </span>
              <span class="stat-text">
                전체
                <strong>{{ filteredImages.length }}</strong>
                개
              </span>
            </div>
            <div class="stat-item">
              <span class="stat-icon star-yellow">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="#f9a825" stroke="#f9a825" stroke-width="1">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
              </span>
              <span class="stat-text">
                찜
                <strong>{{ totalLikes }}</strong>
                개
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 이미지 그리드 -->
      <div v-if="filteredImages.length > 0" class="gallery-grid">
        <div v-for="image in filteredImages" :key="image.id" class="gallery-item" @click="openImageDetail(image)">
          <!-- 실제 이미지가 있는 경우 -->
          <div v-if="image.imageSrc" class="image-container real-image">
            <img :src="resolveImageSrc(image.imageSrc)" :alt="image.caption" class="gallery-image" />
            <div class="image-hover-overlay">
              <span class="hover-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
              </span>
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
            </div>
            <div class="image-actions">
              <button @click.stop="toggleLike(image)" :class="['action-btn', { liked: image.liked }]" :aria-label="image.liked ? '찜 해제' : '찜하기'">
                <svg width="16" height="16" viewBox="0 0 24 24" :fill="image.liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
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
        <span class="empty-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 19l7-7 3 3-7 7-3-3z"></path>
            <path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z"></path>
            <path d="M2 2l7.586 7.586"></path>
            <circle cx="11" cy="11" r="2"></circle>
          </svg>
        </span>
        <h3>아직 생성된 이미지가 없습니다</h3>
        <p>캘린더에서 꿈을 기록하고 분석하면 생성된 이미지가 여기 보입니다.</p>
      </div>
    </div>

    <!-- 이미지 상세 모달 -->
    <ImageDetailModal
      :image="selectedImage"
      :has-prev="hasPrevImage"
      :has-next="hasNextImage"
      @close="closeImageDetail"
      @prev="goToPrevImage"
      @next="goToNextImage"
      @delete="deleteImage"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useGalleryStore } from "../stores/galleryStore";
import { imageService } from "../services/imageService";
import { dreamService } from "../services/dreamService";
import { dreamResultService } from "../services/dreamResultService";
import ImageDetailModal from "../components/image/ImageDetailModal.vue";
import { useConfirm } from "../composables/useConfirm";

const router = useRouter();
const galleryStore = useGalleryStore();
const { galleryImages } = storeToRefs(galleryStore);
const { confirm } = useConfirm();

const searchQuery = ref("");
const activeFilter = ref("all");
const selectedImage = ref(null);
const syncing = ref(false);

const filters = [
  { 
    id: "all", 
    label: "전체", 
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>`
  },
  { 
    id: "recent", 
    label: "최근", 
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>`
  },
  { 
    id: "liked", 
    label: "찜", 
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="#f9a825" stroke="#f9a825" stroke-width="1"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>`
  },
];

// 컴포넌트 마운트 시 갤러리 데이터 새로고침
onMounted(async () => {
  galleryStore.hydrateFromLocalStorage();
  await syncFromServer();
});

// 필터링된 이미지
const filteredImages = computed(() => {
  let result = galleryImages.value;

  // 검색 필터
  if (searchQuery.value) {
    result = result.filter((img) => img.caption.toLowerCase().includes(searchQuery.value.toLowerCase()));
  }

  // 카테고리 필터
  if (activeFilter.value === "recent") {
    result = [...result].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } else if (activeFilter.value === "liked") {
    result = result.filter((img) => img.liked);
  }

  return result;
});

const totalLikes = computed(() => {
  return galleryImages.value.filter((img) => img.liked).length;
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

function handleClose() {
  router.push({ name: "calendar" });
}

function formatDreamDate(dateKey) {
  if (!dateKey) return "";
  const [year, month, day] = dateKey.split("-");
  return `${month}월 ${day}일`;
}

function formatDate(dateString) {
  if (!dateString) return "";
  const date = new Date(dateString);
  return `${date.getMonth() + 1}월 ${date.getDate()}일`;
}

function toggleLike(image) {
  galleryStore.toggleImageLike(image.id);
}


async function deleteImage(image) {
  const confirmed = await confirm({
    title: '이미지 삭제',
    message: '정말 이 이미지를 삭제하시겠습니까?',
    type: 'danger',
    confirmText: '삭제'
  });
  if (!confirmed) return;

  try {
    // 1. 서버에 저장된 이미지인 경우 물리적 파일 삭제
    if (image.imageSrc && image.imageSrc.includes("/uploads/images/")) {
      try {
        await imageService.deleteImage(image.imageSrc);
      } catch (err) {
        console.warn("⚠️ 서버 이미지 파일 삭제 실패:", err.message);
      }
    }

    // 2. DB에서 dream_results의 image_url을 삭제 (새로고침해도 다시 안 뜨도록)
    if (image.dreamId) {
      try {
        // 빈 문자열을 보내면 백엔드에서 null로 처리
        await dreamResultService.updateDreamResult(image.dreamId, {
          imageUrl: "",
        });
      } catch (err) {
        console.warn("⚠️ DB 이미지 URL 삭제 실패:", err.message);
      }
    }

    // 3. 로컬 갤러리에서 제거
    galleryStore.removeFromGallery(image.id);

    // 모달이 열려있으면 닫기
    if (selectedImage.value?.id === image.id) {
      selectedImage.value = null;
    }
  } catch (error) {
    console.error("삭제 실패:", error);
    alert("삭제 중 오류가 발생했습니다.");
  }
}

function openImageDetail(image) {
  selectedImage.value = image;
}

function closeImageDetail() {
  selectedImage.value = null;
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

// 서버에 저장된 이미지가 있는 꿈을 한 번에 불러오기 (갤러리용)
async function syncFromServer() {
  if (syncing.value) return;
  syncing.value = true;

  try {
    // 백엔드에서 이미지가 있는 모든 꿈을 한 번에 조회
    const response = await dreamService.getDreamsWithImages();

    if (!response?.items) {
      // 서버에 이미지가 없으면 로컬 갤러리도 비우기
      galleryStore.resetGallery();
      return;
    }

    // 서버에서 받은 꿈 ID 목록 (이미지가 있는 꿈만)
    const serverDreamIds = new Set(response.items.map((item) => item.dreamId));

    // 로컬 갤러리에서 서버에 없는 항목 제거 (이미지가 삭제된 경우)
    const localDreamIds = galleryImages.value.map((img) => img.dreamId).filter(Boolean);
    for (const localDreamId of localDreamIds) {
      if (!serverDreamIds.has(localDreamId)) {
        // 서버에 없는 항목은 로컬에서도 삭제
        const imageToRemove = galleryImages.value.find((img) => img.dreamId === localDreamId);
        if (imageToRemove) {
          galleryStore.removeFromGallery(imageToRemove.id);
        }
      }
    }

    // 서버에서 받은 꿈들을 갤러리에 추가 또는 업데이트
    for (const item of response.items) {
      // 이미지가 있는 경우만 갤러리에 추가/업데이트
      if (item.imageUrl) {
        galleryStore.addToGallery({
          id: item.dreamId, // dreamId를 일관된 식별자로 사용하여 중복 방지
          dreamId: item.dreamId,
          dreamDate: item.dreamDate,
          title: item.title,
          content: item.content,
          interpretation: item.dreamInterpretation,
          fortuneSummary: item.todayFortuneSummary,
          luckyColor: item.luckyColor,
          luckyItem: item.luckyItem,
          style: "꿈 이미지",
          caption: item.title || "꿈 이미지",
          imageSrc: item.imageUrl,
          mimeType: "image/png",
          liked: item.isLiked ?? false, // 서버에서 받은 찜 상태 반영
          createdAt: item.createdDate || new Date().toISOString(),
          savedAt: new Date().toISOString(),
        });
      }
    }
  } catch (error) {
    console.error("갤러리 동기화 실패:", error);
  } finally {
    syncing.value = false;
  }
}
</script>

<style scoped>
/* ===== 몽글몽글 갤러리 스타일 ===== */

.gallery-card {
  background: white;
  border-radius: 40px;
  padding: 2rem;
  width: 100%;
  max-width: 1200px;
  box-shadow: 0 20px 60px rgba(100, 100, 200, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.page-title {
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
  -webkit-text-fill-color: #4c2b7b;
  line-height: 1.2;
}

.title-badge {
  font-size: 0.7rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: 0.3rem 0.8rem;
  border-radius: 999px;
  background: white;
  color: var(--color-purple-dark);
  font-weight: 600;
}

.header-actions {
  display: inline-flex;
  gap: 0.5rem;
  align-items: center;
}

.gallery-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* ===== 필터 섹션 ===== */
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
  background: var(--color-purple-light);
  border: 2px solid var(--border-purple);
  border-radius: 20px;
  transition: all 0.3s;
}

.search-box:focus-within {
  border-color: var(--color-purple);
  background: white;
  box-shadow: 0 0 0 4px var(--color-purple-light);
}

.search-box svg {
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 1rem;
  outline: none;
  color: #333;
}

.search-input::placeholder {
  color: #bbb;
}

.filter-buttons {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: calc(0.7rem + 2px) calc(1.25rem + 2px);
  border: none;
  background: white;
  border-radius: 999px;
  font-weight: 700;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
  box-shadow: inset 0 0 0 2px var(--border-purple);
}

.filter-btn:hover {
  box-shadow: inset 0 0 0 2px var(--color-purple);
  background: var(--color-purple-light);
}

.filter-btn.active {
  background: var(--gradient-purple-blue);
  color: white;
  box-shadow: 0 4px 15px var(--shadow-purple);
}

.filter-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.filter-icon svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

/* ===== 통계 & 뷰 모드 ===== */
.gallery-stats {
  display: flex;
  gap: 2rem;
  padding: 0.5rem 1.25rem;
  background: var(--gradient-bg-light);
  border-radius: 16px;
  border: 1px solid var(--border-purple);
  font-family: "Dongle", sans-serif;
  margin-left: auto;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.6rem;
  color: var(--color-text-secondary);
  font-weight: 600;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--color-purple);
}

.stat-icon svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.stat-item strong {
  color: var(--color-text-primary);
}

/* ===== 갤러리 그리드 ===== */
.gallery-grid {
  display: grid;
  gap: 1.5rem;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.gallery-item {
  background: white;
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid var(--border-purple);
  box-shadow: 0 8px 24px var(--shadow-purple);
  transition: all 0.3s;
  cursor: pointer;
}

.gallery-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px var(--shadow-purple-md);
  border-color: var(--color-purple);
}

.image-container {
  width: 100%;
  aspect-ratio: 1 / 1;
  position: relative;
  overflow: hidden;
}

.image-container.real-image {
  background: linear-gradient(135deg, var(--color-purple-15), var(--color-blue-15));
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: linear-gradient(135deg, #454278 0%, #7C78B8 50%, #C4B6DC 100%);
  transition: transform 0.3s ease;
}

.gallery-item:hover .gallery-image {
  transform: scale(1.03);
}

.image-hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(205, 180, 219, 0.4), rgba(162, 210, 255, 0.4));
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
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.3));
}

.hover-icon svg {
  width: 40px;
  height: 40px;
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
  padding: 1.25rem;
}

.image-title {
  margin: 0 0 0.6rem 0;
  font-size: 1.05rem;
  color: var(--color-text-primary);
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
  gap: 0.6rem;
  margin-bottom: 0.85rem;
  font-size: 0.8rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.meta-item.date-badge {
  color: var(--color-purple);
  font-weight: 600;
  font-family: 'Dongle';
  font-size: 1rem;
}


.image-actions {
  display: flex;
  gap: 0.4rem;
  justify-content:flex-end;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  padding: 0.5rem;
  border: 1px solid var(--border-purple);
  background: white;
  border-radius: 10px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.action-btn:hover {
  border-color: var(--color-purple);
  background: var(--color-purple-light);
  color: var(--color-purple);
}

.action-btn.liked {
  border-color: #ffc107;
  color: #f9a825;
  background: rgba(255, 193, 7, 0.15);
}

.action-btn.delete:hover {
  border-color: #ff8a80;
  color: #ff5252;
  background: rgba(255, 138, 128, 0.1);
}

/* ===== 빈 상태 ===== */
.empty-gallery {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--color-text-muted);
}

.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
  color: var(--color-purple);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.empty-gallery h3 {
  color: var(--color-text-primary);
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
}

.empty-gallery p {
  max-width: 300px;
  margin: 0 auto;
  line-height: 1.6;
}

/* ===== 갤러리 카드 반응형 ===== */
@media (max-width: 768px) {
  .gallery-card {
    border-radius: 24px;
    padding: 1.25rem 1.5rem 1.5rem;
  }

  .page-title {
    font-size: 1.8rem;
    padding: 0.4rem 1rem;
  }

  .title-badge {
    display: none;
  }
}

@media (max-width: 480px) {
  .gallery-card {
    padding: 1.25rem;
    border-radius: 24px;
  }

  .page-title {
    font-size: 1.6rem;
    padding: 0.35rem 0.85rem;
  }

  .gallery-content {
    gap: 1.25rem;
  }

  .gallery-grid {
    gap: 1.25rem;
  }

  .gallery-grid.grid {
    grid-template-columns: 1fr;
  }

  .filter-buttons {
    gap: 0.5rem;
  }

  .filter-btn {
    padding: 0.55rem 1rem;
    font-size: 0.85rem;
  }

  .gallery-stats {
    gap: 1rem;
    padding: 0.85rem 1rem;
  }

  .stat-item {
    font-size: 0.9rem;
  }
}
</style>
