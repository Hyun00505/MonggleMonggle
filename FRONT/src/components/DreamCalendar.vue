<template>
  <div class="calendar-wrapper">
    <h1 class="service-title">몽글몽글</h1>

    <div class="calendar-card">
      <!-- Calendar Grid -->
      <div class="calendar-body">
        <div class="nav-row">
          <button @click="changeMonth(-1)" class="nav-btn" aria-label="Previous Month">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M15 19L8 12L15 5" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>

          <div class="header-selects">
            <div class="select-wrapper">
              <select v-model="currentDateModel" class="custom-select date-select">
                <option v-for="option in dateOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div>
          </div>

          <button @click="changeMonth(1)" class="nav-btn" aria-label="Next Month">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 5L16 12L9 19" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>
        </div>

        <!-- Wrapped in grid-wrapper for border -->
        <div class="grid-wrapper">
          <div class="calendar-grid">
            <div
              v-for="(day, index) in weekDays"
              :key="day"
              class="weekday-header"
              :class="{
                'sun-header': index === 0,
                'sat-header': index === 6,
              }"
            >
              {{ day }}
            </div>

            <div v-for="n in paddingDays" :key="'pad-' + n" class="day-cell padding"></div>

            <div
              v-for="day in daysInMonth"
              :key="day"
              class="day-cell"
              :class="{
                today: isToday(day),
                'has-post': getPost(day),
                'sun-col': new Date(currentDate.getFullYear(), currentDate.getMonth(), day).getDay() === 0,
                'sat-col': new Date(currentDate.getFullYear(), currentDate.getMonth(), day).getDay() === 6,
                'weekday-col': new Date(currentDate.getFullYear(), currentDate.getMonth(), day).getDay() !== 0 && new Date(currentDate.getFullYear(), currentDate.getMonth(), day).getDay() !== 6,
                'warning-active': isWarningVisible(day),
              }"
              @click="handleDateClick(day)"
            >
              <!-- Mini Warning Bubble -->
              <transition name="pop">
                <div v-if="isWarningVisible(day)" class="mini-warning-bubble">
                  {{ warningMessage }}
                </div>
              </transition>

              <!-- Hide number if post exists -->
              <span v-if="!getPost(day)" class="day-number">{{ day }}</span>

              <!-- Star Indicator (Centered and Larger) -->
              <!-- 해몽 완료: 행운의 색상 / 해몽 안함: 흰색 -->
              <div v-if="getPost(day)" class="star-indicator" :style="{ color: getStarColor(day) }">
                <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                    stroke="currentColor"
                    stroke-width="3"
                    stroke-linejoin="round"
                    stroke-linecap="round"
                  />
                </svg>
              </div>
            </div>

            <!-- Fixed size: Fill remaining cells -->
            <div v-for="n in trailingPaddingDays" :key="'trail-' + n" class="day-cell padding"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";

const props = defineProps({
  initialDate: {
    type: Date,
    default: () => new Date(),
  },
  postedDates: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["date-click", "month-change"]);

const currentDate = ref(new Date(props.initialDate));
const weekDays = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];

watch(currentDate, () => {
  clearWarning();
});

// 부모에서 initialDate가 바뀌면 현재 달도 동기화
watch(
  () => props.initialDate,
  (val) => {
    if (val) {
      currentDate.value = new Date(val);
    }
  }
);

const showWarning = ref(false);
const warningMessage = ref("");
const warningDay = ref(null);
const warningDateKey = ref(null);
let warningTimeout = null;

function getDateKey(day) {
  const year = currentDate.value.getFullYear();
  const month = String(currentDate.value.getMonth() + 1).padStart(2, "0");
  const dayStr = String(day).padStart(2, "0");
  return `${year}-${month}-${dayStr}`;
}

function clearWarning() {
  if (warningTimeout) {
    clearTimeout(warningTimeout);
    warningTimeout = null;
  }
  showWarning.value = false;
  warningDay.value = null;
  warningDateKey.value = null;
}

function triggerWarning(day, message) {
  warningMessage.value = message;
  warningDay.value = day;
  warningDateKey.value = getDateKey(day);
  showWarning.value = true;

  if (warningTimeout) clearTimeout(warningTimeout);

  warningTimeout = setTimeout(() => {
    showWarning.value = false;
    warningDay.value = null;
  }, 1000);
}

// Generate date options (Year. Month)
// Range: 10 years back to 10 years forward
const dateOptions = computed(() => {
  const options = [];
  const currentY = new Date().getFullYear();
  const startYear = currentY - 10;
  const endYear = currentY + 10;

  for (let year = startYear; year <= endYear; year++) {
    for (let month = 0; month < 12; month++) {
      // Format: 2025. 01
      const monthStr = String(month + 1).padStart(2, "0");
      options.push({
        label: `${year}. ${monthStr}`,
        value: `${year}-${month}`, // value for unique identification
      });
    }
  }
  return options;
});

// Model for the single select box
const currentDateModel = computed({
  get: () => {
    const year = currentDate.value.getFullYear();
    const month = currentDate.value.getMonth();
    return `${year}-${month}`;
  },
  set: (value) => {
    const [year, month] = value.split("-").map(Number);
    const newDate = new Date(currentDate.value);
    newDate.setFullYear(year);
    newDate.setMonth(month);
    currentDate.value = newDate;
    emitMonthChange();
    clearWarning();
  },
});

const daysInMonth = computed(() => {
  const year = currentDate.value.getFullYear();
  const month = currentDate.value.getMonth() + 1;
  return new Date(year, month, 0).getDate();
});

const paddingDays = computed(() => {
  const year = currentDate.value.getFullYear();
  const month = currentDate.value.getMonth();
  return new Date(year, month, 1).getDay();
});

// Always ensure 6 rows (42 cells) for fixed height
const trailingPaddingDays = computed(() => {
  const totalCells = 42;
  const usedCells = paddingDays.value + daysInMonth.value;
  return totalCells - usedCells;
});

function changeMonth(delta) {
  const newDate = new Date(currentDate.value);
  newDate.setMonth(newDate.getMonth() + delta);
  currentDate.value = newDate;
  emitMonthChange();
  clearWarning();
}

function emitMonthChange() {
  emit("month-change", {
    year: currentDate.value.getFullYear(),
    // fetchDreamsByMonth가 1~12 값을 기대하므로 +1
    month: currentDate.value.getMonth() + 1,
  });
}

function handleDateClick(day) {
  const selectedDate = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth(), day);

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  if (selectedDate > today) {
    triggerWarning(day, "미래의 꿈은 아직 꿀 수 없어요! 🌙");
    return;
  }

  if (selectedDate < today && !getPost(day)) {
    triggerWarning(day, "이 날은 기록된 꿈이 없어요 ☁️");
    return;
  }

  emit("date-click", selectedDate);
}

function getPost(day) {
  const dateKey = getDateKey(day);
  return props.postedDates[dateKey];
}

// 별 색상 결정: 해몽 완료면 행운의 색상, 아니면 흰색
function getStarColor(day) {
  const post = getPost(day);
  if (!post) return "#000000";

  // 해몽 결과가 있고 색상이 지정되어 있으면 그 색상 사용
  if (post.hasResult && post.color && post.color !== "#000000") {
    return post.color;
  }

  // 해몽 안 했으면 검정색
  return "#000000";
}

function isWarningVisible(day) {
  return showWarning.value && warningDateKey.value === getDateKey(day);
}

function isToday(day) {
  const today = new Date();
  return day === today.getDate() && currentDate.value.getMonth() === today.getMonth() && currentDate.value.getFullYear() === today.getFullYear();
}
</script>

<style scoped>
.calendar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 700px;
}

/* Removed .title-cloud styles */

.service-title {
  font-family: "Dongle", sans-serif;
  font-size: 5rem;
  font-weight: 700;
  /* Text Gradient: Pastel Purple -> Pink -> Blue (Original Lighter Version) */
  background: linear-gradient(to right, #cdb4db, #ffc8dd, #a2d2ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: transparent; /* Fallback */

  /* Restore spacing */
  margin: 0 0 2rem 0;
  line-height: 1.2;
  letter-spacing: 2px;

  /* Restore shadow for dark background visibility */
  filter: drop-shadow(0 2px 4px rgba(255, 255, 255, 0.3));
}

.calendar-card {
  position: relative;
  background: #ffffff;
  border-radius: 40px;
  padding: 1.2rem;
  width: 100%;
  box-shadow: 0 20px 60px rgba(100, 100, 200, 0.15);
  font-family: "Nunito", sans-serif;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Removed text title styles */

.calendar-body {
  width: 100%;
}

.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.5rem;
}

.header-selects {
  display: flex;
  align-items: center;
}

.select-wrapper {
  position: relative;
}

/* Custom Select Styles */
.custom-select {
  appearance: none;
  background-color: transparent;
  border: none;
  font-family: "Nunito", sans-serif !important;
  font-weight: 800;
  color: #333;
  cursor: pointer;
  padding: 8px 20px;
  border-radius: 12px;
  transition: all 0.2s ease;
  text-align: center;
  outline: none;
  font-size: 2.2rem; /* Increased from 1.8rem */
}

.custom-select:hover {
  background-color: #f5f5f5;
}

/* Remove custom arrow */
.select-wrapper::after {
  display: none;
}

.nav-btn {
  background: none;
  border: none;
  color: #aaa;
  cursor: pointer;
  transition: color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
}

.nav-btn:hover {
  color: #8ec5fc;
  background-color: #f0f4f8;
  border-radius: 50%;
}

/* Grid Wrapper with Outer Border - Removed for floating circles */
.grid-wrapper {
  border: none;
  border-radius: 0;
  overflow: visible;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px; /* Spacing between circles */
  background-color: transparent;
}

/* Sunday Column (1st column) - Handled by dynamic classes */
/* Saturday Column (7th column) - Handled by dynamic classes */

.weekday-header {
  background-color: #fafafa; /* Very Light Gray */
  font-size: 1rem;
  color: #777;
  font-weight: 800;
  padding: 8px 0;
  border-radius: 20px; /* Capsule shape */
  margin-bottom: 5px;
}

.sun-header {
  background-color: #fff5f6; /* Very Pale Pink */
  color: #ff8a80;
}

.sat-header {
  background-color: #f2f9ff; /* Very Pale Blue */
  color: #82b1ff;
}

.day-cell {
  /* Circular cells */
  aspect-ratio: 1; /* Keeps cells square */
  width: 100%;
  height: auto;
  background-color: #faf5fc; /* Very Pale Purple */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  margin: 0;
  border-radius: 20%;
}

.sun-col {
  background-color: #fff5f6 !important; /* Very Pale Pink */
}

.sat-col {
  background-color: #f2f9ff !important; /* Very Pale Blue */
}

.day-cell.padding {
  background-color: transparent; /* Transparent for empty cells */
  cursor: default;
  box-shadow: none;
}

.day-cell:hover:not(.padding) {
  transform: scale(1.05);
  filter: brightness(0.95);
}

.day-cell.today:not(.has-post) {
  background: linear-gradient(135deg, #cdb4db, #ffc8dd, #a2d2ff); /* Gradient matching title */
  border: none;
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 200, 221, 0.3);
}

.day-cell.warning-active {
  z-index: 50 !important;
  /* Ensure transform doesn't create a clipping context that hides overflow if that was the issue, 
     but mainly z-index fixes stacking order against subsequent siblings */
}

/* Twinkling effect for today with no post */
/* .day-cell.today:not(.has-post) {
  animation: blink-shadow 2s infinite ease-in-out;
} */

@keyframes blink-shadow {
  0%,
  100% {
    box-shadow: 0 0 8px rgba(205, 180, 219, 0.4);
    transform: scale(1);
  }
  50% {
    box-shadow: 0 0 20px rgba(255, 182, 193, 0.8);
    transform: scale(1.02);
  }
}

.day-number {
  font-size: 1.5rem; /* Increased from 1.2rem */
  color: #555;
  font-weight: 800; /* Increased to 800 */
  /* Ensure centering */
  text-align: center;
  line-height: 1;
}

.day-cell.today .day-number {
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.star-indicator {
  width: 40px;
  height: 40px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.star-indicator svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 1px rgba(0, 0, 0, 0.15));
}

.mini-warning-bubble {
  position: absolute;
  bottom: 80%; /* Position above the cell */
  left: 50%;
  transform: translateX(-50%);
  background: rgba(50, 50, 50, 0.95);
  color: white;
  padding: 0.5rem 0.8rem;
  border-radius: 15px;
  z-index: 100;
  font-weight: 700;
  font-size: 0.75rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  pointer-events: none;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mini-warning-bubble::after {
  content: "";
  position: absolute;
  top: 100%; /* Arrow pointing down */
  left: 50%;
  transform: translateX(-50%);
  border-width: 6px 6px 0;
  border-style: solid;
  border-color: rgba(50, 50, 50, 0.95) transparent transparent transparent;
}

.pop-enter-active,
.pop-leave-active {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translate(-50%, 10px) scale(0.8);
}

/* Responsive Breakpoints */
@media (max-width: 768px) {
  .calendar-card {
    padding: 1.2rem;
    max-width: 95%;
  }

  .day-number {
    font-size: 1.3rem; /* Increased from 1.1rem */
  }

  .custom-select {
    font-size: 1.8rem; /* Increased from 1.5rem */
  }

  .service-title {
    font-size: 4rem;
  }

  .star-indicator {
    width: 30px;
    height: 30px;
  }
}

@media (max-width: 500px) {
  .calendar-wrapper {
    max-width: 95%;
  }

  .calendar-card {
    padding: 0.9rem;
    border-radius: 25px;
  }

  .nav-row {
    margin-bottom: 1.5rem;
  }

  .custom-select {
    font-size: 1.5rem; /* Increased from 1.3rem */
  }

  .weekday-header {
    font-size: 0.9rem;
    padding: 5px 0;
    margin-bottom: 2px;
  }

  /* No gap adjustment needed */

  .day-number {
    font-size: 1.1rem; /* Increased from 0.9rem */
    /* Removed bottom/right positioning to ensure centering */
    position: static;
  }

  .star-indicator {
    width: 24px;
    height: 24px;
  }

  .service-title {
    font-size: 3.5rem;
    margin-bottom: 0.5rem;
  }
}

/* Small mobile support down to 390px (and slightly below) */
@media (max-width: 390px) {
  .calendar-card {
    padding: 1.2rem 0.5rem;
  }

  .custom-select {
    font-size: 1.4rem; /* Increased from 1.2rem */
  }

  .nav-btn {
    font-size: 1.4rem;
  }

  .day-number {
    font-size: 1rem; /* Increased from 0.9rem */
  }

  .star-indicator {
    width: 20px;
    height: 20px;
  }
}
</style>
