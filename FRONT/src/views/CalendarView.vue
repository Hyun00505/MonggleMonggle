<script setup>
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import DreamCalendar from "../components/DreamCalendar.vue";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";

const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const { postedDates } = storeToRefs(dreamEntriesStore);
const { setSelectedDateWithResult, fetchDreamsByMonth } = dreamEntriesStore;

// 캘린더 진입 시 현재 월의 꿈 데이터를 서버에서 불러오기
onMounted(async () => {
  const now = new Date();
  await fetchDreamsByMonth(now.getFullYear(), now.getMonth() + 1);
});

async function handleDateSelect(date) {
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
    <DreamCalendar :posted-dates="postedDates" @date-click="handleDateSelect" />
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
