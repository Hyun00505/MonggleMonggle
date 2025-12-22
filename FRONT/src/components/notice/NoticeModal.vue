<template>
  <Teleport to="body">
    <transition name="modal">
      <div v-if="noticeDetail" class="notice-modal-overlay" @click.self="handleClose">
        <div class="notice-modal">
          <button class="icon-btn icon-btn-absolute" @click="handleClose" aria-label="닫기">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
          
          <!-- 고정 헤더 영역 -->
          <div class="notice-modal-fixed">
            <div class="notice-modal-header">
              <span class="notice-modal-badge">공지</span>
              <span class="notice-modal-date">{{ formatFullDate(noticeDetail.createdDate) }}</span>
              <span class="notice-modal-views">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
                {{ noticeDetail.viewCount || 0 }}
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
            <h2 class="notice-modal-title">{{ noticeDetail.title }}</h2>
          </div>

          <!-- 스크롤 영역 -->
          <div class="notice-modal-body">
            <div class="notice-modal-content">{{ noticeDetail.content }}</div>

          <!-- 좋아요 버튼 -->
          <div class="notice-like-section">
            <button 
              class="notice-like-btn" 
              :class="{ 'liked': noticeLiked }" 
              :disabled="isTogglingLike"
              @click="toggleNoticeLike"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" :fill="noticeLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
              <span>{{ noticeLikes }}</span>
            </button>
          </div>

          <!-- 댓글 섹션 -->
          <div class="comments-section">
            <div class="comments-header">
              <span class="comments-title">댓글</span>
              <span class="comments-count">{{ comments.length }}</span>
            </div>

            <!-- 댓글 입력 -->
            <div class="comment-input-wrapper">
              <textarea 
                v-model="newComment" 
                class="comment-input" 
                placeholder="댓글을 입력하세요..."
                rows="2"
                :disabled="isSubmittingComment"
                @keydown.ctrl.enter="addComment"
              ></textarea>
              <button 
                class="comment-submit-btn" 
                :disabled="!newComment.trim() || isSubmittingComment"
                @click="addComment"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="22" y1="2" x2="11" y2="13"></line>
                  <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                </svg>
              </button>
            </div>

            <!-- 댓글 로딩 -->
            <div v-if="commentsLoading" class="comments-loading">
              댓글을 불러오는 중...
            </div>
            <!-- 댓글 목록 -->
            <div v-else-if="comments.length === 0" class="comments-empty">
              아직 댓글이 없습니다. 첫 댓글을 남겨보세요!
            </div>
            <ul v-else class="comments-list">
              <li v-for="comment in comments" :key="comment.commentId" class="comment-item">
                <div class="comment-avatar">
                  {{ comment.userName?.charAt(0) || '?' }}
                </div>
                <div class="comment-body">
                  <div class="comment-meta">
                    <span class="comment-author">{{ comment.userName }}</span>
                    <span class="comment-date">{{ formatCommentDate(comment.createdDate) }}</span>
                    <!-- 본인 댓글 또는 관리자일 때 삭제 버튼 -->
                    <button 
                      v-if="comment.isOwner || comment.owner || isAdmin" 
                      class="comment-delete-btn"
                      @click="deleteComment(comment.commentId)"
                      title="삭제"
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                      </svg>
                    </button>
                  </div>
                  <p class="comment-text">{{ comment.content }}</p>
                </div>
              </li>
            </ul>
          </div>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from "vue";
import { storeToRefs } from "pinia";
import { useAuthStore } from "../../stores/authStore";
import { noticeService } from "../../services/noticeService";
import { useConfirm } from "../../composables/useConfirm";

const { confirm } = useConfirm();

const props = defineProps({
  notice: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close', 'edit', 'deleted', 'refreshList']);

// 관리자 여부 확인 (storeToRefs로 반응성 유지)
const authStore = useAuthStore();
const { isAdmin } = storeToRefs(authStore);

// 공지사항 상세 데이터 (API에서 조회한 최신 데이터)
const noticeDetail = ref(null);

const newComment = ref('');
const isSubmittingComment = ref(false);

// 좋아요 상태
const noticeLiked = ref(false);
const noticeLikes = ref(0);
const isTogglingLike = ref(false);

// 댓글 데이터
const comments = ref([]);
const commentsLoading = ref(false);

// 모달 열릴 때 데이터 로드
watch(() => props.notice, async (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden';
    newComment.value = '';
    
    // 상세 조회 API 호출 (조회수 증가)
    await fetchNoticeDetail();
    
    // 좋아요 상태 및 댓글 로드
    await Promise.all([
      fetchLikeStatus(),
      fetchComments()
    ]);
  } else {
    document.body.style.overflow = '';
    noticeDetail.value = null;
    comments.value = [];
  }
});

// 공지사항 상세 조회 (조회수 증가)
async function fetchNoticeDetail() {
  if (!props.notice?.noticeId) return;
  
  try {
    const response = await noticeService.getNoticeById(props.notice.noticeId);
    noticeDetail.value = response;
  } catch (error) {
    console.error('공지사항 상세 조회 실패:', error);
    // 실패 시 props.notice 사용
    noticeDetail.value = props.notice;
  }
}

// 모달 닫기 (목록 새로고침 요청)
function handleClose() {
  emit('refreshList');
  emit('close');
}

// 좋아요 상태 조회
async function fetchLikeStatus() {
  if (!props.notice?.noticeId) return;
  
  try {
    const response = await noticeService.getLikeStatus(props.notice.noticeId);
    noticeLiked.value = response.isLiked || response.liked || false;
    noticeLikes.value = response.likeCount || 0;
  } catch (error) {
    console.error('좋아요 상태 조회 실패:', error);
    noticeLiked.value = false;
    noticeLikes.value = props.notice.likeCount || 0;
  }
}

// 댓글 목록 조회
async function fetchComments() {
  if (!props.notice?.noticeId) return;
  
  commentsLoading.value = true;
  try {
    const response = await noticeService.getComments(props.notice.noticeId);
    comments.value = response.comments || [];
  } catch (error) {
    console.error('댓글 조회 실패:', error);
    comments.value = [];
  } finally {
    commentsLoading.value = false;
  }
}

// 날짜 포맷팅 (전체)
function formatFullDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${year}년 ${month}월 ${day}일`;
}

// 댓글 날짜 포맷팅 (간략)
function formatCommentDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${month}/${day}`;
}

// 댓글 작성
async function addComment() {
  if (!newComment.value.trim() || isSubmittingComment.value) return;
  
  isSubmittingComment.value = true;
  try {
    await noticeService.createComment(props.notice.noticeId, newComment.value.trim());
    newComment.value = '';
    await fetchComments(); // 댓글 목록 새로고침
  } catch (error) {
    console.error('댓글 작성 실패:', error);
    alert('댓글 작성에 실패했습니다.');
  } finally {
    isSubmittingComment.value = false;
  }
}

// 댓글 삭제
async function deleteComment(commentId) {
  const confirmed = await confirm({
    title: '댓글 삭제',
    message: '이 댓글을 삭제하시겠습니까?',
    type: 'danger',
    confirmText: '삭제'
  });
  if (!confirmed) return;
  
  try {
    await noticeService.deleteComment(commentId);
    await fetchComments(); // 댓글 목록 새로고침
  } catch (error) {
    console.error('댓글 삭제 실패:', error);
    alert('댓글 삭제에 실패했습니다.');
  }
}

// 좋아요 토글
async function toggleNoticeLike() {
  if (isTogglingLike.value) return;
  
  isTogglingLike.value = true;
  try {
    const response = await noticeService.toggleLike(props.notice.noticeId);
    noticeLiked.value = response.isLiked ?? response.liked ?? false;
    noticeLikes.value = response.likeCount || 0;
  } catch (error) {
    console.error('좋아요 토글 실패:', error);
    alert('좋아요 처리에 실패했습니다.');
  } finally {
    isTogglingLike.value = false;
  }
}

// 공지사항 수정 (관리자 전용)
function handleEdit() {
  emit('edit', noticeDetail.value);
}

// 공지사항 삭제 (관리자 전용)
async function handleDelete() {
  const confirmed = await confirm({
    title: '공지사항 삭제',
    message: '정말 이 공지사항을 삭제하시겠습니까?',
    type: 'danger',
    confirmText: '삭제'
  });
  if (!confirmed) return;
  
  try {
    await noticeService.deleteNotice(noticeDetail.value.noticeId);
    emit('deleted');
    emit('close');
  } catch (error) {
    console.error('공지사항 삭제 실패:', error);
    alert('공지사항 삭제에 실패했습니다.');
  }
}

// ESC 키로 모달 닫기
function handleKeydown(event) {
  if (event.key === 'Escape' && noticeDetail.value) {
    handleClose();
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
  padding: 1.5rem 0.5rem 1.5rem 2rem;
  width: 100%;
  max-width: 540px;
  max-height: 85vh;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  position: relative;
  display: flex;
  flex-direction: column;
}

/* 고정 헤더 영역 */
.notice-modal-fixed {
  flex-shrink: 0;
  padding-right: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 1rem;
}

/* 스크롤 영역 */
.notice-modal-body {
  flex: 1;
  overflow-y: auto;
  padding-right: 1rem;
  min-height: 0;
  scrollbar-width: thin;
  scrollbar-color: var(--color-purple) transparent;
}

.notice-modal-body::-webkit-scrollbar {
  width: 6px;
}

.notice-modal-body::-webkit-scrollbar-track {
  background: transparent;
}

.notice-modal-body::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, var(--color-purple), var(--color-pink));
  border-radius: 6px;
}

.notice-modal-body::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, var(--color-purple-dark, #b39ddb), var(--color-pink-dark, #f48fb1));
}

/* 스크롤바 화살표 버튼 제거 */
.notice-modal-body::-webkit-scrollbar-button {
  display: none;
  height: 0;
  width: 0;
}

.notice-modal-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  font-family: 'Dongle', sans-serif;
}

.notice-modal-badge {
  background: var(--gradient-purple-blue);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 1.4rem;
  font-weight: 700;
}

.notice-modal-date {
  color: #999;
  font-size: 1.5rem;
}

.notice-modal-views {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #aaa;
  font-size: 1.5rem;
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
  margin: 0;
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
  font-family: sans-serif;
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

.notice-like-btn.liked:hover {
  border-color: #ff4757;
  color: #ff4757;
  background: #ffe8ea;
}

.notice-like-btn.liked svg {
  animation: heartBeat 0.3s ease;
}

.notice-like-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  font-family: 'Dongle', sans-serif;
}

.comments-title {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 1.7rem;
  font-weight: 700;
  color: #333;
}

.comments-title svg {
  color: var(--color-purple, #cdb4db);
}

.comments-count {
  background: var(--color-purple-20, rgba(205, 180, 219, 0.2));
  color: var(--color-text-primary, #4c2b7b);
  padding: 0.15rem 0.5rem;
  border-radius: 10px;
  font-size: 0.8rem;
  font-weight: 700;
  font-family: sans-serif;
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
  width: 60px;
  height: 60px;
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

/* 댓글 로딩/없음 */
.comments-loading,
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
  align-items: center;
  gap: 0.75rem;
}

.comment-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--gradient-purple-blue);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
  flex-shrink: 0;
  font-family: 'Dongle', sans-serif;
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
  font-size: 1.2rem;
  color: #999;
  font-family: 'Dongle';
}

.comment-delete-btn {
  margin-left: auto;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: #bbb;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s ease;
  opacity: 0;
}

.comment-item:hover .comment-delete-btn {
  opacity: 1;
}

.comment-delete-btn:hover {
  background: #fef2f2;
  color: #ef4444;
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
  .notice-modal-overlay {
    padding: 0.75rem;
  }

  .notice-modal {
    padding: 1.25rem 0.5rem 1.25rem 1.5rem;
    max-width: 100%;
    max-height: 90vh;
    border-radius: 20px;
  }

  .notice-modal-body {
    padding-right: 0.75rem;
  }

  .notice-modal-title {
    font-size: 1.1rem;
    padding-right: 1.5rem;
  }

  .notice-modal-header {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .admin-actions {
    margin-left: auto;
  }
}

@media (max-width: 480px) {
  .notice-modal-overlay {
    padding: 0.75rem;
    align-items: center;
  }

  .notice-modal {
    padding: 1rem 0.4rem 1rem 1.25rem;
    border-radius: 16px;
    max-height: 85vh;
  }

  .notice-modal-body {
    padding-right: 0.6rem;
  }

  .notice-modal-header {
    margin-bottom: 0.75rem;
    gap: 0.35rem 0.5rem;
    flex-wrap: wrap;
  }

  .notice-modal-badge {
    padding: 0.15rem 0.5rem;
    font-size: 1.1rem;
  }

  .notice-modal-date {
    font-size: 1.2rem;
  }

  .notice-modal-views {
    font-size: 1.2rem;
  }

  .notice-modal-views svg {
    width: 11px;
    height: 11px;
  }

  .admin-actions {
    width: 100%;
    justify-content: flex-start;
    margin-top: 0.25rem;
  }

  .admin-btn {
    width: 30px;
    height: 30px;
  }

  .admin-btn svg {
    width: 13px;
    height: 13px;
  }

  .notice-modal-title {
    font-size: 1rem;
    margin-bottom: 1rem;
    padding-right: 0;
  }

  .notice-modal-content {
    font-size: 0.875rem;
    line-height: 1.7;
  }

  /* 좋아요 섹션 */
  .notice-like-section {
    padding: 1rem 0;
  }

  .notice-like-btn {
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
  }

  .notice-like-btn svg {
    width: 16px;
    height: 16px;
  }

  /* 댓글 섹션 */
  .comments-section {
    margin-top: 1rem;
  }

  .comments-header {
    margin-bottom: 0.75rem;
  }

  .comments-title {
    font-size: 1.5rem;
  }

  .comments-count {
    font-size: 0.75rem;
    padding: 0.1rem 0.4rem;
  }

  .comment-input-wrapper {
    gap: 0.4rem;
    margin-bottom: 1rem;
  }

  .comment-input {
    padding: 0.6rem 0.875rem;
    font-size: 0.85rem;
    border-radius: 14px;
    min-height: 70px;
  }

  .comment-submit-btn {
    width: 50px;
    height: 50px;
  }

  .comment-submit-btn svg {
    width: 16px;
    height: 16px;
  }

  .comments-loading,
  .comments-empty {
    font-size: 0.85rem;
    padding: 1.5rem 1rem;
    border-radius: 12px;
  }

  .comments-list {
    gap: 0.75rem;
  }

  .comment-item {
    gap: 0.6rem;
  }

  .comment-avatar {
    width: 36px;
    height: 36px;
    font-size: 1.3rem;
  }

  .comment-meta {
    gap: 0.4rem;
    margin-bottom: 0.15rem;
  }

  .comment-author {
    font-size: 0.8rem;
  }

  .comment-date {
    font-size: 1.1rem;
  }

  .comment-delete-btn {
    width: 24px;
    height: 24px;
    opacity: 0.7;
  }

  .comment-delete-btn svg {
    width: 14px;
    height: 14px;
  }

  .comment-text {
    font-size: 0.85rem;
    line-height: 1.4;
  }
}
</style>

