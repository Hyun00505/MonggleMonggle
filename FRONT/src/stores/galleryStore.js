import { defineStore } from "pinia";
import { ref, computed, watch } from "vue";
import { useAuthStore } from "./authStore";
import { dreamResultService } from "../services/dreamResultService";

const STORAGE_PREFIX = "dreamGallery_";
const LEGACY_KEY = "dreamGalleryStore";

export const useGalleryStore = defineStore("gallery", () => {
  const gallery = ref([]);
  const authStore = useAuthStore();

  const galleryImages = computed(() => gallery.value);

  // 현재 사용자의 스토리지 키 생성
  function getStorageKey() {
    // authStore.currentUser는 computed, user는 ref라 .value 접근 필요
    const userId = authStore.currentUser?.value?.userId ?? authStore.user?.value?.userId ?? authStore.currentUser?.userId ?? authStore.user?.userId;
    return userId ? `${STORAGE_PREFIX}${userId}` : null;
  }

  function addToGallery(image) {
    // 동일 ID가 이미 있으면 업데이트
    const existingIndex = gallery.value.findIndex((img) => img.id === (image.id ?? image.dreamId));
    const entryId = image.id ?? image.dreamId ?? Date.now();

    const entry = {
      // 기본 정보
      id: entryId,
      dreamId: image.dreamId ?? null,
      dreamDate: image.dreamDate ?? null,

      // 꿈 일기 정보
      title: image.title ?? "",
      content: image.content ?? "",
      caption: image.caption ?? image.title ?? "",

      // 해석 정보
      interpretation: image.interpretation ?? null,
      fortuneSummary: image.fortuneSummary ?? null,
      luckyColor: image.luckyColor ?? null,
      luckyItem: image.luckyItem ?? null,

      // 이미지 정보
      style: image.style ?? "",
      imageSrc: image.imageSrc ?? null,
      mimeType: image.mimeType ?? "image/png",
      gradient: image.gradient ?? null,
      emoji: image.emoji ?? null,

      // 좋아요/통계
      likes: typeof image.likes === "number" ? image.likes : 0,
      liked: image.liked ?? false,

      // 타임스탬프
      createdAt: image.createdAt ?? new Date().toISOString(),
      savedAt: image.savedAt ?? new Date().toISOString(),
    };

    if (existingIndex >= 0) {
      // 기존 항목 병합 업데이트
      gallery.value.splice(existingIndex, 1, { ...gallery.value[existingIndex], ...entry });
    } else {
      gallery.value = [...gallery.value, entry];
    }
    persistGallery();
  }

  function removeFromGallery(imageId) {
    gallery.value = gallery.value.filter((img) => img.id !== imageId);
    persistGallery();
  }

  async function toggleImageLike(imageId) {
    const image = gallery.value.find((img) => img.id === imageId);
    if (!image?.dreamId) return;
  
    try {
      // 서버 동기화
      const response = await dreamResultService.toggleLike(image.dreamId);
      
      // 서버 응답으로 상태 업데이트
      image.liked = response.isLiked;
      persistGallery();
    } catch (error) {
      console.error("찜 토글 실패:", error);
      // 서버 실패 시 로컬에서만 토글 (오프라인 대응)
      image.liked = !image.liked;
      persistGallery();
    }
  }

  function resetGallery() {
    gallery.value = [];
    persistGallery();
  }

  function persistGallery() {
    if (typeof window === "undefined") return;

    const key = getStorageKey();
    if (key) {
      localStorage.setItem(key, JSON.stringify(gallery.value));
    }
  }

  function hydrateFromLocalStorage() {
    if (typeof window === "undefined") return;

    const key = getStorageKey();

    // 로그인하지 않은 경우 빈 배열로 초기화
    if (!key) {
      gallery.value = [];
      return;
    }

    // 현재 사용자의 갤러리 데이터 로드
    const saved = localStorage.getItem(key);
    if (saved) {
      try {
        gallery.value = JSON.parse(saved).map((img) => ({
          ...img,
          dreamId: img.dreamId ?? null,
          dreamDate: img.dreamDate ?? null,
          title: img.title ?? img.caption ?? "",
          content: img.content ?? "",
          interpretation: img.interpretation ?? null,
          fortuneSummary: img.fortuneSummary ?? null,
          luckyColor: img.luckyColor ?? null,
          luckyItem: img.luckyItem ?? null,
          likes: typeof img.likes === "number" ? img.likes : 0,
          liked: img.liked ?? false,
        }));
        return;
      } catch (e) {
        console.error("갤러리 데이터 파싱 실패:", e);
      }
    }

    // 레거시 데이터 마이그레이션 (처음 로그인 시)
    migrateLegacyData(key);
  }

  // 기존 데이터 마이그레이션
  function migrateLegacyData(userKey) {
    const legacy = localStorage.getItem(LEGACY_KEY);
    if (legacy) {
      try {
        const data = JSON.parse(legacy);
        const legacyGallery = Array.isArray(data) ? data : data.gallery || [];

        gallery.value = legacyGallery.map((img) => ({
          ...img,
          dreamId: null,
          dreamDate: null,
          title: img.caption || "",
          content: "",
          interpretation: null,
          fortuneSummary: null,
          luckyColor: null,
          luckyItem: null,
          likes: typeof img.likes === "number" ? img.likes : Math.floor(Math.random() * 20),
          liked: img.liked ?? false,
        }));

        // 새 키로 저장하고 레거시 키 삭제
        localStorage.setItem(userKey, JSON.stringify(gallery.value));
        localStorage.removeItem(LEGACY_KEY);
      } catch (e) {
        console.error("레거시 데이터 마이그레이션 실패:", e);
        gallery.value = [];
      }
    } else {
      gallery.value = [];
    }
  }

  // 사용자 변경 감지하여 갤러리 데이터 다시 로드
  function onUserChange() {
    hydrateFromLocalStorage();
  }

  // authStore의 user 변경 감지
  watch(
    () => authStore.currentUser?.value?.userId ?? authStore.user?.value?.userId,
    (newUserId, oldUserId) => {
      if (newUserId !== oldUserId) {
        hydrateFromLocalStorage();
      }
    },
    { immediate: true }
  );

  // 초기화
  hydrateFromLocalStorage();

  return {
    gallery,
    galleryImages,
    addToGallery,
    removeFromGallery,
    toggleImageLike,
    resetGallery,
    persistGallery,
    hydrateFromLocalStorage,
    onUserChange,
  };
});
