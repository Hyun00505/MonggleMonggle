<template>
  <transition name="fade">
    <div v-if="isOpen" class="report-modal-overlay" @click.self="handleClose">
      <div class="report-modal">
        <div class="report-modal-header">
          <div class="report-mailbox">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2Z" />
              <path d="m4 8 7.4 4.63a2 2 0 0 0 2.2 0L21 8" />
              <path d="M4 16.5 9 13" />
              <path d="m20 16.5-5-3.5" />
            </svg>
          </div>
          <div>
            <div class="report-title">{{ year }}년 {{ month }}월 편지</div>
          </div>
          <button class="modal-close-btn" @click="handleClose" aria-label="리포트 닫기">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="report-letter">
          <div class="letter-top">
            <div class="letter-stamp">Monthly Letter</div>
            <div class="letter-tofrom">
              <div class="letter-line">
                <span>To.</span>
                {{ year }}년 {{ month }}월의 {{ receiverName }}
              </div>
              <div class="letter-line">
                <span>From.</span>
                몽글몽글
              </div>
            </div>
          </div>

          <div class="letter-body">
            <div v-if="isLoadingReport" class="letter-placeholder">편지를 준비하는 중이에요...</div>
            <div v-else-if="reportError" class="letter-error">{{ reportError }}</div>
            <div v-else-if="monthlyReport" class="letter-content" v-html="formattedReport"></div>
            <div v-else class="letter-placeholder">편지 내용을 여기에 담아 전달해 드릴게요.</div>
          </div>

          <div class="letter-footer">
            좋았던 꿈도, 조금 무서웠던 꿈도 모두 당신의 이야기예요. 다음 달에도 편지를 띄워요
            <svg class="footer-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79Z" />
            </svg>
          </div>

          <!-- ADMIN 전용: 다시 분석하기 버튼 -->
          <div v-if="isAdmin && !isLocked" class="admin-actions">
            <button
              class="regenerate-btn"
              :disabled="isLoadingReport || isGeneratingReport"
              @click="handleRegenerate"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
                <path d="M3 3v5h5" />
                <path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16" />
                <path d="M16 21h5v-5" />
              </svg>
              <span v-if="isGeneratingReport">분석 중...</span>
              <span v-else>다시 분석하기</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { storeToRefs } from "pinia";
import { useAuthStore } from "../../stores/authStore";
import { monthlyAnalysisService } from "../../services/monthlyAnalysisService";

// ADMIN 권한 체크

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  year: {
    type: Number,
    required: true,
  },
  month: {
    type: Number,
    required: true,
  },
  isLocked: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["close", "mark-read"]);

const authStore = useAuthStore();
const { currentUser, isAdmin } = storeToRefs(authStore);

// 리포트 상태
const monthlyReport = ref("");
const isLoadingReport = ref(false);
const isGeneratingReport = ref(false);
const reportError = ref("");

// 편지 읽음 상태 관리
const letterReadStatus = ref({});
const LETTER_READ_KEY = "monthlyReportRead";

// 생성 요청 중복 방지를 위한 세션 스토리지 키
const GENERATING_KEY = "monthlyReportGenerating";

// 현재 시간 (직전 달 판단용)
const now = ref(new Date());
let nowTimer = null;

// 생성 중 상태 확인 (세션 스토리지 기반)
function isCurrentlyGenerating() {
  try {
    const generating = sessionStorage.getItem(GENERATING_KEY);
    if (!generating) return false;
    const { year, month, timestamp } = JSON.parse(generating);
    // 5분 이상 경과한 요청은 만료 처리
    if (Date.now() - timestamp > 5 * 60 * 1000) {
      sessionStorage.removeItem(GENERATING_KEY);
      return false;
    }
    return year === props.year && month === props.month;
  } catch {
    return false;
  }
}

// 생성 중 상태 설정
function setGeneratingState(isGenerating) {
  try {
    if (isGenerating) {
      sessionStorage.setItem(GENERATING_KEY, JSON.stringify({
        year: props.year,
        month: props.month,
        timestamp: Date.now()
      }));
    } else {
      sessionStorage.removeItem(GENERATING_KEY);
    }
  } catch {
    // 세션 스토리지 오류 무시
  }
}

// 직전 달인지 확인 (편지 생성/조회는 직전 달만 가능)
const isPreviousMonth = computed(() => {
  const prevMonthDate = new Date(now.value.getFullYear(), now.value.getMonth() - 1, 1);
  return props.year === prevMonthDate.getFullYear() && props.month === prevMonthDate.getMonth() + 1;
});

// 편지 버튼 표시 여부
const hasReportContent = computed(() => {
  // 직전 달인 경우 항상 편지 버튼 표시 (로딩 중이거나 편지가 있거나 생성 가능)
  if (isPreviousMonth.value) {
    return true;
  }
  // 2달 이상 과거는 기존 편지가 있을 때만 표시 (조회만 가능, 생성 불가)
  return isLoadingReport.value || !!monthlyReport.value;
});

// 편지 포맷팅
const formattedReport = computed(() => (monthlyReport.value ? monthlyReport.value.replace(/\n/g, "<br />") : ""));

// 수신자 이름
const receiverName = computed(() => {
  const name = currentUser.value?.name;
  return name ? `${name} 님에게` : "나에게";
});

// 월 키 생성
function getMonthKey() {
  return `${props.year}-${String(props.month).padStart(2, "0")}`;
}

// 편지 읽음 상태 로드
function loadLetterReadStatus() {
  try {
    const stored = localStorage.getItem(LETTER_READ_KEY);
    letterReadStatus.value = stored ? JSON.parse(stored) : {};
  } catch {
    letterReadStatus.value = {};
  }
}

// 편지 읽음 상태 저장
function persistLetterReadStatus() {
  try {
    localStorage.setItem(LETTER_READ_KEY, JSON.stringify(letterReadStatus.value));
  } catch (err) {
    console.error("편지 읽음 상태 저장 실패:", err);
  }
}

// 편지 읽지 않음 상태
const isLetterUnread = computed(() => {
  const key = getMonthKey();
  return isPreviousMonth.value && hasReportContent.value && !letterReadStatus.value[key];
});

// 모달 닫기
function handleClose() {
  emit("close");
}

// 모달 열릴 때 읽음 상태 마킹
function markAsRead() {
  const key = getMonthKey();
  letterReadStatus.value = { ...letterReadStatus.value, [key]: true };
  persistLetterReadStatus();
  emit("mark-read", key);
}

// 월간 리포트 생성 시도
async function tryGenerateReport() {
  // 이미 다른 탭/창에서 생성 중이면 조회로 대체
  if (isCurrentlyGenerating()) {
    await retryFetchReport();
    return;
  }

  isGeneratingReport.value = true;
  setGeneratingState(true);

  try {
    const generated = await monthlyAnalysisService.generateMonthlyAnalysis(props.year, props.month);
    monthlyReport.value = generated?.monthlyReport || "";

    if (!monthlyReport.value) {
      // 생성 요청은 성공했지만 결과가 없으면 재시도
      await retryFetchReport();
    }
  } catch (genErr) {
    // 중복 키 오류 (이미 다른 요청에서 생성됨) - 조회로 재시도
    if (genErr?.response?.status === 409 || genErr?.response?.data?.message?.includes("Duplicate")) {
      await retryFetchReport();
    } else {
      reportError.value = genErr?.response?.data?.message || genErr.message || "월간 리포트를 생성하지 못했습니다.";
      monthlyReport.value = "";
    }
  } finally {
    isGeneratingReport.value = false;
    setGeneratingState(false);
  }
}

// 월간 리포트 조회 (직전 달이면 없을 시 생성, 과거 달은 조회만)
async function fetchMonthlyReport() {
  // 잠긴 달(현재 달 이후)이면 리포트 없음
  if (props.isLocked) {
    monthlyReport.value = "";
    reportError.value = "";
    return;
  }

  // 이미 로딩 중이거나 생성 중이면 중복 호출 방지 (로컬 상태 + 세션 스토리지)
  if (isLoadingReport.value || isGeneratingReport.value || isCurrentlyGenerating()) {
    return;
  }

  isLoadingReport.value = true;
  reportError.value = "";

  try {
    // 기존 편지가 있는지 조회
    const response = await monthlyAnalysisService.getMonthlyAnalysis(props.year, props.month);
    const report = response?.monthlyReport || "";

    if (report) {
      monthlyReport.value = report;
    } else if (isPreviousMonth.value) {
      // 직전 달인 경우에만 편지가 없으면 자동 생성 시도
      await tryGenerateReport();
    } else {
      // 2달 이상 과거는 편지가 없으면 그냥 비워둠 (생성 안 함)
      monthlyReport.value = "";
    }
  } catch (err) {
    if (err?.response?.status === 404) {
      // 편지가 없는 경우
      if (isPreviousMonth.value) {
        // 직전 달이면 생성 시도
        await tryGenerateReport();
      } else {
        // 2달 이상 과거는 생성 안 함
        monthlyReport.value = "";
      }
    } else {
      reportError.value = err?.response?.data?.message || err.message || "월간 리포트를 불러오지 못했습니다.";
      monthlyReport.value = "";
    }
  } finally {
    isLoadingReport.value = false;
  }
}

// 생성 후 결과 다시 조회 (polling)
async function retryFetchReport(retries = 3, delay = 2000) {
  for (let i = 0; i < retries; i++) {
    await new Promise((resolve) => setTimeout(resolve, delay));

    try {
      const response = await monthlyAnalysisService.getMonthlyAnalysis(props.year, props.month);
      const report = response?.monthlyReport || "";

      if (report) {
        monthlyReport.value = report;
        reportError.value = "";
        return;
      }
    } catch (err) {
      console.error(`[retryFetchReport] 오류:`, err);
      // 아직 생성 중이면 계속 재시도
      if (err?.response?.status !== 404) {
        break;
      }
    }
  }

  // 재시도 후에도 결과가 없으면 에러 메시지
  if (!monthlyReport.value) {
    reportError.value = "편지 생성에 시간이 걸리고 있어요. 잠시 후 다시 확인해주세요. ⏳";
  }
}

// 상태 리셋
function resetState() {
  monthlyReport.value = "";
  isLoadingReport.value = false;
  isGeneratingReport.value = false;
  reportError.value = "";
}

// ADMIN 전용: 강제 재분석
async function handleRegenerate() {
  if (!isAdmin.value) return;
  
  const confirmed = confirm("기존 분석 결과를 덮어쓰고 새로 분석하시겠습니까?");
  if (!confirmed) return;

  isGeneratingReport.value = true;
  setGeneratingState(true);
  reportError.value = "";
  monthlyReport.value = ""; // 기존 내용 초기화

  try {
    const generated = await monthlyAnalysisService.generateMonthlyAnalysis(props.year, props.month);
    const report = generated?.monthlyReport || "";
    
    if (report) {
      monthlyReport.value = report;
    } else {
      // 생성 요청은 성공했지만 결과가 비어있으면 조회로 재시도
      await retryFetchReport(5, 3000); // 5번, 3초 간격으로 재시도
    }
  } catch (err) {
    console.error("[ADMIN] 재분석 실패:", err);
    reportError.value = err?.response?.data?.message || err.message || "재분석에 실패했습니다.";
  } finally {
    isGeneratingReport.value = false;
    setGeneratingState(false);
  }
}

// 모달 열릴 때 읽음 상태 마킹
watch(
  () => props.isOpen,
  (newValue) => {
    if (newValue) {
      markAsRead();
    }
  }
);

// 월 변경 시 데이터 갱신
watch(
  [() => props.year, () => props.month],
  () => {
    resetState();
    fetchMonthlyReport();
  },
  { immediate: false }
);

// 라이프사이클
onMounted(() => {
  loadLetterReadStatus();
  fetchMonthlyReport();

  nowTimer = setInterval(() => {
    now.value = new Date();
  }, 60 * 1000);
});

onUnmounted(() => {
  if (nowTimer) {
    clearInterval(nowTimer);
  }
  // 컴포넌트 언마운트 시 진행 중인 상태 정리
  isLoadingReport.value = false;
  isGeneratingReport.value = false;
});

// 부모 컴포넌트에 상태 노출
defineExpose({
  hasReportContent,
  isLetterUnread,
  isLoadingReport,
  fetchMonthlyReport,
  resetState,
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.report-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  z-index: 50;
}

.report-modal {
  background: #fff;
  width: min(880px, 95vw);
  max-height: 90vh;
  overflow-y: auto;
  border-radius: 20px;
  box-shadow: 0 22px 48px rgba(0, 0, 0, 0.18);
  padding: 1.6rem;
  border: 1px solid var(--color-purple-35);
}

.report-modal-header {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  margin-bottom: 1.1rem;
}

.report-mailbox {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--gradient-purple-blue);
  display: grid;
  place-items: center;
  color: #fff;
}

.report-title {
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.8rem;
  color: #4c2b7b;
  line-height: 1.05;
}

.modal-close-btn {
  margin-left: auto;
  border: none;
  background: none;
  cursor: pointer;
  color: #aaa;
  padding: 6px;
  border-radius: 10px;
}

.modal-close-btn:hover {
  background: #f6f3f9;
  color: #555;
}

.report-letter {
  margin-top: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 16px;
  background: #faf7ff;
  border: 1px solid var(--color-purple-35);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6), 0 14px 30px rgba(205, 180, 219, 0.14);
  position: relative;
  overflow: hidden;
}

.report-letter::before {
  content: "";
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(to bottom, transparent, transparent 26px, rgba(205, 180, 219, 0.12) 27px, rgba(205, 180, 219, 0.12) 28px);
  pointer-events: none;
}

.letter-top {
  display: flex;
  gap: 1rem;
  align-items: center;
  position: relative;
  z-index: 1;
  margin-bottom: 1rem;
  justify-content: space-between;
}

.letter-stamp {
  padding: 0.55rem 0.75rem;
  border-radius: 12px;
  background: var(--gradient-report-badge);
  color: #5a3d7a;
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.3rem;
  box-shadow: 0 6px 14px rgba(205, 180, 219, 0.26);
}

.letter-tofrom {
  display: flex;
  flex-direction: column;
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.35rem;
  color: #5c4c79;
}

.letter-line span {
  color: #a26fb8;
  font-weight: 800;
  margin-right: 0.4rem;
}

.letter-body {
  position: relative;
  z-index: 1;
  background: rgba(250, 247, 255, 0.9);
  border-radius: 12px;
  border: 1px dashed var(--color-purple-35);
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.letter-footer {
  margin-top: 0.9rem;
  text-align: right;
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.4rem;
  color: #7a5fa3;
  position: relative;
  z-index: 1;
}

.footer-icon {
  vertical-align: middle;
  color: #7a5fa3;
}

.letter-placeholder {
  min-height: 160px;
  padding: 1rem;
  background: rgba(252, 249, 255, 0.96);
  border-radius: 10px;
  color: #5c4c79;
  font-weight: 700;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.letter-content {
  min-height: 160px;
  padding: 1rem;
  white-space: pre-wrap;
  line-height: 1.6;
  color: #4b3b67;
}

.letter-error {
  min-height: 160px;
  padding: 1rem;
  background: rgba(255, 235, 230, 0.9);
  border-radius: 10px;
  color: #c0392b;
  font-weight: 700;
  line-height: 1.6;
}

.report-modal::-webkit-scrollbar {
  width: 8px;
}

.report-modal::-webkit-scrollbar-thumb {
  background: var(--color-purple-60);
  border-radius: 4px;
}

/* ADMIN 전용 액션 */
.admin-actions {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
  position: relative;
  z-index: 1;
}

.regenerate-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border: 1px solid #e74c3c;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(231, 76, 60, 0.1), rgba(192, 57, 43, 0.1));
  color: #c0392b;
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.2s;
}

.regenerate-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(231, 76, 60, 0.2), rgba(192, 57, 43, 0.2));
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.2);
}

.regenerate-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.regenerate-btn svg {
  stroke: currentColor;
}
</style>
