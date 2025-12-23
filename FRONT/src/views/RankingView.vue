<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { getDreamCountRanking } from "../services/rankingService";

const router = useRouter();

const rankings = ref([]);
const totalUsers = ref(0);
const isLoading = ref(true);
const error = ref(null);

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

onMounted(async () => {
  try {
    isLoading.value = true;
    const data = await getDreamCountRanking();
    rankings.value = data.rankings || [];
    totalUsers.value = data.totalUsers || 0;
  } catch (err) {
    console.error("랭킹 조회 실패:", err);
    error.value = "랭킹을 불러오는데 실패했습니다.";
  } finally {
    isLoading.value = false;
  }
});

// 상위 3명 (트로피용)
const topThree = computed(() => rankings.value.slice(0, 3));

// 나머지 랭킹
const restRankings = computed(() => rankings.value.slice(3));

// 순위에 따른 카드 스타일
function getTopCardClass(rank) {
  switch (rank) {
    case 1:
      return "top-card gold";
    case 2:
      return "top-card silver";
    case 3:
      return "top-card bronze";
    default:
      return "top-card";
  }
}
</script>

<template>
  <div class="ranking-container">
    <div class="ranking-card">
      <div class="card-header">
        <button @click="handleBack" class="icon-btn">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
        </button>
        <h2 class="page-title">
          꿈 일기 랭킹
          <span class="title-badge">Ranking</span>
        </h2>
        <div class="header-actions">
          <button @click="handleClose" class="icon-btn" aria-label="닫기">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <p class="subtitle">가장 많은 꿈 일기를 작성한 몽글러들!</p>

      <!-- 로딩 상태 -->
      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>랭킹을 불러오는 중...</p>
      </div>

      <!-- 에러 상태 -->
      <div v-else-if="error" class="error-state">
        <span class="error-icon">
          <!-- Sad Face SVG -->
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <path d="M16 16s-1.5-2-4-2-4 2-4 2"></path>
            <line x1="9" y1="9" x2="9.01" y2="9"></line>
            <line x1="15" y1="9" x2="15.01" y2="9"></line>
          </svg>
        </span>
        <p>{{ error }}</p>
      </div>

      <!-- 데이터 없음 -->
      <div v-else-if="rankings.length === 0" class="empty-state">
        <span class="empty-icon">
          <!-- Note/Edit SVG -->
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="16" y1="13" x2="8" y2="13"></line>
            <line x1="16" y1="17" x2="8" y2="17"></line>
            <line x1="10" y1="9" x2="8" y2="9"></line>
          </svg>
        </span>
        <p>아직 꿈 일기를 작성한 사용자가 없습니다.</p>
        <p class="empty-hint">첫 번째 랭커가 되어보세요!</p>
      </div>

      <!-- 랭킹 표시 -->
      <div v-else class="ranking-content">
        <!-- 상위 3명 포디움 -->
        <div class="podium-section">
          <div class="podium">
            <!-- 2등 (왼쪽) -->
            <div v-if="topThree[1]" :class="getTopCardClass(2)">
              <div class="avatar silver-avatar">2</div>
              <div class="name">{{ topThree[1].maskedName }}</div>
              <div class="dream-count">{{ topThree[1].dreamCount }}개</div>
              <div class="podium-stand silver-stand"></div>
            </div>
            <div v-else class="top-card placeholder"></div>

            <!-- 1등 (가운데) -->
            <div v-if="topThree[0]" :class="getTopCardClass(1)">
              <div class="crown">
                <!-- Crown SVG -->
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                  <path d="M2 20h20L19 8l-5 4-2-6-2 6-5-4-3 12z" fill="#FFD700" stroke="#DAA520" stroke-width="1.5" />
                  <circle cx="12" cy="6" r="2" fill="#FFD700" stroke="#DAA520" stroke-width="1" />
                  <circle cx="4" cy="8" r="1.5" fill="#FFD700" stroke="#DAA520" stroke-width="1" />
                  <circle cx="20" cy="8" r="1.5" fill="#FFD700" stroke="#DAA520" stroke-width="1" />
                </svg>
              </div>
              <div class="avatar gold-avatar">1</div>
              <div class="name">{{ topThree[0].maskedName }}</div>
              <div class="dream-count">{{ topThree[0].dreamCount }}개</div>
              <div class="podium-stand gold-stand"></div>
            </div>
            <div v-else class="top-card placeholder"></div>

            <!-- 3등 (오른쪽) -->
            <div v-if="topThree[2]" :class="getTopCardClass(3)">
              <div class="avatar bronze-avatar">3</div>
              <div class="name">{{ topThree[2].maskedName }}</div>
              <div class="dream-count">{{ topThree[2].dreamCount }}개</div>
              <div class="podium-stand bronze-stand"></div>
            </div>
            <div v-else class="top-card placeholder"></div>
          </div>
        </div>

        <!-- 나머지 랭킹 리스트 -->
        <div v-if="restRankings.length > 0" class="ranking-list">
          <div class="list-header">
            <span>순위</span>
            <span>이름</span>
            <span>꿈 일기 수</span>
          </div>
          <div v-for="item in restRankings" :key="item.rank" class="ranking-item">
            <span class="rank">{{ item.rank }}</span>
            <span class="name">{{ item.maskedName }}</span>
            <span class="count">{{ item.dreamCount }}개</span>
          </div>
        </div>

        <!-- 총 참여자 수 -->
        <div class="total-users">
          총
          <strong>{{ totalUsers }}</strong>
          명의 몽글러가 참여 중!
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ranking-container {
  width: 100%;
  max-width: 600px;
  animation: fadeSlideUp 0.5s ease;
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

/* 랭킹 카드 */
.ranking-card {
  background: white;
  border-radius: 32px;
  padding: 1.5rem 2rem 2rem;
  box-shadow: 0 16px 48px rgba(100, 100, 200, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #888;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background: #f5f5f5;
  color: #555;
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
  color: #4c2b7b;
  -webkit-text-fill-color: #4c2b7b;
  line-height: 1.2;
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

.header-actions {
  display: inline-flex;
  gap: 0.5rem;
  align-items: center;
}

.subtitle {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

/* 로딩 상태 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 3rem;
  color: var(--color-text-secondary);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-purple-20);
  border-top-color: var(--color-purple);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 에러 및 빈 상태 */
.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 3rem;
  color: var(--color-text-secondary);
  text-align: center;
}

.error-icon,
.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-purple);
}

.empty-hint {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

/* 포디움 섹션 */
.podium-section {
  margin-bottom: 2rem;
}

.podium {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 0.75rem;
  margin-top: 4rem;
}

.top-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem 1rem 0;
  background: var(--gradient-bg-15);
  border: 1px solid var(--border-purple);
  border-radius: 24px 24px 0 0;
  min-width: 100px;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.top-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px var(--shadow-purple);
}

.top-card.placeholder {
  visibility: hidden;
}

.top-card.gold {
  background: linear-gradient(180deg, rgba(255, 215, 0, 0.15), rgba(255, 215, 0, 0.05));
  border: 2px solid rgba(255, 215, 0, 0.4);
}

.top-card.silver {
  background: linear-gradient(180deg, rgba(192, 192, 192, 0.2), rgba(192, 192, 192, 0.08));
  border: 2px solid rgba(192, 192, 192, 0.4);
}

.top-card.bronze {
  background: linear-gradient(180deg, rgba(205, 127, 50, 0.15), rgba(205, 127, 50, 0.05));
  border: 2px solid rgba(205, 127, 50, 0.4);
}

.crown {
  position: absolute;
  top: -37px;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Nunito", sans-serif;
  font-weight: 800;
  font-size: 1.2rem;
  color: white;
  margin-bottom: 0.5rem;
}

.gold-avatar {
  background: linear-gradient(135deg, #ffd700, #ffb347);
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.4);
}

.silver-avatar {
  background: linear-gradient(135deg, #c0c0c0, #a8a8a8);
  box-shadow: 0 4px 15px rgba(192, 192, 192, 0.4);
}

.bronze-avatar {
  background: linear-gradient(135deg, #cd7f32, #b8860b);
  box-shadow: 0 4px 15px rgba(205, 127, 50, 0.4);
}

.top-card {
  font-family: "Dongle", sans-serif;
}

.top-card .name {
  font-weight: 700;
  font-size: 1.6rem;
  color: var(--color-text-primary);
  margin-bottom: 0;
  line-height: 1.2;
}

.top-card .dream-count {
  font-weight: 600;
  font-size: 1.4rem;
  color: var(--color-text-secondary);
  margin-bottom: 0.75rem;
  line-height: 1.2;
}

.podium-stand {
  width: 100%;
  border-radius: 0;
}

.gold-stand {
  height: 80px;
  background: linear-gradient(180deg, #ffd700, #daa520);
}

.silver-stand {
  height: 60px;
  background: linear-gradient(180deg, #c0c0c0, #a0a0a0);
}

.bronze-stand {
  height: 40px;
  background: linear-gradient(180deg, #cd7f32, #8b4513);
}

/* 랭킹 리스트 */
.ranking-list {
  background: #fafafa;
  border-radius: 20px;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.list-header {
  display: grid;
  grid-template-columns: 60px 1fr 100px;
  justify-items: center;
  padding: 0.75rem 1rem;
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.4rem;
  line-height: 1.2;
  color: var(--color-text-muted);
  border-bottom: 1px dashed var(--border-purple);
}

.ranking-item {
  display: grid;
  grid-template-columns: 60px 1fr 100px;
  justify-items: center;
  padding: 1rem;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s ease;
  font-family: "Dongle", sans-serif;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-item:hover {
  background: var(--color-purple-light);
  border-radius: 12px;
}

.ranking-item .rank {
  font-family: "Nunito", sans-serif;
  font-weight: 800;
  font-size: 1.1rem;
  color: var(--color-text-primary);
}

.ranking-item .name {
  font-weight: 600;
  font-size: 1.5rem;
  line-height: 1.2;
  color: var(--color-text-secondary);
}

.ranking-item .count {
  font-weight: 700;
  font-size: 1.5rem;
  line-height: 1.2;
  color: var(--color-purple);
  text-align: right;
}

/* 총 참여자 수 */
.total-users {
  text-align: center;
  font-family: "Dongle", sans-serif;
  font-size: 1.8rem;
  font-weight: 700;
  line-height: 1.2;
  color: var(--color-text-primary);
  padding: 0.75rem 1.5rem;
  background: var(--gradient-bg-15);
  border-radius: 999px;
  border: 1px solid var(--border-purple);
}

.total-users strong {
  font-size: 2.2rem;
  font-weight: 800;
  color: var(--color-purple);
  -webkit-text-fill-color: var(--color-purple);
  margin: 0 0.15rem;
}

/* 반응형 */
@media (max-width: 768px) {
  .page-title {
    font-size: 1.8rem;
    padding: 0.4rem 1rem;
  }

  .title-badge {
    display: none;
  }
}

@media (max-width: 480px) {
  .ranking-card {
    padding: 1.25rem;
    border-radius: 24px;
  }

  .page-title {
    font-size: 1.6rem;
    padding: 0.35rem 0.85rem;
  }

  .podium {
    gap: 0.5rem;
  }

  .top-card {
    min-width: 80px;
    padding: 1rem 0.5rem 0;
    border-radius: 16px 16px 0 0;
  }

  .crown svg {
    width: 22px;
    height: 22px;
  }

  .avatar {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }

  .top-card .name {
    font-size: 1.3rem;
  }

  .top-card .dream-count {
    font-size: 1.1rem;
  }

  .list-header {
    grid-template-columns: 50px 1fr 80px;
    padding: 0.75rem;
    font-size: 1.2rem;
  }

  .ranking-item {
    grid-template-columns: 50px 1fr 80px;
    padding: 0.75rem;
  }

  .ranking-item .name,
  .ranking-item .count {
    font-size: 1.3rem;
  }

  .total-users {
    font-size: 1.5rem;
  }

  .total-users strong {
    font-size: 1.8rem;
  }
}
</style>
