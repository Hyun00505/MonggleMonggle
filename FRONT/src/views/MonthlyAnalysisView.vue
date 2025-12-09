<template>
  <div class="monthly-analysis-card">
    <div class="card-header">
      <button @click="handleBack" class="back-btn">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
      </button>
      <h2 class="page-title">
        월별 분석
        <span class="title-badge">Monthly</span>
      </h2>
      <button @click="handleClose" class="close-btn" aria-label="닫기">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </button>
    </div>

    <div class="month-selector-section">
      <button @click="changeMonth(-1)" class="month-nav-btn">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </button>
      <div class="current-month">{{ currentYear }}년 {{ currentMonth }}월</div>
      <button @click="changeMonth(1)" class="month-nav-btn">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </button>
    </div>

    <div class="monthly-analysis-content" v-if="!isLockedMonth">
      <!-- 왼쪽 컬럼 -->
      <div class="left-column">

        <!-- 통계 카드 -->
        <div class="stats-grid">
          <div class="stat-card dreams-card">
            <div class="stat-icon-wrap">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <line x1="10" y1="9" x2="8" y2="9" />
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ monthlyStats.totalDreams }}</div>
              <div class="stat-label">기록된 꿈</div>
            </div>
          </div>
          <div class="stat-card streak-card">
            <div class="stat-icon-wrap">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2c1 3 2.5 3.5 3.5 4.5A5 5 0 0 1 17 10a5 5 0 1 1-10 0c0-.3 0-.6.1-.9a2 2 0 1 0 3.3-2C11 6.5 12 5 12 2z" />
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ monthlyStats.streak }}</div>
              <div class="stat-label">연속 기록</div>
            </div>
          </div>
        </div>

        <!-- 꿈 목록 -->
        <div class="section-card">
          <div class="dream-header">
            <h3 class="section-title">
              <span class="title-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
                </svg>
              </span>
              이번 달 꿈 목록
            </h3>
            <div v-if="totalDreamPages > 1" class="dream-pagination">
              <button class="page-btn icon" @click="prevDreamPage" :disabled="currentDreamPage === 1">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="15 18 9 12 15 6"></polyline>
                </svg>
              </button>
              <span class="page-indicator">{{ currentDreamPage }} / {{ totalDreamPages }}</span>
              <button class="page-btn icon" @click="nextDreamPage" :disabled="currentDreamPage === totalDreamPages">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 6 15 12 9 18"></polyline>
                </svg>
              </button>
            </div>
          </div>
          <div v-if="monthlyDreams.length > 0" class="dream-list">
            <div v-for="dream in visibleDreams" :key="dream.date" class="dream-item" @click="goToDream(dream.date)">
              <div class="dream-date-badge" :class="dream.colorClass">{{ dream.day }}일</div>
              <div class="dream-info">
                <h4>{{ dream.title }}</h4>
                <p>{{ dream.preview }}</p>
              </div>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </div>
          </div>
          <div v-else class="empty-state">
            <span class="empty-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
            </span>
            <p>이번 달에 기록된 꿈이 없습니다</p>
          </div>
        </div>
      </div>

      <!-- 오른쪽 컬럼 -->
      <div class="right-column">
        <!-- 월별 메모 -->
        <div class="section-card memo-section">
          <div class="section-header">
            <h3 class="section-title">
              <span class="title-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                </svg>
              </span>
              이번 달 회고
            </h3>
            <button @click="startAddMemo" class="add-memo-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              메모 추가
            </button>
          </div>

          <!-- 새 메모 작성 -->
          <div v-if="isAddingMemo" class="memo-note new-memo">
            <textarea v-model="newMemoContent" class="memo-textarea" placeholder="이번 달 꿈 기록을 되돌아보며 느낀 점을 자유롭게 작성해보세요..." ref="newMemoInput" />
            <div class="memo-note-actions">
              <button @click="addNewMemo" class="save-btn" :disabled="!newMemoContent.trim()">저장</button>
              <button @click="cancelAddMemo" class="cancel-btn">취소</button>
            </div>
          </div>

          <!-- 메모 목록 -->
          <div v-if="monthlyMemos.length > 0" class="memo-list">
            <div v-for="memo in monthlyMemos" :key="memo.id" class="memo-note" :class="getMemoColorClass(memo.id)">
              <div v-if="editingMemoId !== memo.id" class="memo-note-content">
                <p :class="{ 'memo-text-clamped': !isExpanded(memo.id) }">{{ memo.content }}</p>
                <button v-if="shouldShowExpandBtn(memo.content)" @click="toggleExpand(memo.id)" class="expand-btn">
                  {{ isExpanded(memo.id) ? "접기" : "더보기" }}
                </button>
                <div class="memo-note-footer">
                  <span class="memo-date">{{ formatDate(memo.createdAt) }}</span>
                  <div class="memo-note-btns">
                    <button @click="startEditMemo(memo)" class="memo-action-btn edit">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                      </svg>
                    </button>
                    <button @click="deleteMemoItem(memo.id)" class="memo-action-btn delete">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
              <div v-else class="memo-note-edit">
                <textarea v-model="editMemoContent" class="memo-textarea" />
                <div class="memo-note-actions">
                  <button @click="saveEditMemo" class="save-btn" :disabled="!editMemoContent.trim()">저장</button>
                  <button @click="cancelEditMemo" class="cancel-btn">취소</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 빈 상태 -->
          <div v-else-if="!isAddingMemo" class="memo-empty">
            <p>이번 달의 꿈에 대한 소감을 작성해보세요</p>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="lock-card">
      <div class="lock-text">
        <div class="lock-caption">
          <div class="caption-main">이 달의 꿈 기록은 {{ nextAvailableText }}에 열려요</div>
          <div class="caption-sub">조금만 더 기다려 주세요 ✨</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { useMonthlyMemoStore } from "../stores/monthlyMemoStore";

const route = useRoute();
const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const memoStore = useMonthlyMemoStore();
const { postedDates } = storeToRefs(dreamEntriesStore);

const now = ref(new Date());
let nowTimer = null;

const lastMonthDate = computed(() => new Date(now.value.getFullYear(), now.value.getMonth() - 1, 1));
const currentYear = ref(lastMonthDate.value.getFullYear());
const currentMonth = ref(lastMonthDate.value.getMonth() + 1);

const isLockedMonth = computed(() => {
  const selectedKey = currentYear.value * 12 + currentMonth.value;
  const currentKey = now.value.getFullYear() * 12 + (now.value.getMonth() + 1);
  return selectedKey >= currentKey;
});

const nextAvailableText = computed(() => {
  // 선택된 월 기준 다음 달 1일을 안내
  const nextMonthDate = new Date(currentYear.value, currentMonth.value, 1);
  const y = nextMonthDate.getFullYear();
  const m = String(nextMonthDate.getMonth() + 1).padStart(2, "0");
  return `${y}년 ${m}월 01일`;
});

// 메모 관련 상태
const monthlyMemos = ref([]);
const isAddingMemo = ref(false);
const newMemoContent = ref("");
const editingMemoId = ref(null);
const editMemoContent = ref("");
const expandedMemos = ref(new Set());

// 색상 클래스 배열
const colorClasses = ["color-purple", "color-pink", "color-blue"];
const memoColorClasses = ["memo-purple", "memo-pink", "memo-pink", "memo-purple"];

// 월 변경
function changeMonth(delta) {
  currentMonth.value += delta;
  if (currentMonth.value > 12) {
    currentMonth.value = 1;
    currentYear.value += 1;
  } else if (currentMonth.value < 1) {
    currentMonth.value = 12;
    currentYear.value -= 1;
  }
  updateRouteQuery();
}

// 라우터 쿼리와 상태 동기화
function syncFromRoute() {
  const qYear = Number(route.query.year);
  const qMonth = Number(route.query.month);
  if (!Number.isNaN(qYear) && qYear > 0) currentYear.value = qYear;
  if (!Number.isNaN(qMonth) && qMonth >= 1 && qMonth <= 12) currentMonth.value = qMonth;
}

function updateRouteQuery() {
  const query = {
    ...route.query,
    year: currentYear.value,
    month: String(currentMonth.value).padStart(2, "0"),
  };
  router.replace({ name: "monthly-analysis", query });
}

onMounted(() => {
  syncFromRoute();
  updateRouteQuery();
  nowTimer = setInterval(() => {
    now.value = new Date();
  }, 60 * 1000);
});

onUnmounted(() => {
  if (nowTimer) {
    clearInterval(nowTimer);
  }
});

watch(
  () => route.query,
  () => {
    syncFromRoute();
  }
);

// 해당 월의 꿈 필터링
const monthlyDreams = computed(() => {
  if (isLockedMonth.value) return [];

  const dreams = [];
  const yearMonth = `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;

  const entries = postedDates.value ? Object.entries(postedDates.value) : [];

  entries.forEach(([dateKey, dream], index) => {
    if (dateKey.startsWith(yearMonth)) {
      const day = parseInt(dateKey.split("-")[2]);
      const title = dream?.title || "";
      const content = dream?.content || "";
      dreams.push({
        date: dateKey,
        day: day,
        title,
        preview: content.substring(0, 50) + (content.length > 50 ? "..." : ""),
        colorClass: colorClasses[index % 3],
      });
    }
  });

  return dreams.sort((a, b) => a.day - b.day);
});

// 꿈 목록 페이지네이션 (4개씩)
const dreamPageSize = 4;
const currentDreamPage = ref(1);
const totalDreamPages = computed(() =>
  Math.max(1, Math.ceil(monthlyDreams.value.length / dreamPageSize))
);
const visibleDreams = computed(() => {
  const start = (currentDreamPage.value - 1) * dreamPageSize;
  return monthlyDreams.value.slice(start, start + dreamPageSize);
});

function nextDreamPage() {
  if (currentDreamPage.value < totalDreamPages.value) currentDreamPage.value += 1;
}

function prevDreamPage() {
  if (currentDreamPage.value > 1) currentDreamPage.value -= 1;
}

watch(monthlyDreams, () => {
  currentDreamPage.value = 1;
});

// 통계 계산 (실제 최장 연속 기록일을 계산)
const monthlyStats = computed(() => {
  const total = monthlyDreams.value.length;

  // 날짜(day)만 뽑아서 정렬 후 최장 연속 길이 계산
  const days = Array.from(new Set(monthlyDreams.value.map((d) => d.day))).sort((a, b) => a - b);
  let longest = 0;
  let current = 0;
  let prev = null;

  for (const day of days) {
    if (prev !== null && day === prev + 1) {
      current += 1;
    } else {
      current = 1;
    }
    longest = Math.max(longest, current);
    prev = day;
  }

  return {
    totalDreams: total,
    streak: `${longest}일`,
  };
});

function handleBack() {
  // 브라우저 히스토리가 있을 경우 뒤로가기, 없으면 캘린더로 이동
  const historyLength = window.history.length;
  if (historyLength > 1) {
    router.back();
  } else {
    router.push({ name: "calendar" });
  }
}

function handleClose() {
  router.push({ name: "calendar" });
}

function goToDream(dateKey) {
  // 해당 날짜의 꿈으로 이동
  router.push({ name: "write", query: { date: dateKey } });
}

// 메모 로드
function loadMonthlyMemos() {
  const memos = memoStore.getMonthlyMemos(currentYear.value, currentMonth.value);
  monthlyMemos.value = sanitizeMemos(memos);
  // 상태 초기화
  isAddingMemo.value = false;
  newMemoContent.value = "";
  editingMemoId.value = null;
  editMemoContent.value = "";
}

// 내용이 비어 있는 메모 제거
function sanitizeMemos(memos) {
  if (!Array.isArray(memos)) return [];
  return memos.filter((memo) => memo?.content && memo.content.trim().length > 0);
}

// 선택된 연월의 꿈 일기 서버 동기화
function loadMonthlyDreams() {
  if (isLockedMonth.value) return;
  dreamEntriesStore.fetchDreamsByMonth(currentYear.value, currentMonth.value);
}

// 새 메모 추가 시작
function startAddMemo() {
  isAddingMemo.value = true;
  newMemoContent.value = "";
}

// 새 메모 저장
function addNewMemo() {
  if (!newMemoContent.value.trim()) return;
  memoStore.addMemo(currentYear.value, currentMonth.value, newMemoContent.value.trim());
  loadMonthlyMemos();
}

// 새 메모 추가 취소
function cancelAddMemo() {
  isAddingMemo.value = false;
  newMemoContent.value = "";
}

// 메모 수정 시작
function startEditMemo(memo) {
  editingMemoId.value = memo.id;
  editMemoContent.value = memo.content;
}

// 메모 수정 저장
function saveEditMemo() {
  if (!editMemoContent.value.trim()) return;
  memoStore.updateMemo(currentYear.value, currentMonth.value, editingMemoId.value, editMemoContent.value.trim());
  loadMonthlyMemos();
}

// 메모 수정 취소
function cancelEditMemo() {
  editingMemoId.value = null;
  editMemoContent.value = "";
}

// 메모 삭제
function deleteMemoItem(memoId) {
  if (confirm("이 메모를 삭제하시겠습니까?")) {
    memoStore.deleteMemo(currentYear.value, currentMonth.value, memoId);
    loadMonthlyMemos();
  }
}

// 메모 색상 클래스
function getMemoColorClass(memoId) {
  const index = monthlyMemos.value.findIndex((m) => m.id === memoId);
  return memoColorClasses[index % memoColorClasses.length];
}

// 날짜 포맷
function formatDate(dateString) {
  const date = new Date(dateString);
  const yy = String(date.getFullYear()).slice(-2);
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yy}.${mm}.${dd}`;
}

// 메모 펼치기/접기
function toggleExpand(memoId) {
  if (expandedMemos.value.has(memoId)) {
    expandedMemos.value.delete(memoId);
  } else {
    expandedMemos.value.add(memoId);
  }
  // 반응성 트리거
  expandedMemos.value = new Set(expandedMemos.value);
}

function isExpanded(memoId) {
  return expandedMemos.value.has(memoId);
}

// 내용이 5줄 이상인지 체크 (대략 100자 이상)
function shouldShowExpandBtn(content) {
  return content.length > 100 || content.split("\n").length > 4;
}

// 초기 로드
loadMonthlyMemos();
loadMonthlyDreams();
watch([currentYear, currentMonth], () => {
  loadMonthlyMemos();
  loadMonthlyDreams();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Dongle:wght@300;400;700&display=swap");

/* 파스텔 색상 변수 */
:root {
  --pastel-purple: #cdb4db;
  --pastel-pink: #ffc8dd;
  --pastel-blue: #a2d2ff;
  --pastel-purple-dark: #b799c7;
  --pastel-pink-dark: #ffb3cc;
  --pastel-blue-dark: #8bb8e8;
}

.monthly-analysis-card {
  background: white;
  border-radius: 32px;
  padding: 1.5rem 2rem 2rem;
  width: 100%;
  max-width: 1100px;
  box-shadow: 0 16px 48px rgba(100, 100, 200, 0.12);
  animation: fadeSlideUp 0.5s ease;
  overflow: hidden; /* 레이아웃 튐 방지 */
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.back-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.2s;
}

.back-btn:hover,
.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.2s;
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
  background: linear-gradient(135deg, rgba(205, 180, 219, 0.3), rgba(162, 210, 255, 0.3));
  -webkit-text-fill-color: #4c2b7b;
  line-height: 1.2;
  overflow: visible;
}

.title-badge {
  font-size: 0.7rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: 0.3rem 0.8rem;
  border-radius: 999px;
  background: white;
  color: #b799c7;
  font-weight: 600;
}

.monthly-analysis-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 380px);
  gap: 1.8rem;
  align-items: start;
}

.left-column {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  min-width: 0;
}

.right-column {
  display: flex;
  flex-direction: column;
  padding-left: 1.8rem;
  border-left: 1px solid rgba(205, 180, 219, 0.3);
  min-width: 0; /* 폭 확장으로 인한 튐 방지 */
  height: 100%;
}

.right-column .memo-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.month-selector-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  padding: 1rem;
  background: linear-gradient(135deg, rgba(205, 180, 219, 0.15), rgba(255, 200, 221, 0.15), rgba(162, 210, 255, 0.15));
  border-radius: 20px;
  margin-bottom: 1.5rem;
}

.month-nav-btn {
  background: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  cursor: pointer;
  color: #b799c7;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(205, 180, 219, 0.2);
}

.month-nav-btn:hover {
  background: #cdb4db;
  color: white;
  transform: scale(1.08);
}

.current-month {
  font-family: "Dongle", sans-serif;
  font-size: 3rem;
  font-weight: 700;
  color: #4c2b7b;
  min-width: 180px;
  text-align: center;
  line-height: 1.2;
}

.stats-grid {
  display: flex;
  gap: 0.75rem;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 1rem 1.25rem;
  border-radius: 18px;
  transition: all 0.25s ease;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.dreams-card {
  background: linear-gradient(135deg, #e8dcf0 0%, #ddd0e8 100%);
  color: #5a3d7a;
  box-shadow: 0 6px 20px rgba(205, 180, 219, 0.25);
}

.dreams-card:hover {
  box-shadow: 0 8px 28px rgba(205, 180, 219, 0.4);
}

.streak-card {
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e0 100%);
  color: #7a4a5a;
  box-shadow: 0 6px 20px rgba(255, 200, 221, 0.25);
}

.streak-card:hover {
  box-shadow: 0 8px 28px rgba(255, 200, 221, 0.4);
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
}

.dreams-card .stat-icon-wrap svg {
  width: 26px;
  height: 26px;
  stroke: #8a6aa8;
}

.streak-card .stat-icon-wrap svg {
  width: 38px;
  height: 38px;
  stroke: #c97a8d;
  top: 5px;
}

.stat-content {
  display: flex;
  flex-direction: row;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.stat-value {
  font-family: "Dongle", sans-serif;
  font-size: 3rem;
  font-weight: 700;
  line-height: 1;
  letter-spacing: -0.5px;
  color: #4c2b7b;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

.stat-label {
  font-size: 1rem;
  opacity: 0.9;
  font-weight: 700;
  margin-top: 2px;
}

.section-card {
  background: linear-gradient(135deg, rgba(205, 180, 219, 0.1), rgba(255, 200, 221, 0.1), rgba(162, 210, 255, 0.1));
  border-radius: 20px;
  padding: 1.5rem;
  border: 1px solid rgba(205, 180, 219, 0.2);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: "Dongle", sans-serif;
  font-size: 1.8rem;
  margin: 0 0 1rem 0;
  color: #4c2b7b;
  line-height: 1.2;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-icon svg {
  width: 20px;
  height: 20px;
  stroke: #8a6aa8;
}

/* 메모 섹션 */
.memo-section {
  padding-bottom: 1rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header .section-title {
  margin: 0;
}

.add-memo-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #cdb4db, #b799c7);
  color: white;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 3px 10px rgba(205, 180, 219, 0.3);
}

.add-memo-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(205, 180, 219, 0.4);
}

.memo-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  flex: 1;
  overflow-y: auto;
  /* max-height: 500px; */
  padding-right: 0.5rem;
  /* Firefox 스크롤바 */
  scrollbar-width: thin;
  scrollbar-color: #cdb4db rgba(205, 180, 219, 0.1);
}

/* Webkit 스크롤바 (Chrome, Safari, Edge) */
.memo-list::-webkit-scrollbar {
  width: 6px;
}

.memo-list::-webkit-scrollbar-track {
  background: rgba(205, 180, 219, 0.1);
  border-radius: 3px;
}

.memo-list::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #cdb4db, #ffc8dd);
  border-radius: 3px;
}

.memo-list::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #b799c7, #ffb3cc);
}

.memo-note {
  position: relative;
  padding: 1rem;
  border-radius: 4px 4px 20px 4px;
  min-height: 100px;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 3px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
}

.memo-note::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  border-radius: 4px 4px 0 0;
}

.memo-note:hover {
  transform: translateY(-3px) rotate(-0.5deg);
  box-shadow: 4px 6px 15px rgba(0, 0, 0, 0.12);
}

/* 메모 색상들 */
.memo-purple {
  background: linear-gradient(180deg, #f3eef7 0%, #ebe4f0 100%);
}
.memo-purple::before {
  background: #cdb4db;
}

.memo-pink {
  background: linear-gradient(180deg, #fff0f5 0%, #ffe8ef 100%);
}
.memo-pink::before {
  background: #ffc8dd;
}

.new-memo {
  background: white;
  border: 2px dashed rgba(205, 180, 219, 0.4);
  grid-column: 1 / -1;
  min-height: auto;
  margin-bottom: 16px;
}

.new-memo::before {
  display: none;
}

.memo-note-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.memo-note-content p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.6;
  color: #444;
  white-space: pre-wrap;
  word-break: break-word;
}

.memo-note-content p.memo-text-clamped {
  display: -webkit-box;
  -webkit-line-clamp: 5;
  line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.expand-btn {
  display: inline-block;
  margin-top: 0.5rem;
  padding: 0.25rem 0.6rem;
  border: none;
  border-radius: 6px;
  background: rgba(205, 180, 219, 0.2);
  color: #8a6aa8;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.expand-btn:hover {
  background: rgba(205, 180, 219, 0.4);
  color: #6a4a88;
}

.memo-note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.75rem;
  padding-top: 0.5rem;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.memo-date {
  font-size: 0.75rem;
  color: #999;
}

.memo-note-btns {
  display: flex;
  gap: 0.25rem;
}

.memo-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.2s;
}

.memo-action-btn.edit {
  color: #8a6aa8;
}

.memo-action-btn.edit:hover {
  background: #cdb4db;
  color: white;
}

.memo-action-btn.delete {
  color: #c97a8d;
}

.memo-action-btn.delete:hover {
  background: #ffc8dd;
  color: white;
}

.memo-note-edit {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.memo-textarea {
  width: 100%;
  min-height: 121px;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-family: inherit;
  resize: none;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.2s;
  /* Firefox 스크롤바 */
  scrollbar-width: thin;
  scrollbar-color: #cdb4db rgba(205, 180, 219, 0.1);
}

/* Webkit 스크롤바 (Chrome, Safari, Edge) */
.memo-textarea::-webkit-scrollbar {
  width: 8px;
}

.memo-textarea::-webkit-scrollbar-track {
  background: rgba(205, 180, 219, 0.1);
  border-radius: 4px;
}

.memo-textarea::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #cdb4db, #ffc8dd);
  border-radius: 4px;
  border: 2px solid transparent;
  background-clip: padding-box;
}

.memo-textarea::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #b799c7, #ffb3cc);
  background-clip: padding-box;
}

.memo-textarea:focus {
  outline: none;
  background: white;
  box-shadow: 0 0 0 2px rgba(205, 180, 219, 0.3);
}

.memo-note-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  margin-top: 16px;
}

.memo-note-actions .save-btn,
.memo-note-actions .cancel-btn {
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
}

.memo-empty {
  text-align: center;
  padding-top: 2rem;
  color: #aaa;
  font-style: italic;
}

.memo-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.save-btn,
.cancel-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.save-btn {
  background: linear-gradient(135deg, #cdb4db, #b799c7);
  color: white;
  box-shadow: 0 4px 12px rgba(205, 180, 219, 0.4);
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(205, 180, 219, 0.5);
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #eee;
}

.dream-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.dream-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}

.dream-pagination {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  margin: 0 0 1rem 0;
}

.page-btn {
  padding: 0.35rem 0.45rem;
  border: 1px solid rgba(205, 180, 219, 0.4);
  border-radius: 10px;
  background: white;
  color: #8a6aa8;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn.icon svg {
  display: block;
}

.page-btn:hover:not(:disabled) {
  background: rgba(205, 180, 219, 0.22);
  color: #754f9b;
  transform: translateY(-1px);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

.page-indicator {
  font-family: "Dongle", sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #6a4a88;
  padding: 0rem 0.45rem;
  border-radius: 12px;
  background: rgba(205, 180, 219, 0.18);
  box-shadow: 0 4px 10px rgba(205, 180, 219, 0.15);
}

.lock-card {
  margin: 1.2rem auto 0;
  padding:11.4rem 2.2rem;
  border-radius: 32px;
  border: 1px solid rgba(205, 180, 219, 0.32);
  background: radial-gradient(circle at 18% 18%, rgba(255, 200, 221, 0.22), transparent 40%),
    radial-gradient(circle at 82% 32%, rgba(162, 210, 255, 0.22), transparent 45%),
    linear-gradient(135deg, rgba(205, 180, 219, 0.2), rgba(255, 200, 221, 0.18), rgba(162, 210, 255, 0.18));
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.6rem;
  box-shadow: 0 18px 40px rgba(205, 180, 219, 0.24);
  text-align: center;
}

.lock-text {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  align-items: center;
}

.lock-caption {
  display: flex;
  flex-direction: column;
  gap: 1.45rem;
  align-items: center;
}

.lock-caption .pill {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.55rem 1rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: #7a5fa3;
  font-weight: 800;
  font-size: 0.92rem;
  box-shadow: 0 12px 24px rgba(205, 180, 219, 0.24);
}

.lock-caption .caption-main {
  font-weight: 800;
  font-size: 1.1rem;
  color: #6f4d95;
  background: rgba(255, 255, 255, 0.85);
  padding: 0.8rem 1.1rem;
  border-radius: 16px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7), 0 10px 24px rgba(205, 180, 219, 0.18);
}

.lock-caption .caption-sub {
  font-size: 0.98rem;
  color: #8a6aa8;
  font-weight: 700;
}

.dream-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid rgba(205, 180, 219, 0.15);
}

.dream-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 16px rgba(205, 180, 219, 0.2);
  border-color: rgba(205, 180, 219, 0.3);
}

.dream-item svg {
  color: #ccc;
  transition: color 0.2s;
}

.dream-item:hover svg {
  color: #b799c7;
}

.dream-date-badge {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.5rem;
  color: white;
  flex-shrink: 0;
  font-family: "Dongle", sans-serif;
}

.dream-date-badge.color-purple {
  background: linear-gradient(135deg, #cdb4db, #b799c7);
}

.dream-date-badge.color-pink {
  background: linear-gradient(135deg, #ffc8dd, #ffb3cc);
}

.dream-date-badge.color-blue {
  background: linear-gradient(135deg, #a2d2ff, #8bb8e8);
}

.dream-info {
  flex: 1;
  min-width: 0;
}

.dream-info h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  color: #333;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dream-info p {
  margin: 0;
  font-size: 0.85rem;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  text-align: center;
  padding: 2.5rem 1rem;
  color: #aaa;
}

.empty-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 0.75rem;
}

.empty-icon svg {
  width: 48px;
  height: 48px;
  stroke: #cdb4db;
}

.empty-state p {
  margin: 0;
  font-size: 0.95rem;
}

/* 반응형 */
@media (max-width: 1040px) {
  .stat-content {
    flex-direction: column;
  }
}

@media (max-width: 900px) {
  .monthly-analysis-content {
    grid-template-columns: 1fr;
  }

  .right-column {
    padding-left: 0;
    border-left: none;
  }

  .right-column .memo-section {
    max-height: none;
  }

  .stat-content {
    flex-direction: row;
  }
}

@media (max-width: 768px) {
  .monthly-analysis-card {
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

  .current-month {
    font-size: 1.6rem;
    min-width: 140px;
  }

  .stats-grid {
    flex-direction: column;
    gap: 0.6rem;
  }

  .stat-card {
    padding: 0.9rem 1rem;
    gap: 2rem;
  }

  .stat-icon-wrap {
    width: 42px;
    height: 42px;
  }

  .stat-icon-wrap svg {
    width: 22px;
    height: 22px;
  }

  .stat-value {
    font-size: 2.5rem;
  }

  .stat-label {
    font-size: 0.9rem;
  }

  .section-card {
    padding: 1.25rem;
  }

  .section-title {
    font-size: 1.5rem;
  }
}
</style>
