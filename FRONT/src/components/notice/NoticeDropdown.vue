<template>
  <div class="notice-wrapper" ref="noticeWrapper">
    <button @click="toggleNotice" class="glass-btn notice-btn" :class="{ 'has-new': hasNewNotice }" aria-label="공지사항">
      <span class="btn-icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
        </svg>
      </span>
      <span v-if="hasNewNotice" class="notice-badge"></span>
    </button>

    <!-- 드롭다운 목록 -->
    <transition name="popover">
      <div v-if="showNotice" class="glass-popover notice-popover">
        <div class="notice-header">
          <span class="notice-title">공지사항</span>
          <button v-if="isAdmin" class="notice-write-btn" @click="openWriteModal">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            작성
          </button>
        </div>
        <div v-if="noticesLoading" class="notice-loading">
          <span>불러오는 중...</span>
        </div>
        <div v-else-if="notices.length === 0" class="notice-empty">
          <span>등록된 공지사항이 없습니다.</span>
        </div>
        <ul v-else class="notice-list">
          <li 
            v-for="notice in notices.slice(0, 5)" 
            :key="notice.noticeId" 
            class="notice-item"
            @click="openNoticeDetail(notice)"
          >
            <div class="notice-item-content">
              <span class="notice-item-title">{{ notice.title }}</span>
              <div class="notice-item-stats">
                <span class="notice-stat">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                  {{ notice.viewCount || 0 }}
                </span>
                <span class="notice-stat">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                  </svg>
                  {{ notice.likeCount || 0 }}
                </span>
              </div>
            </div>
            <span class="notice-item-date">{{ formatDate(notice.createdDate) }}</span>
          </li>
        </ul>
      </div>
    </transition>

    <!-- 상세 팝업 모달 -->
    <NoticeModal 
      :notice="selectedNotice" 
      @close="selectedNotice = null"
      @edit="handleEditNotice"
      @deleted="handleNoticeDeleted"
      @refreshList="fetchNotices"
    />
    
    <!-- 공지사항 작성/수정 모달 (관리자 전용) -->
    <NoticeWriteModal 
      v-if="isAdmin"
      :show="showWriteModal" 
      :editNotice="editingNotice"
      @close="closeWriteModal" 
      @created="handleNoticeCreated"
      @updated="handleNoticeUpdated"
    />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useAuthStore } from "../../stores/authStore";
import { noticeService } from "../../services/noticeService";
import NoticeModal from "./NoticeModal.vue";
import NoticeWriteModal from "./NoticeWriteModal.vue";

// 인증 스토어에서 관리자 여부 확인
const authStore = useAuthStore();
const isAdmin = computed(() => authStore.isAdmin);

// 공지사항 관련 상태
const showNotice = ref(false);
const noticeWrapper = ref(null);
const notices = ref([]);
const noticesLoading = ref(false);
const selectedNotice = ref(null);
const lastReadNoticeId = ref(null);
const showWriteModal = ref(false);
const editingNotice = ref(null);

// 새 공지사항 여부 (로컬 스토리지와 비교)
const hasNewNotice = computed(() => {
  if (notices.value.length === 0) return false;
  const latestId = notices.value[0]?.noticeId;
  return latestId && latestId !== lastReadNoticeId.value;
});

// 공지사항 토글
async function toggleNotice() {
  showNotice.value = !showNotice.value;
  
  if (showNotice.value && notices.value.length === 0) {
    await fetchNotices();
  }
  
  // 공지사항 열면 읽은 것으로 처리
  if (showNotice.value && notices.value.length > 0) {
    const latestId = notices.value[0]?.noticeId;
    if (latestId) {
      lastReadNoticeId.value = latestId;
      localStorage.setItem('lastReadNoticeId', latestId.toString());
    }
  }
}

// 공지사항 불러오기
async function fetchNotices() {
  noticesLoading.value = true;
  try {
    const response = await noticeService.getAllNotices();
    notices.value = response.notices || [];
  } catch (error) {
    console.error('공지사항 로드 실패:', error);
    notices.value = [];
  } finally {
    noticesLoading.value = false;
  }
}

// 공지사항 상세 보기 (팝업 열기)
function openNoticeDetail(notice) {
  selectedNotice.value = notice;
  showNotice.value = false; // 드롭다운 닫기
}

// 공지사항 작성 모달 열기 (관리자 전용)
function openWriteModal() {
  editingNotice.value = null; // 작성 모드
  showWriteModal.value = true;
  showNotice.value = false; // 드롭다운 닫기
}

// 공지사항 작성/수정 모달 닫기
function closeWriteModal() {
  showWriteModal.value = false;
  editingNotice.value = null;
}

// 공지사항 작성 완료 처리
async function handleNoticeCreated() {
  await fetchNotices(); // 목록 새로고침
}

// 공지사항 수정 모달 열기 (관리자 전용)
function handleEditNotice(notice) {
  selectedNotice.value = null; // 상세 모달 닫기
  editingNotice.value = notice; // 수정할 공지사항 설정
  showWriteModal.value = true;
}

// 공지사항 수정 완료 처리
async function handleNoticeUpdated() {
  await fetchNotices(); // 목록 새로고침
}

// 공지사항 삭제 완료 처리
async function handleNoticeDeleted() {
  await fetchNotices(); // 목록 새로고침
}

// 날짜 포맷팅 (간략)
function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${month}/${day}`;
}

// 공지사항 외부 클릭 처리
function handleNoticeOutsideClick(event) {
  if (!noticeWrapper.value) return;
  if (noticeWrapper.value.contains(event.target)) return;
  showNotice.value = false;
}

// 외부에서 드롭다운 닫기 (헤더에서 다른 드롭다운 열 때 사용)
function closeDropdown() {
  showNotice.value = false;
}

// expose로 외부에서 접근 가능하게 함
defineExpose({ closeDropdown });

onMounted(() => {
  // 로컬 스토리지에서 마지막 읽은 공지사항 ID 불러오기
  const storedId = localStorage.getItem('lastReadNoticeId');
  if (storedId) {
    lastReadNoticeId.value = parseInt(storedId);
  }
  
  // 공지사항 미리 불러오기 (새 공지사항 뱃지 표시용)
  fetchNotices();
  
  document.addEventListener("click", handleNoticeOutsideClick);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleNoticeOutsideClick);
});
</script>

<style scoped>
.notice-wrapper {
  position: relative;
}

.notice-btn {
  position: relative;
  padding: 0.5rem;
  border-radius: 50%;
  width: 42px;
  height: 42px;
  justify-content: center;
}

.notice-btn.has-new::after {
  content: '';
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  background: #ff6b6b;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.8);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

.notice-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  background: #ff6b6b;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.8);
}

.notice-popover {
  top: calc(100% + 10px);
  left: 0;
  width: 320px;
  max-height: 400px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.notice-popover::before {
  left: 14px;
  right: auto;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 0.5rem;
}

.notice-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  font-family: 'Dongle';
}

.notice-write-btn {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.05rem 0.6rem;
  font-size: 1.2rem;
  font-weight: 700;
  color: #fff;
  background: var(--gradient-purple-blue);
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Dongle' !important;
}

.notice-write-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--shadow-purple, rgba(205, 180, 219, 0.4));
}

.notice-write-btn svg {
  stroke-width: 2.5;
}

.notice-loading,
.notice-empty {
  padding: 2rem 1rem;
  text-align: center;
  color: #999;
  font-size: 0.9rem;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 280px;
  overflow-y: auto;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0.5rem;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notice-item:hover {
  background: linear-gradient(135deg, #f8f9ff, #e8f0fe);
}

.notice-item-content {
  flex: 1;
  min-width: 0;
  margin-right: 0.5rem;
}

.notice-item-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
  margin-bottom: 0.25rem;
}

.notice-item-stats {
  display: flex;
  gap: 0.6rem;
}

.notice-stat {
  display: flex;
  align-items: center;
  gap: 0.2rem;
  font-size: 0.75rem;
  color: #aaa;
  font-family: "Nunito";
}

.notice-stat svg {
  opacity: 0.7;
}

.notice-item-date {
  font-size: 1.2rem;
  color: #999;
  flex-shrink: 0;
  align-self: flex-start;
  font-family: 'Dongle';
  line-height: 1rem;
}

/* 반응형 */
@media (max-width: 768px) {
  .notice-btn {
    padding: 0.5rem;
    border-radius: 16px;
    min-width: 42px;
    height: 42px;
  }

  .notice-popover {
    left: 0;
    width: 300px;
  }

  .notice-popover::before {
    left: 14px;
    right: auto;
  }
}

@media (max-width: 480px) {
  .notice-btn {
    min-width: 38px;
    height: 38px;
    padding: 0.4rem;
  }

  .notice-popover {
    left: 0;
    width: 280px;
    max-height: 350px;
  }

  .notice-popover::before {
    left: 14px;
    right: auto;
  }
}
</style>

