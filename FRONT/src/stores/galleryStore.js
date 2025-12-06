import { defineStore } from "pinia";
import { ref, computed } from "vue";

const STORAGE_KEY = "dreamGalleryStore";
const LEGACY_KEY = "dreamStore";

export const useGalleryStore = defineStore("gallery", () => {
  const gallery = ref([]);

  const galleryImages = computed(() => gallery.value);

  function addToGallery(image) {
    const entry = {
      // 기본 정보
      id: image.id ?? Date.now(),
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

    gallery.value = [...gallery.value, entry];
    persistGallery();
  }

  function removeFromGallery(imageId) {
    gallery.value = gallery.value.filter((img) => img.id !== imageId);
    persistGallery();
  }

  function toggleImageLike(imageId) {
    const image = gallery.value.find((img) => img.id === imageId);
    if (!image) return;

    image.liked = !image.liked;
    const delta = image.liked ? 1 : -1;
    image.likes = Math.max(0, (image.likes || 0) + delta);
    persistGallery();
  }

  function resetGallery() {
    gallery.value = [];
    persistGallery();
  }

  function persistGallery() {
    if (typeof window === "undefined") return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(gallery.value));
  }

  function hydrateFromLocalStorage() {
    if (typeof window === "undefined") return;

    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      // 기존 데이터에 새 필드 기본값 추가
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
    }

    const legacy = localStorage.getItem(LEGACY_KEY);
    if (legacy) {
      const data = JSON.parse(legacy);
      gallery.value = (data.gallery || []).map((img) => ({
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
    }
  }

  hydrateFromLocalStorage();

  return {
    gallery,
    galleryImages,
    addToGallery,
    removeFromGallery,
    toggleImageLike,
    resetGallery,
    persistGallery,
  };
});
