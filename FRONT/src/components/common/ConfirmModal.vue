<template>
  <Teleport to="body">
    <transition name="confirm-modal">
      <div v-if="show" class="confirm-modal-overlay" @click.self="handleCancel">
        <div class="confirm-modal">
          <!-- 닫기 버튼 -->
          <button class="icon-btn icon-btn-absolute" @click="handleCancel" aria-label="닫기">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
          <div class="confirm-icon" :class="iconType">
            <!-- 경고 아이콘 (기본) -->
            <svg v-if="type === 'warning'" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            <!-- 삭제 아이콘 -->
            <svg v-else-if="type === 'danger'" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              <line x1="10" y1="11" x2="10" y2="17"></line>
              <line x1="14" y1="11" x2="14" y2="17"></line>
            </svg>
            <!-- 정보 아이콘 -->
            <svg v-else-if="type === 'info'" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            <!-- 성공 아이콘 -->
            <svg v-else-if="type === 'success'" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
          </div>
          <h3 class="confirm-title">{{ title }}</h3>
          <p class="confirm-message" v-html="formattedMessage"></p>
          <p v-if="subMessage" class="confirm-sub">{{ subMessage }}</p>
          <div class="confirm-actions">
            <button v-if="showCancel" class="confirm-btn cancel" @click="handleCancel">
              {{ cancelText }}
            </button>
            <button class="confirm-btn ok" :class="okButtonClass" @click="handleConfirm">
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '확인'
  },
  message: {
    type: String,
    default: ''
  },
  subMessage: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'warning', // warning, danger, info, success
    validator: (value) => ['warning', 'danger', 'info', 'success'].includes(value)
  },
  confirmText: {
    type: String,
    default: '확인'
  },
  cancelText: {
    type: String,
    default: '취소'
  },
  showCancel: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['confirm', 'cancel', 'close']);

// 메시지에서 \n을 <br>로 변환
const formattedMessage = computed(() => {
  return props.message.replace(/\n/g, '<br>');
});

// 아이콘 타입에 따른 클래스
const iconType = computed(() => {
  return {
    'icon-warning': props.type === 'warning',
    'icon-danger': props.type === 'danger',
    'icon-info': props.type === 'info',
    'icon-success': props.type === 'success'
  };
});

// 확인 버튼 클래스
const okButtonClass = computed(() => {
  return {
    'btn-danger': props.type === 'danger',
    'btn-primary': props.type !== 'danger'
  };
});

function handleConfirm() {
  emit('confirm');
  emit('close');
}

function handleCancel() {
  emit('cancel');
  emit('close');
}
</script>

<style scoped>
.confirm-modal-overlay {
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
  z-index: 10000;
  padding: 1rem;
}

.confirm-modal {
  position: relative;
  background: white;
  border-radius: 24px;
  padding: 2rem;
  width: 100%;
  max-width: 340px;
  text-align: center;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.confirm-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 1rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-icon.icon-warning {
  background: linear-gradient(135deg, #fff5f5, #ffe8ea);
  color: #f97316;
}

.confirm-icon.icon-danger {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  color: #ef4444;
}

.confirm-icon.icon-info {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  color: #3b82f6;
}

.confirm-icon.icon-success {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  color: #22c55e;
}

.confirm-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 0.75rem;
}

.confirm-message {
  font-size: 0.95rem;
  color: #666;
  margin: 0 0 0.25rem;
  line-height: 1.6;
}

.confirm-sub {
  font-size: 0.9rem;
  color: #888;
  margin: 0 0 1.5rem;
}

.confirm-message:last-of-type {
  margin-bottom: 1.5rem;
}

.confirm-actions {
  display: flex;
  gap: 0.75rem;
}

.confirm-btn {
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 12px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.confirm-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.confirm-btn.cancel:hover {
  background: #eee;
}

.confirm-btn.ok.btn-primary {
  background: var(--gradient-purple-blue, linear-gradient(135deg, #cdb4db, #a2d2ff));
  color: white;
}

.confirm-btn.ok.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(205, 180, 219, 0.4);
}

.confirm-btn.ok.btn-danger {
  background: linear-gradient(135deg, #f87171, #ef4444);
  color: white;
}

.confirm-btn.ok.btn-danger:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

/* 모달 애니메이션 */
.confirm-modal-enter-active,
.confirm-modal-leave-active {
  transition: opacity 0.3s ease;
}

.confirm-modal-enter-active .confirm-modal,
.confirm-modal-leave-active .confirm-modal {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.confirm-modal-enter-from,
.confirm-modal-leave-to {
  opacity: 0;
}

.confirm-modal-enter-from .confirm-modal {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

.confirm-modal-leave-to .confirm-modal {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

@media (max-width: 480px) {
  .confirm-modal {
    padding: 1.5rem;
    border-radius: 20px;
    max-width: 300px;
  }

  .confirm-icon {
    width: 50px;
    height: 50px;
  }

  .confirm-icon svg {
    width: 26px;
    height: 26px;
  }

  .confirm-title {
    font-size: 1.1rem;
  }

  .confirm-message {
    font-size: 0.9rem;
  }
}
</style>

