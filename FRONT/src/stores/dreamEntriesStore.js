import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useGalleryStore } from './galleryStore';
import { useMonthlyMemoStore } from './monthlyMemoStore';
import { luckyColorPalette, getLuckyColorById } from '../constants/luckyColors';
import { dreamService } from '../services/dreamService';

const STORAGE_KEY = 'dreamEntriesStore';
const LEGACY_KEY = 'dreamStore';

function formatDateKey(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function getRandomLuckyColorId() {
  return Math.floor(Math.random() * luckyColorPalette.length);
}

export const useDreamEntriesStore = defineStore('dreamEntries', () => {
  const selectedDate = ref(null);
  const dreamTitle = ref('');
  const dreamContent = ref('');
  const selectedEmotion = ref(null);
  const posts = ref({});
  const showAnalysisOption = ref(false);
  const currentLuckyColorId = ref(getRandomLuckyColorId());
  const loading = ref(false);
  const error = ref(null);
  const currentDreamId = ref(null); // 서버에서 받은 꿈 ID

  function setSelectedDate(date) {
    selectedDate.value = date;
    if (!date) {
      resetWriteFields();
      return;
    }

    const dateKey = formatDateKey(date);
    const existingPost = posts.value[dateKey];

    if (existingPost) {
      dreamTitle.value = existingPost.title;
      dreamContent.value = existingPost.content;
      selectedEmotion.value = existingPost.emotion ?? null;
      currentDreamId.value = existingPost.dreamId ?? null;
      showAnalysisOption.value = true;
      if (typeof existingPost.luckyColorId === 'number') {
        currentLuckyColorId.value = existingPost.luckyColorId;
      }
    } else {
      resetWriteFields();
      showAnalysisOption.value = false;
      currentLuckyColorId.value = getRandomLuckyColorId();
    }
  }

  function resetWriteFields() {
    dreamTitle.value = '';
    dreamContent.value = '';
    selectedEmotion.value = null;
    currentDreamId.value = null;
  }

  // API 연동 꿈 저장
  async function saveDream() {
    if (!selectedDate.value) return false;

    const dateKey = formatDateKey(selectedDate.value);
    const luckyColor = getLuckyColorById(currentLuckyColorId.value);
    
    loading.value = true;
    error.value = null;

    try {
      // 로그인 상태일 때만 API 호출
      const token = localStorage.getItem('accessToken');
      if (token) {
        const dreamData = {
          title: dreamTitle.value,
          content: dreamContent.value,
          dreamDate: dateKey,
          emotionScore: selectedEmotion.value || 3,
        };

        let response;
        if (currentDreamId.value) {
          // 수정
          response = await dreamService.updateDream(currentDreamId.value, dreamData);
        } else {
          // 새로 생성
          response = await dreamService.createDream(dreamData);
          currentDreamId.value = response.dreamId;
        }
      }

      // 로컬 상태 업데이트
      posts.value = {
        ...posts.value,
        [dateKey]: {
          dreamId: currentDreamId.value,
          title: dreamTitle.value,
          content: dreamContent.value,
          emotion: selectedEmotion.value,
          color: luckyColor.hex,
          luckyColorId: luckyColor.id,
          luckyColorName: luckyColor.name
        }
      };

      showAnalysisOption.value = true;
      persistEntries();
      return true;
    } catch (err) {
      error.value = err.response?.data?.message || '꿈 일기 저장에 실패했습니다.';
      console.error('Dream save error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  // API 연동 꿈 삭제
  async function deleteDream() {
    if (!selectedDate.value) return;
    const dateKey = formatDateKey(selectedDate.value);

    if (!posts.value[dateKey]) return;

    loading.value = true;
    error.value = null;

    try {
      // 로그인 상태이고 dreamId가 있으면 API 호출
      const token = localStorage.getItem('accessToken');
      const dreamId = posts.value[dateKey]?.dreamId;
      
      if (token && dreamId) {
        await dreamService.deleteDream(dreamId);
      }

      const updatedPosts = { ...posts.value };
      delete updatedPosts[dateKey];
      posts.value = updatedPosts;

      resetWriteFields();
      selectedEmotion.value = null;
      showAnalysisOption.value = false;
      persistEntries();
    } catch (err) {
      error.value = err.response?.data?.message || '꿈 일기 삭제에 실패했습니다.';
      console.error('Dream delete error:', err);
    } finally {
      loading.value = false;
    }
  }

  // 서버에서 월별 꿈 목록 가져오기
  async function fetchDreamsByMonth(year, month) {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    loading.value = true;
    error.value = null;

    try {
      const response = await dreamService.getDreamsByMonth(year, month);
      
      // 서버 데이터를 로컬 상태에 병합
      if (response.dreams && Array.isArray(response.dreams)) {
        response.dreams.forEach(dream => {
          const dateKey = dream.dreamDate;
          posts.value[dateKey] = {
            dreamId: dream.dreamId,
            title: dream.title,
            content: dream.content,
            emotion: dream.emotionScore,
            color: dream.luckyColor || getLuckyColorById(getRandomLuckyColorId()).hex,
            luckyColorId: dream.luckyColorId || getRandomLuckyColorId(),
            luckyColorName: dream.luckyColorName || ''
          };
        });
        persistEntries();
      }
    } catch (err) {
      error.value = err.response?.data?.message || '꿈 목록을 가져오는데 실패했습니다.';
      console.error('Fetch dreams error:', err);
    } finally {
      loading.value = false;
    }
  }

  function enableEditMode() {
    showAnalysisOption.value = false;
  }

  function resetWriteState() {
    showAnalysisOption.value = false;
  }

  function clearSelectedDate() {
    selectedDate.value = null;
    resetWriteFields();
    showAnalysisOption.value = false;
    currentLuckyColorId.value = getRandomLuckyColorId();
  }

  function resetAll() {
    clearSelectedDate();
    posts.value = {};
    currentLuckyColorId.value = getRandomLuckyColorId();
    persistEntries();

    // cascade reset to other domains
    const galleryStore = useGalleryStore();
    galleryStore.resetGallery();

    const memoStore = useMonthlyMemoStore();
    memoStore.resetMemos();
  }

  function setEmotion(emotion) {
    selectedEmotion.value = emotion;
  }

  function getMonthlyStats(year, month) {
    const yearMonth = `${year}-${String(month).padStart(2, '0')}`;
    const monthlyDreams = Object.entries(posts.value).filter(([dateKey]) =>
      dateKey.startsWith(yearMonth)
    );

    return {
      totalDreams: monthlyDreams.length,
      dreams: monthlyDreams.map(([dateKey, dream]) => ({
        date: dateKey,
        ...dream
      }))
    };
  }

  const formattedSelectedDate = computed(() => {
    if (!selectedDate.value) return '';
    return selectedDate.value.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  });

  const postedDates = computed(() => posts.value);
  const currentLuckyColor = computed(() => getLuckyColorById(currentLuckyColorId.value));

  function setLuckyColorId(id) {
    if (typeof id === 'number' && luckyColorPalette.some(color => color.id === id)) {
      currentLuckyColorId.value = id;
    } else {
      currentLuckyColorId.value = getRandomLuckyColorId();
    }

    persistEntries();
  }

  function persistEntries() {
    if (typeof window === 'undefined') return;
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        posts: posts.value,
        luckyColorId: currentLuckyColorId.value
      })
    );
  }

  function hydrateFromLocalStorage() {
    if (typeof window === 'undefined') return;

    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const data = JSON.parse(saved);
      posts.value = data.posts || {};
      currentLuckyColorId.value =
        typeof data.luckyColorId === 'number' ? data.luckyColorId : 0;
      return;
    }

    const legacy = localStorage.getItem(LEGACY_KEY);
    if (legacy) {
      const data = JSON.parse(legacy);
      posts.value = data.posts || {};
    }
  }

  hydrateFromLocalStorage();

  return {
    selectedDate,
    dreamTitle,
    dreamContent,
    selectedEmotion,
    posts,
    showAnalysisOption,
    formattedSelectedDate,
    postedDates,
    currentLuckyColor,
    loading,
    error,
    currentDreamId,
    setSelectedDate,
    saveDream,
    deleteDream,
    setEmotion,
    enableEditMode,
    resetWriteState,
    clearSelectedDate,
    resetAll,
    setLuckyColorId,
    getMonthlyStats,
    persistEntries,
    fetchDreamsByMonth
  };
});

