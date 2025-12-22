<script setup>
import { computed } from 'vue';
import { RouterView, useRoute, useRouter } from 'vue-router';
import CloudBackground from '../components/CloudBackground.vue';
import TheHeader from './TheHeader.vue';
import { useDreamEntriesStore } from '../stores/dreamEntriesStore';
import { useAuthStore } from '../stores/authStore';
import { useConfirm } from '../composables/useConfirm';

const route = useRoute();
const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const authStore = useAuthStore();
const { resetAll } = dreamEntriesStore;
const { confirm } = useConfirm();

const showHeader = computed(() => route.meta.hideHeader !== true);

function handleNavigateMypage() {
  router.push({ name: 'mypage' });
}

async function handleLogout() {
  await authStore.logout();
  resetAll();
  await confirm({
    title: '로그아웃',
    message: '로그아웃 되었습니다.',
    type: 'info',
    confirmText: '확인',
    showCancel: false
  });
  router.push({ name: 'landing' });
}
</script>

<template>
  <CloudBackground />

  <TheHeader
    v-if="showHeader"
    @navigate-mypage="handleNavigateMypage"
    @logout="handleLogout"
  />

  <main class="main-container">
    <RouterView />
  </main>
</template>

<style scoped>
.main-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 76px 20px 20px 20px;
  position: relative;
  z-index: 1;
}
</style>


