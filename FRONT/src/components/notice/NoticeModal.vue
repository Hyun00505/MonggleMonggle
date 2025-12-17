<template>
  <Teleport to="body">
    <transition name="modal">
      <div v-if="notice" class="notice-modal-overlay" @click.self="$emit('close')">
        <div class="notice-modal">
          <button class="icon-btn icon-btn-absolute" @click="$emit('close')" aria-label="닫기">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
          
          <!-- 공지사항 본문 -->
          <div class="notice-modal-header">
            <span class="notice-modal-badge">공지</span>
            <span class="notice-modal-date">{{ formatFullDate(notice.createdDate) }}</span>
            <span class="notice-modal-views">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
              {{ notice.viewCount || 0 }}
            </span>
            <!-- 관리자 전용 수정/삭제 버튼 -->
            <div v-if="isAdmin" class="admin-actions">
              <button class="admin-btn edit-btn" @click="handleEdit" title="수정">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button class="admin-btn delete-btn" @click="handleDelete" title="삭제">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  <line x1="10" y1="11" x2="10" y2="17"></line>
                  <line x1="14" y1="11" x2="14" y2="17"></line>
                </svg>
              </button>
            </div>
          </div>
          <h2 class="notice-modal-title">{{ notice.title }}</h2>
          <div class="notice-modal-content">{{ notice.content }}</div>

          <!-- 좋아요 버튼 -->
          <div class="notice-like-section">
            <button class="notice-like-btn" :class="{ 'liked': noticeLiked }" @click="toggleNoticeLike">
              <svg width="20" height="20" viewBox="0 0 24 24" :fill="noticeLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
              <span>{{ noticeLikes }}</span>
            </button>
          </div>

          <!-- 댓글 섹션 -->
          <div class="comments-section">
            <div class="comments-header">
              <span class="comments-title">💬 댓글</span>
              <span class="comments-count">{{ comments.length }}</span>
            </div>

            <!-- 댓글 입력 -->
            <div class="comment-input-wrapper">
              <textarea 
                v-model="newComment" 
                class="comment-input" 
                placeholder="댓글을 입력하세요..."
                rows="2"
                @keydown.ctrl.enter="addComment"
              ></textarea>
              <button class="comment-submit-btn" @click="addComment" :disabled="!newComment.trim()">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="22" y1="2" x2="11" y2="13"></line>
                  <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                </svg>
              </button>
            </div>

            <!-- 댓글 목록 -->
            <div v-if="comments.length === 0" class="comments-empty">
              아직 댓글이 없습니다. 첫 댓글을 남겨보세요!
            </div>
            <ul v-else class="comments-list">
              <li v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-avatar">
                  {{ comment.author.charAt(0) }}
                </div>
                <div class="comment-body">
                  <div class="comment-meta">
                    <span class="comment-author">{{ comment.author }}</span>
                    <span class="comment-date">{{ comment.date }}</span>
                  </div>
                  <p class="comment-text">{{ comment.content }}</p>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from "vue";
import { useAuthStore } from "../../stores/authStore";
import { noticeService } from "../../services/noticeService";

const props = defineProps({
  notice: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close', 'edit', 'deleted']);

// 관리자 여부 확인
const authStore = useAuthStore();
const isAdmin = computed(() => authStore.isAdmin);

const newComment = ref('');

// 공지사항 좋아요 상태 (UI만)
const noticeLiked = ref(false);
const noticeLikes = ref(0);

// 더미 댓글 데이터
const comments = ref([
  {
    id: 1,
    author: '김몽글',
    content: '좋은 공지 감사합니다! 앞으로도 잘 부탁드려요 😊',
    date: '12/15'
  },
  {
    id: 2,
    author: '박드림',
    content: '확인했습니다~',
    date: '12/16'
  }
]);

// 모달 열릴 때 스크롤 방지 및 상태 초기화
watch(() => props.notice, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden';
    newComment.value = '';
    // 좋아요 상태 초기화
    noticeLiked.value = false;
    noticeLikes.value = newVal.likeCount || 0;
  } else {
    document.body.style.overflow = '';
  }
});

// 날짜 포맷팅 (전체)
function formatFullDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${year}년 ${month}월 ${day}일`;
}

// 댓글 추가 (UI만)
function addComment() {
  if (!newComment.value.trim()) return;
  
  const now = new Date();
  comments.value.unshift({
    id: Date.now(),
    author: '나',
    content: newComment.value.trim(),
    date: `${now.getMonth() + 1}/${now.getDate()}`
  });
  newComment.value = '';
}

// 공지사항 좋아요 토글 (UI만)
function toggleNoticeLike() {
  noticeLiked.value = !noticeLiked.value;
  noticeLikes.value += noticeLiked.value ? 1 : -1;
}

// 공지사항 수정 (관리자 전용)
function handleEdit() {
  emit('edit', props.notice);
}

// 공지사항 삭제 (관리자 전용)
async function handleDelete() {
  if (!confirm('정말 이 공지사항을 삭제하시겠습니까?')) return;
  
  try {
    await noticeService.deleteNotice(props.notice.noticeId);
    emit('deleted');
    emit('close');
  } catch (error) {
    console.error('공지사항 삭제 실패:', error);
    alert('공지사항 삭제에 실패했습니다.');
  }
}

// ESC 키로 모달 닫기
function handleKeydown(event) {
  if (event.key === 'Escape' && props.notice) {
    emit('close');
  }
}

onMounted(() => {
  document.addEventListener("keydown", handleKeydown);
});

onBeforeUnmount(() => {
  document.removeEventListener("keydown", handleKeydown);
  document.body.style.overflow = '';
});
</script>

<style scoped>
/* 모달 오버레이 */
.notice-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 1rem;
}

/* 모달 본체 */
.notice-modal {
  background: white;
  border-radius: 24px;
  padding: 1.5rem 2rem 2rem 2rem;
  width: 100%;
  max-width: 540px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  position: relative;
}

.notice-modal-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.notice-modal-badge {
  background: var(--gradient-purple-blue);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 700;
}

.notice-modal-date {
  color: #999;
  font-size: 0.85rem;
}

.notice-modal-views {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #aaa;
  font-size: 0.85rem;
}

.notice-modal-views svg {
  opacity: 0.7;
}

/* 관리자 전용 버튼 */
.admin-actions {
  display: flex;
  gap: 0.4rem;
}

.admin-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.edit-btn {
  background: #f0f9ff;
  color: #0ea5e9;
}

.edit-btn:hover {
  background: #e0f2fe;
  transform: translateY(-1px);
}

.delete-btn {
  background: #fef2f2;
  color: #ef4444;
}

.delete-btn:hover {
  background: #fee2e2;
  transform: translateY(-1px);
}

.notice-modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 1.5rem 0;
  line-height: 1.4;
  padding-right: 2rem;
}

.notice-modal-content {
  font-size: 0.95rem;
  color: #555;
  line-height: 1.8;
  white-space: pre-wrap;
}

/* 좋아요 섹션 */
.notice-like-section {
  display: flex;
  justify-content: center;
  padding: 1.25rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.notice-like-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.25rem;
  border: 2px solid #f0f0f0;
  border-radius: 25px;
  background: white;
  color: #999;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notice-like-btn:hover {
  border-color: #ffccd5;
  color: #ff6b6b;
  background: #fff5f5;
}

.notice-like-btn.liked {
  border-color: #ff6b6b;
  color: #ff6b6b;
  background: #fff5f5;
}

.notice-like-btn.liked svg {
  animation: heartBeat 0.3s ease;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

/* 댓글 섹션 */
.comments-section {
  margin-top: 1.5rem;
}

.comments-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.comments-title {
  font-size: 1rem;
  font-weight: 700;
  color: #333;
}

.comments-count {
  background: var(--color-purple-20, rgba(205, 180, 219, 0.2));
  color: var(--color-text-primary, #4c2b7b);
  padding: 0.15rem 0.5rem;
  border-radius: 10px;
  font-size: 0.8rem;
  font-weight: 700;
}

/* 댓글 입력 */
.comment-input-wrapper {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.25rem;
}

.comment-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 2px solid #f0f0f0;
  border-radius: 16px;
  font-size: 0.9rem;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}

.comment-input:focus {
  border-color: var(--color-purple, #cdb4db);
}

.comment-input::placeholder {
  color: #bbb;
}

.comment-submit-btn {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: var(--gradient-purple-blue);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
  align-self: flex-end;
}

.comment-submit-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px var(--shadow-purple, rgba(205, 180, 219, 0.4));
}

.comment-submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 댓글 없음 */
.comments-empty {
  text-align: center;
  color: #999;
  font-size: 0.9rem;
  padding: 2rem 1rem;
  background: #fafafa;
  border-radius: 16px;
}

/* 댓글 목록 */
.comments-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.comment-item {
  display: flex;
  gap: 0.75rem;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--gradient-purple-blue);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.comment-author {
  font-size: 0.9rem;
  font-weight: 700;
  color: #333;
}

.comment-date {
  font-size: 0.8rem;
  color: #999;
}

.comment-text {
  font-size: 0.9rem;
  color: #555;
  line-height: 1.5;
  margin: 0;
  word-break: break-word;
}

/* 모달 애니메이션 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .notice-modal,
.modal-leave-active .notice-modal {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .notice-modal {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

.modal-leave-to .notice-modal {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

/* 반응형 */
@media (max-width: 768px) {
  .notice-modal {
    padding: 1.5rem;
    max-width: 90%;
  }

  .notice-modal-title {
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .notice-modal {
    padding: 1.25rem;
    border-radius: 20px;
  }

  .notice-modal-title {
    font-size: 1rem;
  }

  .notice-modal-content {
    font-size: 0.9rem;
  }

  .comment-input-wrapper {
    flex-direction: column;
  }

  .comment-submit-btn {
    width: 100%;
    height: 40px;
    border-radius: 12px;
  }

  .comment-avatar {
    width: 32px;
    height: 32px;
    font-size: 0.8rem;
  }
}
</style>

