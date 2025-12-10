<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import DreamCalendar from "../components/DreamCalendar.vue";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";

const route = useRoute();
const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const { postedDates, selectedDate } = storeToRefs(dreamEntriesStore);
const { setSelectedDateWithResult, fetchDreamsByMonth } = dreamEntriesStore;

// 캘린더 초기 진입 시 노출할 기준 날짜 (기본: 오늘, 이전 선택/쿼리 우선)
const initialDate = ref(new Date());

function resolveInitialDate() {
  // 1순위: 스토어에 남아있는 선택 날짜
  if (selectedDate.value) return new Date(selectedDate.value);

  // 2순위: 라우터 쿼리 파라미터(date)
  const queryDate = route.query.date;
  if (queryDate) {
    const parsed = new Date(queryDate);
    if (!Number.isNaN(parsed.getTime())) return parsed;
  }

  // 기본: 오늘
  return new Date();
}

// 캘린더 진입 시 적절한 월의 꿈 데이터를 서버에서 불러오기
onMounted(async () => {
  initialDate.value = resolveInitialDate();
  await fetchDreamsByMonth(initialDate.value.getFullYear(), initialDate.value.getMonth() + 1);
});

async function handleMonthChange({ year, month }) {
  // 월 변경 시에도 초기 날짜 상태를 해당 월로 맞춰 뒤로가기 시 재사용
  initialDate.value = new Date(year, month - 1, 1);
  await fetchDreamsByMonth(year, month);
}

async function handleDateSelect(date) {
  // 선택한 날짜를 저장해 뒤로가기 시 동일 월을 유지
  initialDate.value = date;

  // 날짜 선택 시 해몽 결과도 함께 조회하도록 변경
  await setSelectedDateWithResult(date);

  // 날짜를 쿼리 파라미터로 전달 (새로고침 시 복원용)
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const dateStr = `${year}-${month}-${day}`;
  router.push({ name: "write", query: { date: dateStr } });
}
</script>

<template>
  <div class="view-wrapper">
    <DreamCalendar :posted-dates="postedDates" :initial-date="initialDate" @date-click="handleDateSelect" @month-change="handleMonthChange" />
    <p class="instruction-text">날짜를 클릭해서 꿈 이야기를 들려주세요!</p>
  </div>
</template>

<style scoped>
.view-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.instruction-text {
  margin-top: 2rem;
  color: white;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    opacity: 0.7;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0.7;
  }
}
</style>
