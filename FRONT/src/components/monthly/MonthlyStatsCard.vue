<template>
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
        <div class="stat-value">{{ totalDreams }}개</div>
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
        <div class="stat-value">{{ streak }}</div>
        <div class="stat-label">연속 기록</div>
      </div>
    </div>
    <div class="stat-card emotion-card">
      <div class="stat-icon-wrap">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10" />
          <path d="M8 14s1.5 2 4 2 4-2 4-2" />
          <line x1="9" y1="9" x2="9.01" y2="9" />
          <line x1="15" y1="9" x2="15.01" y2="9" />
        </svg>
      </div>
      <div class="stat-content">
        <div class="stat-value score-value">
          {{ displayScore }}
          <span class="score-max">/5점</span>
        </div>
        <div class="stat-label">평균 감정 점수</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  totalDreams: {
    type: Number,
    default: 0,
  },
  streak: {
    type: String,
    default: "0일",
  },
  avgEmotionScore: {
    type: Number,
    default: null,
  },
});

// 점수를 소수점 한 자리로 표시
const displayScore = computed(() => {
  if (props.avgEmotionScore === null || props.avgEmotionScore === undefined) {
    return "-";
  }
  return props.avgEmotionScore.toFixed(1);
});
</script>

<style scoped>
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
  box-shadow: 0 8px 28px var(--shadow-purple-md);
}

.streak-card {
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e0 100%);
  color: #7a4a5a;
  box-shadow: 0 6px 20px rgba(255, 200, 221, 0.25);
}

.streak-card:hover {
  box-shadow: 0 8px 28px rgba(255, 200, 221, 0.4);
}

.emotion-card {
  background: linear-gradient(135deg, #d4edff 0%, #c4e4ff 100%);
  color: #4a6a8a;
  box-shadow: 0 6px 20px rgba(162, 210, 255, 0.25);
}

.emotion-card:hover {
  box-shadow: 0 8px 28px rgba(162, 210, 255, 0.4);
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

.emotion-card .stat-icon-wrap svg {
  width: 28px;
  height: 28px;
  stroke: #5a8ab8;
}

.stat-content {
  display: flex;
  flex-direction: column;
  min-width: 0;
  align-items: flex-start;
  gap: 0.15rem;
}

.stat-value {
  font-family: "Dongle", sans-serif;
  font-size: 2.6rem;
  font-weight: 700;
  line-height: 1;
  letter-spacing: -0.5px;
  color: #4c2b7b;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

.score-value {
  display: flex;
  align-items: baseline;
}

.score-max {
  font-size: 1.4rem;
  font-weight: 600;
  opacity: 0.6;
  margin-left: 1px;
}

.stat-label {
  font-family: "Dongle", sans-serif;
  font-size: 1.3rem;
  font-weight: 600;
  color: inherit;
  white-space: nowrap;
  line-height: 1.2;
}

@media (max-width: 1040px) {
  .stat-card {
    gap: 1rem;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    flex-direction: column;
    gap: 0.6rem;
  }

  .stat-card {
    flex-direction: row;
    padding: 0.9rem 1.25rem;
    gap: 1rem;
  }

  .stat-content {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    width: 100%;
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
    font-size: 2.4rem;
  }

  .stat-label {
    font-size: 1.2rem;
  }

  .score-max {
    font-size: 1.2rem;
  }
}
</style>
