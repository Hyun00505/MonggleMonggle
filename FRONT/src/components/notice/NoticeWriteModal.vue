<template>
  <Teleport to="body">
    <transition name="modal">
      <div v-if="show" class="notice-modal-overlay" @click.self="$emit('close')">
        <div class="notice-modal">
          <button class="icon-btn icon-btn-absolute" @click="$emit('close')" aria-label="닫기">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
          
          <!-- 헤더 -->
          <div class="notice-modal-header">
            <span class="notice-modal-badge">{{ isEditMode ? '공지 수정' : '공지 작성' }}</span>
          </div>
          
          <!-- 제목 입력 -->
          <div class="form-group">
            <label for="notice-title">제목</label>
            <input 
              id="notice-title"
              v-model="title"
              type="text"
              placeholder="공지사항 제목을 입력하세요"
              class="form-input"
              :disabled="isSubmitting"
            />
          </div>
          
          <!-- 내용 입력 -->
          <div class="form-group">
            <label for="notice-content">내용</label>
            <textarea 
              id="notice-content"
              v-model="content"
              placeholder="공지사항 내용을 입력하세요"
              class="form-textarea"
              rows="8"
              :disabled="isSubmitting"
            ></textarea>
          </div>
          
          <!-- 버튼 영역 -->
          <div class="modal-actions">
            <button class="btn-cancel" @click="$emit('close')" :disabled="isSubmitting">
              취소
            </button>
            <button class="btn-submit" @click="handleSubmit" :disabled="isSubmitting || !isValid">
              <span v-if="isSubmitting">{{ isEditMode ? '수정 중...' : '등록 중...' }}</span>
              <span v-else>{{ isEditMode ? '수정' : '등록' }}</span>
            </button>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from "vue";
import { noticeService } from "../../services/noticeService";

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  editNotice: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close', 'created', 'updated']);

const title = ref('');
const content = ref('');
const isSubmitting = ref(false);

// 수정 모드 여부
const isEditMode = computed(() => !!props.editNotice);

const isValid = computed(() => {
  return title.value.trim().length > 0 && content.value.trim().length > 0;
});

// 모달이 열릴 때마다 폼 초기화 또는 수정 데이터 로드
watch(() => props.show, (newVal) => {
  if (newVal) {
    if (props.editNotice) {
      // 수정 모드: 기존 데이터 로드
      title.value = props.editNotice.title || '';
      content.value = props.editNotice.content || '';
    } else {
      // 작성 모드: 폼 초기화
      title.value = '';
      content.value = '';
    }
    document.body.style.overflow = 'hidden';
  } else {
    document.body.style.overflow = '';
  }
});

async function handleSubmit() {
  if (!isValid.value || isSubmitting.value) return;
  
  isSubmitting.value = true;
  try {
    if (isEditMode.value) {
      // 수정 모드
      await noticeService.updateNotice(props.editNotice.noticeId, {
        title: title.value.trim(),
        content: content.value.trim()
      });
      emit('updated');
    } else {
      // 작성 모드
      await noticeService.createNotice({
        title: title.value.trim(),
        content: content.value.trim()
      });
      emit('created');
    }
    emit('close');
  } catch (error) {
    console.error(isEditMode.value ? '공지사항 수정 실패:' : '공지사항 등록 실패:', error);
    alert(isEditMode.value ? '공지사항 수정에 실패했습니다.' : '공지사항 등록에 실패했습니다.');
  } finally {
    isSubmitting.value = false;
  }
}

// ESC 키로 모달 닫기
function handleKeydown(event) {
  if (event.key === 'Escape' && props.show) {
    emit('close');
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown);
  document.body.style.overflow = '';
});
</script>

<style scoped>
/* 모달 오버레이 - NoticeModal과 동일 */
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

/* 모달 본체 - NoticeModal과 동일 */
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

/* 닫기 버튼 */
.icon-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  background: #e5e5e5;
}

.icon-btn svg {
  width: 18px;
  height: 18px;
  stroke: #666;
}

.icon-btn-absolute {
  position: absolute;
  top: 1rem;
  right: 1rem;
}

/* 헤더 - NoticeModal과 동일 */
.notice-modal-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.notice-modal-badge {
  background: var(--gradient-purple-blue);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 1.2rem;
  font-weight: 700;
  font-family: 'Dongle', sans-serif;
}

/* 폼 그룹 */
.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  font-family: 'Dongle', sans-serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
}

/* 입력 필드 - NoticeModal 댓글 입력과 유사 */
.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  font-size: 0.95rem;
  border: 2px solid #f0f0f0;
  border-radius: 16px;
  background: white;
  transition: all 0.2s ease;
  font-family: inherit;
  box-sizing: border-box;
  outline: none;
}

.form-input:focus,
.form-textarea:focus {
  border-color: var(--color-purple, #cdb4db);
  box-shadow: 0 0 0 4px var(--color-purple-20, rgba(205, 180, 219, 0.2));
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: #bbb;
}

.form-textarea {
  resize: vertical;
  min-height: 180px;
  line-height: 1.8;
}

/* 버튼 영역 */
.modal-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 0.35rem 1.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  font-family: 'Dongle', sans-serif !important;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover:not(:disabled) {
  background: #e5e5e5;
}

.btn-submit {
  background: var(--gradient-purple-blue);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--shadow-purple, rgba(205, 180, 219, 0.4));
}

.btn-submit:disabled,
.btn-cancel:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 모달 애니메이션 - NoticeModal과 동일 */
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

/* 반응형 - NoticeModal과 동일 */
@media (max-width: 768px) {
  .notice-modal {
    padding: 1.5rem;
    max-width: 90%;
  }
}

@media (max-width: 480px) {
  .notice-modal {
    padding: 1.25rem;
    border-radius: 20px;
  }

  .modal-actions {
    flex-direction: column-reverse;
  }

  .btn-cancel,
  .btn-submit {
    width: 100%;
  }
}
</style>
