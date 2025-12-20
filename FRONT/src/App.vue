<script setup>
import { RouterView } from 'vue-router';
import ConfirmModal from './components/common/ConfirmModal.vue';
import { useConfirm } from './composables/useConfirm';

const { state, handleConfirm, handleCancel, handleClose } = useConfirm();
</script>

<template>
  <RouterView />
  
  <!-- 전역 확인 모달 -->
  <ConfirmModal
    :show="state.show"
    :title="state.title"
    :message="state.message"
    :sub-message="state.subMessage"
    :type="state.type"
    :confirm-text="state.confirmText"
    :cancel-text="state.cancelText"
    :show-cancel="state.showCancel"
    @confirm="handleConfirm"
    @cancel="handleCancel"
    @close="handleClose"
  />
</template>
