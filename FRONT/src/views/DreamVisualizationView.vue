<template>
  <div class="visualization-card">
    <div class="card-header">
      <button @click="handleBack" class="back-btn">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
      </button>
      <h2 class="page-title">꿈 시각화</h2>
      <div class="spacer"></div>
    </div>

    <div class="visualization-content">
      <!-- 꿈 선택 섹션 -->
      <div class="dream-select-section">
        <label>시각화할 꿈 선택</label>
        <select v-model="selectedDreamKey" class="dream-selector" @change="loadDreamContent">
          <option value="">꿈을 선택하세요</option>
          <option v-for="(dream, key) in sortedDreams" :key="key" :value="key">{{ formatDateDisplay(key) }} | {{ truncateTitle(dream.title) }}</option>
        </select>
      </div>

      <!-- 선택된 꿈 미리보기 -->
      <div v-if="selectedDream" class="dream-preview">
        <div class="preview-header">
          <span class="preview-date">{{ formatDateDisplay(selectedDreamKey) }}</span>
          <h3 class="preview-title">{{ selectedDream.title }}</h3>
        </div>
        <div class="preview-content-wrapper">
          <p class="preview-content">{{ selectedDream.content }}</p>
        </div>
      </div>

      <!-- 프롬프트 입력 -->
      <div class="prompt-section">
        <label>이미지 스타일 선택</label>
        <div class="style-grid">
          <button v-for="style in imageStyles" :key="style.id" :class="['style-btn', { active: selectedStyle === style.id }]" @click="selectedStyle = style.id">
            <span class="style-emoji">{{ style.emoji }}</span>
            <span class="style-name">{{ style.name }}</span>
          </button>
        </div>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="generateError" class="generate-error">
        <span class="error-icon">⚠️</span>
        <p>{{ generateError }}</p>
      </div>

      <!-- 생성 버튼 -->
      <button class="generate-btn" :disabled="!selectedDreamKey || isGenerating" @click="generateImage">
        <span v-if="!isGenerating">✨ 이미지 생성하기</span>
        <span v-else>
          <span class="spinner"></span>
          생성 중...
        </span>
      </button>

      <!-- 생성된 이미지 -->
      <div v-if="generatedImages.length > 0" class="generated-images">
        <h3>생성된 이미지</h3>
        <div class="image-grid">
          <div v-for="(img, index) in generatedImages" :key="img.id || index" class="image-card">
            <!-- 실제 이미지 -->
            <div v-if="img.imageSrc" class="image-placeholder real-image">
              <img :src="img.imageSrc" :alt="img.caption" class="generated-image" />
            </div>
            <!-- 폴백 (그라데이션 + 이모지) -->
            <div v-else class="image-placeholder" :style="{ background: img.gradient }">
              <div class="image-content">
                <span class="image-emoji">{{ img.emoji }}</span>
                <p class="image-caption">{{ img.caption }}</p>
              </div>
            </div>
            <div class="image-info">
              <p class="image-caption-text">{{ img.caption }}</p>
              <span class="image-style-badge">{{ img.style }}</span>
            </div>
            <div class="image-actions">
              <span class="auto-saved-badge">✅ 자동 저장됨</span>
              <button @click="downloadImage(img)" class="action-icon-btn download">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="7 10 12 15 17 10"></polyline>
                  <line x1="12" y1="15" x2="12" y2="3"></line>
                </svg>
                <span>다운로드</span>
              </button>
            </div>
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
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { useGalleryStore } from "../stores/galleryStore";
import { fortuneService } from "../services/fortuneService";
import { imageService } from "../services/imageService";
import { dreamResultService } from "../services/dreamResultService";

const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const { postedDates } = storeToRefs(dreamEntriesStore);
const galleryStore = useGalleryStore();

const selectedDreamKey = ref("");
const selectedDream = ref(null);
const selectedDreamResult = ref(null); // 해몽 결과 저장
const selectedStyle = ref("dreamy");
const isGenerating = ref(false);
const generateError = ref(null);
const generatedImages = ref([]);

// 이미지 스타일 (프론트엔드 ID -> AI API 스타일명 매핑)
const imageStyles = [
  { id: "dreamy", name: "몽환적", emoji: "🌙", apiStyle: "몽환적" },
  { id: "watercolor", name: "수채화", emoji: "🎨", apiStyle: "수채화" },
  { id: "anime", name: "애니", emoji: "✨", apiStyle: "애니메이션" },
  { id: "fantasy", name: "판타지", emoji: "🦄", apiStyle: "판타지" },
];

// 날짜순으로 정렬된 꿈 목록 (최신순)
const sortedDreams = computed(() => {
  const entries = Object.entries(postedDates.value || {});
  entries.sort((a, b) => new Date(b[0]) - new Date(a[0]));
  return Object.fromEntries(entries);
});

function handleBack() {
  router.push({ name: "calendar" });
}

function formatDateDisplay(dateKey) {
  const [year, month, day] = dateKey.split("-");
  return `${month}월 ${day}일`;
}

function truncateTitle(title, maxLength = 15) {
  if (!title) return "";
  return title.length > maxLength ? title.substring(0, maxLength) + "..." : title;
}

async function loadDreamContent() {
  if (selectedDreamKey.value) {
    selectedDream.value = postedDates.value[selectedDreamKey.value];

    // 해몽 결과 가져오기
    if (selectedDream.value?.dreamId) {
      try {
        const result = await dreamResultService.getDreamResult(selectedDream.value.dreamId);
        selectedDreamResult.value = result;
        console.log("📜 해몽 결과 로드:", result);
      } catch (err) {
        console.log("해몽 결과 없음");
        selectedDreamResult.value = null;
      }
    } else {
      selectedDreamResult.value = null;
    }
  } else {
    selectedDream.value = null;
    selectedDreamResult.value = null;
  }
}

async function generateImage() {
  if (!selectedDreamKey.value || !selectedDream.value) return;

  isGenerating.value = true;
  generateError.value = null;

  try {
    const styleInfo = imageStyles.find((s) => s.id === selectedStyle.value);
    const dreamPrompt = `${selectedDream.value.title}. ${selectedDream.value.content}`;

    console.log("🎨 이미지 생성 요청:", { dream_prompt: dreamPrompt, style: styleInfo.apiStyle });

    // AI API 호출
    const response = await fortuneService.generateDreamImage({
      dream_prompt: dreamPrompt,
      style: styleInfo.apiStyle,
    });

    console.log("✅ 이미지 생성 응답:", response);

    if (response.success && response.images && response.images.length > 0) {
      for (const img of response.images) {
        const imageEntry = {
          id: Date.now() + Math.random(),
          dreamId: selectedDream.value.dreamId,
          dreamKey: selectedDreamKey.value,
          dreamDate: selectedDreamKey.value,
          title: selectedDream.value.title,
          content: selectedDream.value.content,
          interpretation: selectedDreamResult.value?.dreamInterpretation || null,
          fortuneSummary: selectedDreamResult.value?.todayFortuneSummary || null,
          luckyColor: selectedDreamResult.value?.luckyColor || null,
          luckyItem: selectedDreamResult.value?.luckyItem || null,
          style: styleInfo.name,
          imageData: img.image_data,
          mimeType: img.mime_type,
          imageSrc: `data:${img.mime_type};base64,${img.image_data}`,
          caption: `${selectedDream.value.title} - ${styleInfo.name}`,
          createdAt: new Date().toISOString(),
        };

        generatedImages.value.unshift(imageEntry);

        // 자동으로 갤러리에 저장
        await saveToGallery(imageEntry, false);
      }
    } else {
      generateError.value = response.message || "이미지 생성에 실패했습니다.";
    }
  } catch (error) {
    console.error("❌ 이미지 생성 에러:", error);
    generateError.value = error.response?.data?.detail || error.message || "이미지 생성 중 오류가 발생했습니다.";
  } finally {
    isGenerating.value = false;
  }
}

async function saveToGallery(image, showAlert = true) {
  try {
    let savedImageUrl = image.imageSrc;

    // 백엔드에 이미지 업로드
    try {
      console.log("📤 이미지 업로드 중...");
      const uploadResponse = await imageService.uploadImage(image.imageSrc, image.dreamId);

      if (uploadResponse.success && uploadResponse.imageUrl) {
        savedImageUrl = uploadResponse.imageUrl;
        console.log("✅ 이미지 업로드 성공:", savedImageUrl);

        // dream_results 테이블에 이미지 URL 업데이트
        if (image.dreamId) {
          await dreamResultService.updateDreamResult(image.dreamId, {
            imageUrl: savedImageUrl,
          });
        }
      }
    } catch (uploadError) {
      console.warn("⚠️ 서버 업로드 실패, 로컬에만 저장:", uploadError.message);
    }

    // 갤러리 스토어에 저장 (꿈 내용과 해석 정보 포함)
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
    } else {
      console.log("✅ 갤러리에 자동 저장됨");
    }
  } catch (error) {
    console.error("갤러리 저장 실패:", error);
    if (showAlert) {
      alert("저장 중 오류가 발생했습니다.");
    }
  }
}

function downloadImage(image) {
  if (!image.imageSrc) {
    alert("다운로드할 이미지가 없습니다.");
    return;
  }

  try {
    const link = document.createElement("a");
    link.href = image.imageSrc;
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
</script>

<style scoped>
.visualization-card {
  background: white;
  border-radius: 40px;
  padding: 2rem;
  width: 100%;
  max-width: 800px;
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

.visualization-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.dream-select-section label,
.prompt-section label,
.custom-prompt-section label {
  display: block;
  font-size: 0.9rem;
  font-weight: 700;
  color: #666;
  margin-bottom: 0.75rem;
}

.dream-selector {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e8f0fe;
  border-radius: 15px;
  font-size: 1rem;
  font-family: "Nunito", sans-serif;
  background: #fafcff;
  cursor: pointer;
  transition: border-color 0.3s;
}

.dream-selector:focus {
  outline: none;
  border-color: #a2d2ff;
}

.dream-preview {
  background: linear-gradient(135deg, #faf5ff, #f3e8ff);
  padding: 1.25rem;
  border-radius: 16px;
  border-left: 4px solid #a855f7;
  max-height: 200px;
  display: flex;
  flex-direction: column;
}

.preview-header {
  margin-bottom: 0.75rem;
  flex-shrink: 0;
}

.preview-date {
  font-size: 0.75rem;
  font-weight: 600;
  color: #a855f7;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.preview-title {
  margin: 0.25rem 0 0;
  color: #4c2b7b;
  font-size: 1.1rem;
  font-weight: 700;
  line-height: 1.3;
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
  background: rgba(168, 85, 247, 0.3);
  border-radius: 4px;
}

.preview-content {
  margin: 0;
  color: #555;
  line-height: 1.6;
  font-size: 0.9rem;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 에러 메시지 */
.generate-error {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 12px;
  color: #dc2626;
}

.generate-error .error-icon {
  font-size: 1.2rem;
}

.generate-error p {
  margin: 0;
  font-size: 0.85rem;
  line-height: 1.4;
}

.style-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.style-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem 1rem;
  border: 2px solid #e8f0fe;
  border-radius: 15px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.style-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(162, 210, 255, 0.2);
}

.style-btn.active {
  border-color: #a2d2ff;
  background: linear-gradient(135deg, #f8f9ff, #e8f0fe);
  box-shadow: 0 8px 20px rgba(162, 210, 255, 0.3);
}

.style-emoji {
  font-size: 2rem;
}

.style-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #666;
}

.custom-prompt-input {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e8f0fe;
  border-radius: 15px;
  font-size: 1rem;
  font-family: "Nunito", sans-serif;
  background: #fafcff;
  resize: vertical;
  min-height: 100px;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.custom-prompt-input:focus {
  outline: none;
  border-color: #a2d2ff;
  box-shadow: 0 0 0 4px rgba(162, 210, 255, 0.1);
  background: white;
}

.generate-btn {
  width: 100%;
  padding: 1.25rem;
  border: none;
  border-radius: 20px;
  font-size: 1.2rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
}

.generate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px rgba(102, 126, 234, 0.4);
}

.generate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 0.5rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.generated-images {
  margin-top: 2rem;
}

.generated-images h3 {
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  color: #333;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
}

.image-card {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.image-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
}

.image-placeholder {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.image-placeholder.real-image {
  background: linear-gradient(135deg, #f3e8ff, #e8f4ff);
}

.generated-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-card:hover .generated-image {
  transform: scale(1.03);
}

.image-content {
  text-align: center;
  color: white;
  z-index: 1;
}

.image-emoji {
  font-size: 4rem;
  display: block;
  margin-bottom: 1rem;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.image-caption {
  font-size: 1rem;
  font-weight: 600;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  padding: 0 1rem;
}

.image-info {
  padding: 0.75rem 1rem;
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.image-caption-text {
  margin: 0 0 0.5rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  line-height: 1.3;
}

.image-style-badge {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
  background: linear-gradient(135deg, #c77dff, #6fa7ff);
  color: white;
}

.image-actions {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: white;
}

.action-icon-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 600;
  transition: all 0.2s;
}

.auto-saved-badge {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  background: linear-gradient(135deg, #dcfce7, #bbf7d0);
  color: #166534;
  border-radius: 10px;
  font-size: 0.8rem;
  font-weight: 600;
}

.action-icon-btn.download {
  background: #f0f0f0;
  color: #555;
}

.action-icon-btn.download:hover {
  background: #e0e0e0;
}

/* ===== 반응형 ===== */
@media (max-width: 768px) {
  .visualization-card {
    padding: 1.5rem;
    border-radius: 28px;
  }

  .page-title {
    font-size: 2rem;
  }

  .style-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.75rem;
  }

  .style-btn {
    padding: 1rem 0.75rem;
  }

  .style-emoji {
    font-size: 1.5rem;
  }

  .style-name {
    font-size: 0.8rem;
  }

  .dream-preview {
    max-height: 180px;
    padding: 1rem;
  }

  .preview-title {
    font-size: 1rem;
  }

  .preview-content {
    font-size: 0.85rem;
  }

  .image-grid {
    grid-template-columns: 1fr;
  }

  .generate-btn {
    padding: 1rem;
    font-size: 1rem;
  }
}

@media (max-width: 480px) {
  .visualization-card {
    padding: 1.25rem;
    border-radius: 24px;
  }

  .page-title {
    font-size: 1.75rem;
  }

  .style-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.5rem;
  }

  .style-btn {
    padding: 0.875rem 0.5rem;
  }

  .style-emoji {
    font-size: 1.3rem;
  }

  .style-name {
    font-size: 0.75rem;
  }

  .action-icon-btn span {
    display: none;
  }

  .action-icon-btn {
    padding: 0.6rem;
  }
}
</style>
