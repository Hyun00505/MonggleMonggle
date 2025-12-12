<template>
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
      <div v-if="totalPages > 1" class="dream-pagination">
        <button class="page-btn icon" @click="$emit('prev-page')" :disabled="currentPage === 1">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </button>
        <span class="page-indicator">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn icon" @click="$emit('next-page')" :disabled="currentPage === totalPages">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 6 15 12 9 18"></polyline>
          </svg>
        </button>
      </div>
    </div>
    <div v-if="dreams.length > 0" class="dream-list">
      <div v-for="dream in dreams" :key="dream.date" class="dream-item" @click="$emit('select-dream', dream.date)">
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
</template>

<script setup>
defineProps({
  dreams: {
    type: Array,
    default: () => [],
  },
  currentPage: {
    type: Number,
    default: 1,
  },
  totalPages: {
    type: Number,
    default: 1,
  },
});

defineEmits(["select-dream", "prev-page", "next-page"]);
</script>

<style scoped>
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

.dream-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
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

@media (max-width: 768px) {
  .section-card {
    padding: 1.25rem;
  }

  .section-title {
    font-size: 1.5rem;
  }
}
</style>

