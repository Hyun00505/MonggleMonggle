import { defineStore } from 'pinia';
import { ref } from 'vue';

const STORAGE_KEY = 'dreamMonthlyMemoStore';
const LEGACY_KEY = 'dreamStore';

function getMemoKey(year, month) {
  return `${year}-${String(month).padStart(2, '0')}`;
}

export const useMonthlyMemoStore = defineStore('monthlyMemos', () => {
  const monthlyMemos = ref({});

  // 새 메모 추가
  function addMemo(year, month, content) {
    const key = getMemoKey(year, month);
    const newMemo = {
      id: Date.now(),
      content,
      createdAt: new Date().toISOString()
    };
    
    if (!monthlyMemos.value[key]) {
      monthlyMemos.value[key] = [];
    }
    
    monthlyMemos.value = {
      ...monthlyMemos.value,
      [key]: [...monthlyMemos.value[key], newMemo]
    };
    persistMemos();
    return newMemo;
  }

  // 메모 수정
  function updateMemo(year, month, memoId, content) {
    const key = getMemoKey(year, month);
    if (!monthlyMemos.value[key]) return;
    
    monthlyMemos.value = {
      ...monthlyMemos.value,
      [key]: monthlyMemos.value[key].map(memo => 
        memo.id === memoId ? { ...memo, content } : memo
      )
    };
    persistMemos();
  }

  // 메모 삭제
  function deleteMemo(year, month, memoId) {
    const key = getMemoKey(year, month);
    if (!monthlyMemos.value[key]) return;
    
    monthlyMemos.value = {
      ...monthlyMemos.value,
      [key]: monthlyMemos.value[key].filter(memo => memo.id !== memoId)
    };
    persistMemos();
  }

  // 월별 메모 목록 가져오기
  function getMonthlyMemos(year, month) {
    const key = getMemoKey(year, month);
    return monthlyMemos.value[key] || [];
  }

  // 하위 호환을 위한 단일 메모 가져오기 (기존 데이터 마이그레이션용)
  function getMonthlyMemo(year, month) {
    const memos = getMonthlyMemos(year, month);
    if (memos.length > 0) {
      return memos[0].content;
    }
    return '';
  }

  // 하위 호환을 위한 단일 메모 저장 (기존 코드 호환용)
  function saveMonthlyMemo(year, month, memo) {
    const key = getMemoKey(year, month);
    const memos = monthlyMemos.value[key] || [];
    
    if (memos.length === 0) {
      addMemo(year, month, memo);
    } else {
      updateMemo(year, month, memos[0].id, memo);
    }
  }

  function resetMemos() {
    monthlyMemos.value = {};
    persistMemos();
  }

  function persistMemos() {
    if (typeof window === 'undefined') return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(monthlyMemos.value));
  }

  function hydrateFromLocalStorage() {
    if (typeof window === 'undefined') return;

    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const data = JSON.parse(saved);
      // 기존 문자열 데이터를 배열로 마이그레이션
      Object.keys(data).forEach(key => {
        if (typeof data[key] === 'string') {
          data[key] = data[key] ? [{
            id: Date.now(),
            content: data[key],
            createdAt: new Date().toISOString()
          }] : [];
        }
      });
      monthlyMemos.value = data;
      return;
    }

    const legacy = localStorage.getItem(LEGACY_KEY);
    if (legacy) {
      const data = JSON.parse(legacy);
      const legacyMemos = data.monthlyMemos || {};
      // 레거시 데이터도 배열로 변환
      Object.keys(legacyMemos).forEach(key => {
        if (typeof legacyMemos[key] === 'string') {
          legacyMemos[key] = legacyMemos[key] ? [{
            id: Date.now(),
            content: legacyMemos[key],
            createdAt: new Date().toISOString()
          }] : [];
        }
      });
      monthlyMemos.value = legacyMemos;
    }
  }

  hydrateFromLocalStorage();

  return {
    monthlyMemos,
    addMemo,
    updateMemo,
    deleteMemo,
    getMonthlyMemos,
    getMonthlyMemo,
    saveMonthlyMemo,
    resetMemos,
    persistMemos
  };
});
