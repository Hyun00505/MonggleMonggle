import { ref, reactive } from 'vue';

// 싱글톤 상태 (앱 전체에서 하나의 모달만 사용)
const state = reactive({
  show: false,
  title: '확인',
  message: '',
  subMessage: '',
  type: 'warning',
  confirmText: '확인',
  cancelText: '취소',
  showCancel: true,
  resolve: null
});

export function useConfirm() {
  /**
   * 확인 모달을 표시하고 Promise를 반환
   * @param {Object} options - 모달 옵션
   * @param {string} options.title - 모달 제목
   * @param {string} options.message - 모달 메시지
   * @param {string} options.subMessage - 부가 메시지
   * @param {string} options.type - 모달 타입 (warning, danger, info, success)
   * @param {string} options.confirmText - 확인 버튼 텍스트
   * @param {string} options.cancelText - 취소 버튼 텍스트
   * @param {boolean} options.showCancel - 취소 버튼 표시 여부
   * @returns {Promise<boolean>} - 확인: true, 취소: false
   */
  function confirm(options = {}) {
    return new Promise((resolve) => {
      state.title = options.title || '확인';
      state.message = options.message || '';
      state.subMessage = options.subMessage || '';
      state.type = options.type || 'warning';
      state.confirmText = options.confirmText || '확인';
      state.cancelText = options.cancelText || '취소';
      state.showCancel = options.showCancel !== false;
      state.resolve = resolve;
      state.show = true;
    });
  }

  function handleConfirm() {
    state.show = false;
    if (state.resolve) {
      state.resolve(true);
      state.resolve = null;
    }
  }

  function handleCancel() {
    state.show = false;
    if (state.resolve) {
      state.resolve(false);
      state.resolve = null;
    }
  }

  function handleClose() {
    state.show = false;
    if (state.resolve) {
      state.resolve(false);
      state.resolve = null;
    }
  }

  return {
    state,
    confirm,
    handleConfirm,
    handleCancel,
    handleClose
  };
}

