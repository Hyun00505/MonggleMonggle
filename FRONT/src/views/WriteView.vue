<script setup>
import { onMounted, watch, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { useAuthStore } from "../stores/authStore";
import { formatDateKey, isTodayDate, isFutureDate } from "../utils/dateUtils";

const router = useRouter();
const route = useRoute();
const dreamEntriesStore = useDreamEntriesStore();
const authStore = useAuthStore();
const { currentUser } = storeToRefs(authStore);
const { dreamTitle, dreamContent, formattedSelectedDate, showAnalysisOption, selectedDate, selectedEmotion, hasExistingResult, posts } = storeToRefs(dreamEntriesStore);
const isCoinDepleted = computed(() => (currentUser.value?.coin ?? 0) <= 0);

// 지난 달 여부 확인 (현재 달 이전이면 true)
const isPastMonth = computed(() => {
  if (!selectedDate.value) return false;
  const today = new Date();
  const currentYear = today.getFullYear();
  const currentMonth = today.getMonth();
  const selectedYear = selectedDate.value.getFullYear();
  const selectedMonth = selectedDate.value.getMonth();
  
  // 선택된 날짜가 현재 년월보다 이전이면 지난 달
  return selectedYear < currentYear || 
         (selectedYear === currentYear && selectedMonth < currentMonth);
});

const { saveDream, deleteDream, setEmotion, enableEditMode, resetWriteState, setSelectedDateWithResult, fetchDreamsByMonth, validateRequiredFields } = dreamEntriesStore;

const emotions = [
  { value: 1, label: "매우 나쁨", icon: "😫" },
  { value: 2, label: "나쁨", icon: "😞" },
  { value: 3, label: "보통", icon: "😐" },
  { value: 4, label: "좋음", icon: "🙂" },
  { value: 5, label: "매우 좋음", icon: "🥰" },
];

function updateEmotion(event) {
  if (!showAnalysisOption.value) {
    setEmotion(Number(event.target.value));
  }
}

async function restoreFromQuery() {
  const dateStr = route.query.date;
  if (!dateStr) return;

  const [year, month, day] = String(dateStr).split("-").map(Number);
  const restoredDate = new Date(year, month - 1, day);

  if (Number.isNaN(restoredDate.getTime())) {
    router.replace({ name: "calendar" });
    return;
  }

  // 필요한 월 데이터가 없으면 로드 (과거 작성 여부 확인용)
  await ensureMonthData(restoredDate);

  if (!isAccessibleDate(restoredDate)) {
    alert("꿈 일기는 오늘 날짜에만 작성할 수 있습니다.");
    router.replace({ name: "calendar" });
    return;
  }

  // 이미 선택된 날짜와 다를 때만 갱신
  const currentKey = selectedDate.value ? formatDateKey(selectedDate.value) : null;
  if (currentKey !== dateStr) {
    await setSelectedDateWithResult(restoredDate);
  }
}

onMounted(async () => {
  await restoreFromQuery();

  if (selectedDate.value) {
    // 새로고침 등으로 쿼리가 없더라도 선택 날짜 검증
    if (!isAccessibleDate(selectedDate.value)) {
      alert("꿈 일기는 오늘 날짜에만 작성할 수 있습니다.");
      router.replace({ name: "calendar" });
      return;
    }
  }

  // 날짜가 여전히 없으면 캘린더로 이동
  if (!selectedDate.value && !route.query.date) {
    router.replace({ name: "calendar" });
  }
});

// 다른 화면에서 쿼리 date가 바뀌어도 반영되도록 감시
watch(
  () => route.query.date,
  async () => {
    await restoreFromQuery();
  }
);

function handleBack() {
  resetWriteState();
  router.push({ name: "calendar" });
}

function handleSave() {
  const validation = validateRequiredFields();
  if (!validation.valid) {
    alert(validation.message);
    return;
  }

  const saved = saveDream();
  if (!saved) {
    alert("날짜를 먼저 선택해주세요.");
  }
}

function handleDelete() {
  if (confirm("삭제하시면 기존의 꿈 일기, 꿈 해몽 결과, 꿈 이미지 등이 모두 삭제됩니다.\n정말 삭제하시겠습니까?")) {
    deleteDream();
    router.push({ name: "calendar" });
  }
}

function handleEdit() {
  if(!confirm("수정하시면 기존의 꿈 해몽 결과, 꿈 이미지 등이 모두 삭제됩니다.\n정말 수정하시겠습니까?")) {
    return;
  }
  enableEditMode();
}

function handleAnalysis() {
  // 코인 0인데 ai 해몽 버튼 클릭 햇을 때 
  if (isCoinDepleted.value) {
    alert("오늘 사용 가능한 AI 티켓을 모두 사용했습니다.");
    return;
  }

  // 현재 날짜를 쿼리 파라미터로 전달
  const dateKey = route.query.date || formatDateKey(selectedDate.value);
  sessionStorage.setItem("analysisRequestedDate", dateKey);
  router.push({ name: "loading", query: { date: dateKey } });
}

// 해몽 결과 보기
function handleViewResult() {
  const dateKey = route.query.date || formatDateKey(selectedDate.value);
  router.push({ name: "analysis", query: { date: dateKey } });
}

function hasPostForDate(date) {
  const key = formatDateKey(date);
  return !!posts.value[key];
}

function isAccessibleDate(date) {
  if (isFutureDate(date)) return false; // 미래는 작성 불가
  if (isTodayDate(date)) return true; // 오늘은 작성 가능
  // 과거는 작성된 일기만 허용
  return hasPostForDate(date);
}

async function ensureMonthData(date) {
  const key = formatDateKey(date);
  if (posts.value[key]) return;
  const y = date.getFullYear();
  const m = date.getMonth() + 1;
  await fetchDreamsByMonth(y, m);
}
</script>

<template>
  <div class="view-wrapper write-view">
    <div class="write-card">
      <div class="main-content">
        <div class="card-header">
          <button @click="handleBack" class="icon-btn" aria-label="뒤로가기">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 12H5M12 19l-7-7 7-7" />
            </svg>
          </button>
          <h3>{{ formattedSelectedDate }}</h3>
          <div class="spacer"></div>
        </div>

        <div class="divider"></div>

        <div class="form-content">
          <input v-model="dreamTitle" type="text" placeholder="꿈 제목을 입력해주세요" class="title-input" :disabled="showAnalysisOption" />

          <div class="divider"></div>

          <textarea v-model="dreamContent" placeholder="어떤 꿈을 꾸셨나요?" class="content-input" :disabled="showAnalysisOption"></textarea>
        </div>
      </div>

      <div class="side-actions">
        <div class="emotion-selector" :class="{ 'read-only': showAnalysisOption }">
          <p class="emotion-label">꿈 속의 나</p>

          <div class="current-emotion-display">
            <transition name="scale" mode="out-in">
              <div :key="selectedEmotion || 'none'" class="emoji-wrapper">
                <span class="main-emoji">
                  {{ selectedEmotion ? emotions.find((e) => e.value === selectedEmotion)?.icon : "🤔" }}
                </span>
                <span class="main-label">
                  {{ selectedEmotion ? emotions.find((e) => e.value === selectedEmotion)?.label : "선택해주세요" }}
                </span>
              </div>
            </transition>
          </div>

          <div class="slider-container">
            <input
              type="range"
              min="1"
              max="5"
              step="1"
              :value="selectedEmotion || 3"
              @input="updateEmotion"
              class="emotion-range"
              :disabled="showAnalysisOption"
              :style="{ backgroundSize: ((selectedEmotion || 3) - 1) * 25 + '% 100%' }"
            />
            <div class="range-marks">
              <span v-for="n in 5" :key="n" class="mark" :class="{ active: selectedEmotion === n }"></span>
            </div>
          </div>
        </div>

        <transition name="fade" mode="out-in">
          <!-- 작성 중일 때: 저장 버튼 -->
          <div v-if="!showAnalysisOption" key="save-mode" class="button-group">
            <button @click="handleSave" class="action-btn save-btn" aria-label="꿈 기록 저장">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                <polyline points="17 21 17 13 7 13 7 21"></polyline>
                <polyline points="7 3 7 8 15 8"></polyline>
              </svg>
              <span class="label">작성 완료</span>
            </button>
          </div>

          <!-- 작성 완료 시: 수정, 삭제, 분석 버튼 -->
          <div v-else key="view-mode" class="button-group">
            <!-- 지난 달이 아닐 때만 수정 버튼 표시 -->
            <button v-if="!isPastMonth" @click="handleEdit" class="action-btn edit-btn" aria-label="꿈 기록 수정">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
              </svg>
              <span class="label">수정하기</span>
            </button>

            <!-- 지난 달이 아닐 때만 삭제 버튼 표시 -->
            <button v-if="!isPastMonth" @click="handleDelete" class="action-btn delete-btn" aria-label="꿈 기록 삭제">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                <line x1="10" y1="11" x2="10" y2="17"></line>
                <line x1="14" y1="11" x2="14" y2="17"></line>
              </svg>
              <span class="label">삭제하기</span>
            </button>
            <!-- 해몽 결과가 있을 때: 결과 보기 버튼 (지난 달도 조회 가능) -->
              <button v-if="hasExistingResult" @click="handleViewResult" class="action-btn view-result-btn" aria-label="해몽 결과 보기">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
                <span class="label">결과 보기</span>
              </button>
              
              <!-- 지난 달이 아닐 때만 AI 꿈해몽 버튼 표시 -->
              <button v-if="!isPastMonth" @click="handleAnalysis" class="action-btn analysis-btn" :class="{ disabled: (currentUser?.coin ?? 0) <= 0 }">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="sparkle-icon">
                  <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"></path>
                  <path d="M4.5 4.5L5.5 6.5L6.5 4.5L8.5 3.5L6.5 2.5L5.5 0.5L4.5 2.5L2.5 3.5L4.5 4.5Z" fill="currentColor" stroke="none" class="twinkle"></path>
                  <path d="M19.5 19.5L20.5 21.5L21.5 19.5L23.5 18.5L21.5 17.5L20.5 15.5L19.5 17.5L17.5 18.5L19.5 19.5Z" fill="currentColor" stroke="none" class="twinkle delay-1"></path>
                </svg>
                <span class="label">AI 꿈해몽</span>
              </button>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.write-card {
  background: white;
  border-radius: 40px;
  padding: 2.5rem;
  width: 100%;
  max-width: 700px;
  min-height: 700px;
  box-shadow: 0 20px 60px rgba(100, 100, 200, 0.15);
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* 데스크탑에서 가로 배치 */
@media (min-width: 768px) {
  .write-card {
    flex-direction: row;
    max-width: 900px;
    padding-right: 1.5rem; /* 버튼 영역 확보 */
  }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.side-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  border-top: 1px solid #eee;
  /* gap: 1.5rem; */
}

.emotion-selector {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  width: 100%;
  background: #fff;
  border-radius: 20px;
  margin-top: 1rem;
}

.current-emotion-display {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 모바일에서 emotion display 크기 축소 */
@media (max-width: 767px) {
  .current-emotion-display {
    height: 60px;
  }

  .main-emoji {
    font-size: 2.5rem !important;
  }

  .main-label {
    font-size: 0.8rem !important;
  }
}

.emoji-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.main-emoji {
  font-size: 3rem;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1));
  animation: bounce 2s infinite ease-in-out;
}

.main-label {
  font-size: 0.9rem;
  font-weight: 700;
  color: #555;
}

.emotion-label {
  color: #555;
  font-weight: 700;
}

.slider-container {
  width: 100%;
  position: relative;
  height: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

@media (max-width: 767px) {
  .slider-container {
    max-width: 280px;
    margin-bottom: 10px;
  }
}

.emotion-range {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  height: 8px;
  border-radius: 5px;
  background: #e0e0e0;
  background-image: linear-gradient(90deg, #ffc8dd, #cdb4db);
  background-repeat: no-repeat;
  outline: none;
  cursor: pointer;
  z-index: 2;
}

.emotion-range:disabled {
  cursor: default;
  opacity: 0.7;
}

.emotion-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fff;
  border: 4px solid #cdb4db;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s;
}

.emotion-range:not(:disabled)::-webkit-slider-thumb:hover {
  transform: scale(1.2);
}

.emotion-range::-moz-range-thumb {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fff;
  border: 4px solid #cdb4db;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s;
}

.range-marks {
  position: absolute;
  top: 50%;
  left: 10px;
  right: 10px;
  transform: translateY(-50%);
  display: flex;
  justify-content: space-between;
  pointer-events: none;
  z-index: 1;
}

.mark {
  width: 8px;
  height: 8px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.mark.active {
  background: #cdb4db;
}

.scale-enter-active,
.scale-leave-active {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.5);
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.button-group {
  display: flex;
  flex-direction: row;
  gap: 1rem;
  width: 100%;
}

@media (min-width: 768px) {
  .side-actions {
    flex-direction: column;
    width: 120px;
    justify-content: flex-start; /* flex-end -> flex-start */
    border-top: none;
    border-left: 1px solid #eee;
    padding-left: 1.5rem;
  }

  .emotion-selector {
    margin-top: 0;
    margin-bottom: 2rem;
  }

  .button-group {
    flex-direction: column;
    gap: 1rem;
    width: 100%;
    margin-top: auto; /* 버튼들을 아래로 밀어줌 */
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0.5rem;
}

.card-header h3 {
  font-size: 1.2rem;
  color: #333;
  font-weight: 700;
  font-family: "Nunito", sans-serif;
}

.spacer {
  width: 24px;
}

.divider {
  width: 100%;
  border-top: 2px dashed #ce93d8;
  margin: 1rem 0;
  opacity: 0.6;
}

.form-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title-input {
  font-size: 1.2rem;
  font-weight: bold;
  border: none;
  outline: none;
  color: #333;
  width: 100%;
  padding: 0.5rem 0;
  background: transparent;
}

.title-input:disabled {
  color: #666;
  cursor: default;
}

.title-input::placeholder {
  color: #ccc;
}

.content-input {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-size: 1.2rem;
  line-height: 1.6;
  color: #555;
  font-family: "Nunito", sans-serif;
  padding-top: 0.5rem;
  background: transparent;
  min-height: 300px;
  scrollbar-width: thin;
  scrollbar-color: #cdb4db transparent;
}

.content-input::-webkit-scrollbar {
  width: 4px;
}

.content-input::-webkit-scrollbar-track {
  background: transparent;
}

.content-input::-webkit-scrollbar-thumb {
  background-color: #cdb4db;
  border-radius: 4px;
  border: none;
}

.content-input::-webkit-scrollbar-thumb:hover {
  background-color: #b39ddb;
}

@media (max-width: 767px) {
  .content-input {
    font-size: 1.1rem;
    min-height: 327px;
  }
}

.content-input:disabled {
  color: #666;
  cursor: default;
}

/* 버튼 스타일 */
.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  height: 100px;
  font-weight: 700;
  font-size: 0.9rem;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
}

.save-btn {
  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);
  color: black;
}

.edit-btn {
  background: #f0f0f0;
  color: #555;
}

.delete-btn {
  background: transparent;
  color: #b0bec5;
  border: 1px solid #eceff1;
}

.analysis-btn {
  background: linear-gradient(135deg, #ab47bc, #1a237e);
  color: #ffe082;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 224, 130, 0.3);
}

.analysis-btn.disabled {
  color: #8c8c8c;
  cursor: default;
}

.analysis-btn.disabled::before {
  display: none;
}


.analysis-btn.disabled .sparkle-icon {
  opacity: 0.4;
}

/* 해몽 결과 보기 버튼 - 청록/민트 톤 */
.view-result-btn {
  background: linear-gradient(135deg, #64b5f6, #4dd0e1);
  color: white;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.3);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.view-result-btn::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: shine 3s infinite;
}

.view-result-btn:hover {
  background: linear-gradient(135deg, #42a5f5, #26c6da);
  box-shadow: 0 8px 20px rgba(77, 208, 225, 0.35);
}

/* 반짝이는 효과 */
.analysis-btn::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 224, 130, 0.4), transparent);
  animation: shine 3s infinite;
}

.sparkle-icon {
  filter: drop-shadow(0 0 5px rgba(255, 224, 130, 0.8));
}

.twinkle {
  animation: twinkle 2s infinite ease-in-out;
  transform-origin: center;
}

.delay-1 {
  animation-delay: 1s;
}

@keyframes shine {
  0% {
    left: -100%;
  }
  20% {
    left: 100%;
  }
  100% {
    left: 100%;
  }
}

@keyframes twinkle {
  0%,
  100% {
    transform: scale(0.5);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}

.icon {
  width: 24px;
  height: 24px;
}

.label {
  text-align: center;
  line-height: 1.3;
}

/* 모바일에서의 버튼 조정 */
@media (max-width: 767px) {
  .button-group {
    display: flex;
    flex-direction: row;
    gap: 0.8rem;
    align-items: stretch;
  }

  /* 기본 버튼 스타일 */
  .action-btn {
    height: 56px;
    flex-direction: row;
    padding: 0;
    justify-content: center;
    align-items: center;
    border-radius: 16px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  }

  /* 수정, 삭제 버튼: 아이콘만 표시 */
  .edit-btn,
  .delete-btn {
    flex: 0 0 56px; /* 정사각형 형태 */
    width: 56px;
  }

  .edit-btn .label,
  .delete-btn .label {
    display: none;
  }

  /* 꿈 분석 버튼: 남은 공간 차지 */
  .analysis-btn,
  .view-result-btn {
    flex: 1;
    padding: 0 0.75rem;
    gap: 0.3rem;
  }

  .save-btn {
    flex: 1;
    padding: 0 1rem;
    gap: 0.5rem;
  }

  .icon {
    font-size: 1.2rem;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
