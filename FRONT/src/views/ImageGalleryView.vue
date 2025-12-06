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
        <div v-for="image in filteredImages" :key="image.id" :class="['gallery-item', { tall: viewMode === 'masonry' && image.id % 3 === 0 }]" @click="openImageDetail(image)">
          <!-- 실제 이미지가 있는 경우 -->
          <div v-if="image.imageSrc" class="image-container real-image">
            <img :src="image.imageSrc" :alt="image.caption" class="gallery-image" />
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
    <div v-if="selectedImage" class="modal-overlay" @click="closeImageDetail">
      <div class="modal-content" @click.stop>
        <button @click="closeImageDetail" class="modal-close">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
        <!-- 실제 이미지가 있는 경우 -->
        <div v-if="selectedImage.imageSrc" class="modal-image real-image">
          <img :src="selectedImage.imageSrc" :alt="selectedImage.caption" class="modal-actual-image" />
        </div>
        <!-- 기존 gradient/emoji 표시 -->
        <div v-else class="modal-image" :style="{ background: selectedImage.gradient }">
          <span class="modal-emoji">{{ selectedImage.emoji }}</span>
        </div>
        <div class="modal-info">
          <!-- 제목과 날짜 -->
          <div class="modal-header-info">
            <h2>{{ selectedImage.title || selectedImage.caption }}</h2>
            <div class="modal-meta">
              <span class="meta-badge date">📅 {{ formatDreamDate(selectedImage.dreamDate) || formatDate(selectedImage.createdAt) }}</span>
              <span class="meta-badge style">🎨 {{ selectedImage.style }}</span>
            </div>
          </div>

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

          <!-- 액션 버튼 -->
          <div class="modal-actions">
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
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useGalleryStore } from "../stores/galleryStore";
import { imageService } from "../services/imageService";

const router = useRouter();
const galleryStore = useGalleryStore();
const { galleryImages } = storeToRefs(galleryStore);

const searchQuery = ref("");
const activeFilter = ref("all");
const viewMode = ref("grid");
const selectedImage = ref(null);

const filters = [
  { id: "all", label: "전체", emoji: "🎨" },
  { id: "recent", label: "최근", emoji: "🕐" },
  { id: "liked", label: "좋아요", emoji: "❤️" },
  { id: "dreamy", label: "몽환적", emoji: "🌙" },
];

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
    // 서버에 저장된 이미지인 경우 백엔드에서도 삭제
    if (image.imageSrc && image.imageSrc.startsWith("/uploads/")) {
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
}

function closeImageDetail() {
  selectedImage.value = null;
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800&display=swap");
@import url("https://fonts.googleapis.com/css2?family=Dongle:wght@300;400;700&display=swap");

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
  grid-auto-rows: 250px;
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

.gallery-item.tall {
  grid-row: span 2;
}

.image-container {
  width: 100%;
  height: 200px;
  position: relative;
  overflow: hidden;
}

.image-container.real-image {
  background: linear-gradient(135deg, #f3e8ff, #e8f4ff);
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.gallery-item:hover .gallery-image {
  transform: scale(1.05);
}

.gallery-item.tall .image-container {
  height: 100%;
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

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 2rem;
}

.modal-content {
  background: white;
  border-radius: 30px;
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow: auto;
  position: relative;
}

.modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  transition: all 0.2s;
  z-index: 1;
}

.modal-close:hover {
  background: white;
  transform: scale(1.1);
}

.modal-image {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 30px 30px 0 0;
  overflow: hidden;
}

.modal-image.real-image {
  background: linear-gradient(135deg, #f3e8ff, #e8f4ff);
}

.modal-actual-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.modal-emoji {
  font-size: 8rem;
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.2));
}

.modal-info {
  padding: 2rem;
}

.modal-info h2 {
  margin: 0 0 1rem 0;
  color: #333;
}

.modal-meta {
  display: flex;
  gap: 2rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: #999;
}

.modal-prompt {
  padding: 1rem;
  background: #f8faff;
  border-radius: 15px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
}

.modal-action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 2rem;
  border: 2px solid #e8f0fe;
  background: white;
  border-radius: 15px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-action-btn:hover {
  border-color: #a2d2ff;
  background: #f8f9ff;
  transform: translateY(-2px);
}

.modal-action-btn.primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
}

.modal-action-btn.primary:hover {
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.modal-action-btn.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  border: none;
}

.modal-action-btn.danger:hover {
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.3);
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

/* 모달 헤더 정보 */
.modal-header-info {
  margin-bottom: 1.5rem;
}

.modal-header-info h2 {
  margin: 0 0 0.75rem 0;
  color: #333;
  font-size: 1.5rem;
}

.modal-meta {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.meta-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.meta-badge.date {
  background: #f0f4ff;
  color: #667eea;
}

.meta-badge.style {
  background: linear-gradient(135deg, #c77dff20, #6fa7ff20);
  color: #764ba2;
}

/* 모달 섹션 */
.modal-section {
  margin-bottom: 1.5rem;
  padding: 1rem 1.25rem;
  border-radius: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 0.75rem 0;
}

.section-icon {
  font-size: 1.1rem;
}

/* 꿈 일기 내용 섹션 */
.dream-content-section {
  background: linear-gradient(135deg, #faf5ff, #f3e8ff);
  border-left: 4px solid #a855f7;
}

.dream-content-text {
  margin: 0;
  color: #555;
  line-height: 1.7;
  font-size: 0.95rem;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 꿈 해석 섹션 */
.interpretation-section {
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  border-left: 4px solid #0ea5e9;
}

.interpretation-text {
  margin: 0;
  color: #0c4a6e;
  line-height: 1.7;
  font-size: 0.95rem;
  white-space: pre-wrap;
}

/* 오늘의 운세 섹션 */
.fortune-section {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border-left: 4px solid #f59e0b;
}

.fortune-text {
  margin: 0;
  color: #78350f;
  line-height: 1.7;
  font-size: 0.95rem;
}

/* 행운 정보 섹션 */
.lucky-section {
  background: #f8faff;
  padding: 1rem;
}

.lucky-items {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.lucky-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.lucky-label {
  font-size: 0.75rem;
  color: #888;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.lucky-value {
  font-size: 1rem;
  font-weight: 700;
  color: #333;
}

.lucky-value.color-value {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.color-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(0, 0, 0, 0.1);
}

/* 모달 내용 스크롤 */
.modal-info {
  padding: 1.5rem 2rem;
  max-height: calc(90vh - 400px);
  overflow-y: auto;
}

.modal-info::-webkit-scrollbar {
  width: 6px;
}

.modal-info::-webkit-scrollbar-track {
  background: transparent;
}

.modal-info::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 6px;
}

/* 반응형 */
@media (max-width: 768px) {
  .modal-content {
    max-width: 95%;
    margin: 1rem;
    max-height: 95vh;
  }

  .modal-image {
    height: 280px;
  }

  .modal-info {
    padding: 1.25rem;
    max-height: calc(95vh - 320px);
  }

  .modal-header-info h2 {
    font-size: 1.25rem;
  }

  .modal-section {
    padding: 0.875rem 1rem;
  }

  .section-title {
    font-size: 0.9rem;
  }

  .dream-content-text,
  .interpretation-text,
  .fortune-text {
    font-size: 0.9rem;
  }

  .lucky-items {
    gap: 1rem;
  }

  .modal-actions {
    flex-direction: column;
    gap: 0.5rem;
  }

  .modal-action-btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .modal-image {
    height: 220px;
  }

  .meta-badge {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
  }

  .modal-meta {
    gap: 0.5rem;
  }
}
</style>
