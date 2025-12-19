<template>
  <div class="monthly-analysis-card">
    <div class="card-header">
      <button @click="handleBack" class="icon-btn">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
      </button>
      <h2 class="page-title">
        월별 분석
        <span class="title-badge">Monthly</span>
      </h2>
      <button v-if="hasReportContent" class="report-btn" @click="openReport" :disabled="isLockedMonth" aria-label="월별 분석 리포트 열기">
        <span v-if="isLetterUnread" class="report-badge" aria-label="새 편지 도착">1</span>
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 8l9 6 9-6" />
          <path d="M5 19h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2z" />
          <path d="M3 8l9 6 9-6" />
        </svg>
        <span>우체통</span>
      </button>
      <div class="header-actions">
        <button @click="handleClose" class="icon-btn" aria-label="닫기">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
        </button>
      </div>
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
        <MonthlyStatsCard :total-dreams="monthlyStats.totalDreams" :streak="monthlyStats.streak" :avg-emotion-score="avgEmotionScore" />
        <MonthlyDreamList :dreams="visibleDreams" :current-page="currentDreamPage" :total-pages="totalDreamPages" @select-dream="goToDream" @prev-page="prevDreamPage" @next-page="nextDreamPage" />
      </div>

      <!-- 오른쪽 컬럼 -->
      <div class="right-column">
        <MonthlyMemoSection ref="memoSectionRef" :memos="monthlyMemos" @add-memo="handleAddMemo" @edit-memo="handleEditMemo" @delete-memo="handleDeleteMemo" />
      </div>
    </div>

    <!-- 잠금 상태 -->
    <div v-else class="lock-card">
      <div class="lock-text">
        <div class="lock-caption">
          <div class="lock-emoji">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 11h18v6a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3v-6z" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              <path d="M14 7h2" />
            </svg>
          </div>
          <div class="caption-main">이 달의 꿈 기록은 {{ nextAvailableText }}에 열려요</div>
          <div class="caption-sub">조금만 더 기다려 주세요 ✨</div>
        </div>
      </div>
    </div>

    <!-- 월별 리포트 모달 -->
    <MonthlyReportModal ref="reportModalRef" :is-open="isReportOpen" :year="currentYear" :month="currentMonth" :is-locked="isLockedMonth" @close="closeReport" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { monthlyAnalysisService } from "../services/monthlyAnalysisService";

// 컴포넌트 임포트
import MonthlyStatsCard from "../components/monthly/MonthlyStatsCard.vue";
import MonthlyDreamList from "../components/monthly/MonthlyDreamList.vue";
import MonthlyMemoSection from "../components/monthly/MonthlyMemoSection.vue";
import MonthlyReportModal from "../components/monthly/MonthlyReportModal.vue";

const route = useRoute();
const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const { postedDates } = storeToRefs(dreamEntriesStore);

// 컴포넌트 ref
const memoSectionRef = ref(null);
const reportModalRef = ref(null);

// 현재 시간 (잠금 상태 계산용)
const now = ref(new Date());
let nowTimer = null;

// 기본값: 지난 달
const lastMonthDate = computed(() => new Date(now.value.getFullYear(), now.value.getMonth() - 1, 1));
const currentYear = ref(lastMonthDate.value.getFullYear());
const currentMonth = ref(lastMonthDate.value.getMonth() + 1);

// 리포트 모달 열림 상태
const isReportOpen = ref(false);

// 메모 상태
const monthlyMemos = ref([]);

// 감정 점수 상태
const avgEmotionScore = ref(null);

const isLockedMonth = computed(() => {
  const selectedKey = currentYear.value * 12 + currentMonth.value;
  const currentKey = now.value.getFullYear() * 12 + (now.value.getMonth() + 1);
  return selectedKey >= currentKey;
});

// 모달 컴포넌트에서 편지 상태 가져오기
const hasReportContent = computed(() => reportModalRef.value?.hasReportContent ?? false);
const isLetterUnread = computed(() => reportModalRef.value?.isLetterUnread ?? false);

const nextAvailableText = computed(() => {
  const nextMonthDate = new Date(currentYear.value, currentMonth.value, 1);
  const y = nextMonthDate.getFullYear();
  const m = String(nextMonthDate.getMonth() + 1).padStart(2, "0");
  return `${y}년 ${m}월 01일`;
});

// 색상 클래스 배열
const colorClasses = ["color-purple", "color-pink", "color-blue"];

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

// 라우터 쿼리 동기화
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

// 꿈 목록 페이지네이션
const dreamPageSize = 4;
const currentDreamPage = ref(1);
const totalDreamPages = computed(() => Math.max(1, Math.ceil(monthlyDreams.value.length / dreamPageSize)));
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

// 통계 계산
const monthlyStats = computed(() => {
  const total = monthlyDreams.value.length;
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

// 네비게이션
function handleBack() {
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
  router.push({ name: "write", query: { date: dateKey } });
}

// 리포트 모달
function openReport() {
  if (isLockedMonth.value) return;
  isReportOpen.value = true;
}

function closeReport() {
  isReportOpen.value = false;
}

// 메모 CRUD 핸들러
async function handleAddMemo(content) {
  try {
    await monthlyAnalysisService.saveMemo({
      year: currentYear.value,
      month: currentMonth.value,
      memoContent: content,
    });
    await loadMonthlyMemos();
  } catch (err) {
    console.error("메모 저장 실패:", err);
    alert(err?.response?.data?.message || "메모 저장에 실패했습니다. 다시 시도해주세요.");
  }
}

async function handleEditMemo({ id, content }) {
  try {
    await monthlyAnalysisService.saveMemo({
      year: currentYear.value,
      month: currentMonth.value,
      memoContent: content,
      memoId: id,
    });
    await loadMonthlyMemos();
  } catch (err) {
    console.error("메모 수정 실패:", err);
    alert(err?.response?.data?.message || "메모 수정에 실패했습니다. 다시 시도해주세요.");
  }
}

async function handleDeleteMemo(memoId) {
  try {
    await monthlyAnalysisService.deleteMemo(memoId);
    await loadMonthlyMemos();
  } catch (err) {
    console.error("메모 삭제 실패:", err);
    alert(err?.response?.data?.message || "메모 삭제에 실패했습니다. 다시 시도해주세요.");
  }
}

// 메모 로드
async function loadMonthlyMemos() {
  if (isLockedMonth.value) {
    monthlyMemos.value = [];
    return;
  }

  // 자식 컴포넌트 상태 초기화
  memoSectionRef.value?.resetState();

  try {
    const memos = await monthlyAnalysisService.getMemo(currentYear.value, currentMonth.value);
    monthlyMemos.value = sanitizeMemos(
      Array.isArray(memos)
        ? memos.map((m) => ({
            id: m.memoId,
            content: m.memoContent,
            createdAt: m.createdDate || m.updatedDate || new Date().toISOString(),
          }))
        : []
    );
  } catch (err) {
    if (err?.response?.status === 404) {
      monthlyMemos.value = [];
    } else {
      console.error("메모 조회 실패:", err);
    }
  }
}

function sanitizeMemos(memos) {
  if (!Array.isArray(memos)) return [];
  return memos.filter((memo) => memo?.content && memo.content.trim().length > 0);
}

// 꿈 데이터 로드
function loadMonthlyDreams() {
  if (isLockedMonth.value) return;
  dreamEntriesStore.fetchDreamsByMonth(currentYear.value, currentMonth.value);
}

// 월별 분석 데이터 로드 (avgEmotionScore 포함)
async function loadMonthlyAnalysis() {
  if (isLockedMonth.value) {
    avgEmotionScore.value = null;
    return;
  }

  try {
    const analysis = await monthlyAnalysisService.getMonthlyAnalysis(currentYear.value, currentMonth.value);
    avgEmotionScore.value = analysis?.avgEmotionScore ?? null;
  } catch (err) {
    if (err?.response?.status === 404) {
      avgEmotionScore.value = null;
    } else {
      console.error("월별 분석 조회 실패:", err);
      avgEmotionScore.value = null;
    }
  }
}

// 라이프사이클
onMounted(() => {
  syncFromRoute();
  updateRouteQuery();
  loadMonthlyMemos();
  loadMonthlyDreams();
  loadMonthlyAnalysis();

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

// 월 변경 시 데이터 갱신
watch([currentYear, currentMonth], () => {
  loadMonthlyMemos();
  loadMonthlyDreams();
  loadMonthlyAnalysis();
});
</script>

<style scoped>
.monthly-analysis-card {
  background: white;
  border-radius: 32px;
  padding: 1.5rem 2rem 2rem;
  width: 100%;
  max-width: 1100px;
  box-shadow: 0 16px 48px rgba(100, 100, 200, 0.12);
  animation: fadeSlideUp 0.5s ease;
  overflow: hidden;
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

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #888;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #f5f5f5;
  color: #555;
}

.header-actions {
  display: inline-flex;
  gap: 0.5rem;
  align-items: center;
}

.report-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.55rem 0.9rem;
  border: 1px solid var(--border-dashed-purple);
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(205, 180, 219, 0.16), rgba(162, 210, 255, 0.16));
  color: #6a4a88;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(205, 180, 219, 0.18);
  font-family: "Dongle", sans-serif !important;
  font-weight: 700;
  font-size: 1.3rem;
  position: absolute !important;
  right: 53px;
  width: 90px;
  position: relative;
}

@media (max-width: 768px) {
  .report-btn {
    padding: 0.55rem;
    width: 42px;
    height: 42px;
    right: 52px;
    justify-content: center;
  }
  .report-btn span {
    display: none;
  }
}

.report-btn svg {
  stroke: currentColor;
}

.report-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background: #ff7bb1;
  box-shadow: 0 0 0 3px rgba(255, 123, 177, 0.25);
  color: #fff;
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.1rem;
  line-height: 18px;
  text-align: center;
  animation: badge-pulse 1.4s ease-in-out infinite;
}

@keyframes badge-pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 3px rgba(255, 123, 177, 0.25);
    opacity: 1;
  }
  50% {
    transform: scale(1.12);
    box-shadow: 0 0 0 4px rgba(255, 123, 177, 0.35);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 3px rgba(255, 123, 177, 0.25);
    opacity: 1;
  }
}

.report-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(205, 180, 219, 0.28);
  color: #4c2b7b;
}

.report-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
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
  overflow: visible;
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
  border-left: 1px solid var(--border-purple-light);
  min-width: 0;
  height: 100%;
}

.month-selector-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  padding: 1rem;
  background: var(--gradient-bg-15);
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
  color: var(--color-purple-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 8px var(--color-purple-20);
}

.month-nav-btn:hover {
  background: var(--color-purple);
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

.lock-card {
  margin: 1.2rem auto 0;
  padding: 11.4rem 2.2rem;
  border-radius: 32px;
  border: 1px solid rgba(205, 180, 219, 0.32);
  background: radial-gradient(circle at 18% 18%, rgba(255, 200, 221, 0.22), transparent 40%), radial-gradient(circle at 82% 32%, rgba(162, 210, 255, 0.22), transparent 45%),
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

.lock-emoji {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--gradient-purple-blue);
  display: grid;
  place-items: center;
  color: #fff;
}

.lock-caption {
  display: flex;
  flex-direction: column;
  gap: 1.45rem;
  align-items: center;
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

/* 반응형 */
@media (max-width: 900px) {
  .monthly-analysis-content {
    grid-template-columns: 1fr;
  }

  .right-column {
    padding-left: 0;
    border-left: none;
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
}
</style>
