<template>
  <header class="app-header">
    <div class="header-content">
      <!-- 네비게이션 메뉴 -->
      <nav class="nav-menu">
        <button @click="toggleMenu" class="glass-btn menu-btn" aria-label="Menu">
          <span class="btn-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="3" y1="12" x2="21" y2="12"></line>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
          </span>
          <span class="btn-text">메뉴</span>
        </button>

        <div v-if="showMenu" class="dropdown-menu">
          <router-link to="/calendar" class="menu-item" @click="closeMenu">
            <span class="menu-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
            </span>
            <span>캘린더</span>
          </router-link>
          <router-link to="/gallery" class="menu-item" @click="closeMenu">
            <span class="menu-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21 15 16 10 5 21"></polyline>
              </svg>
            </span>
            <span>갤러리</span>
          </router-link>
          <router-link to="/monthly-analysis" class="menu-item" @click="closeMenu">
            <span class="menu-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="20" x2="18" y2="10"></line>
                <line x1="12" y1="20" x2="12" y2="4"></line>
                <line x1="6" y1="20" x2="6" y2="14"></line>
              </svg>
            </span>
            <span>월별 분석</span>
          </router-link>
          <router-link to="/ranking" class="menu-item" @click="closeMenu">
            <span class="menu-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"></path>
                <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"></path>
                <path d="M4 22h16"></path>
                <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"></path>
                <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"></path>
                <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"></path>
              </svg>
            </span>
            <span>랭킹</span>
          </router-link>
          <div class="menu-divider"></div>
          <router-link to="/landing" class="menu-item about-item" @click="closeMenu">
            <span class="menu-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M12 16v-4"></path>
                <path d="M12 8h.01"></path>
              </svg>
            </span>
            <span>서비스 소개</span>
          </router-link>
        </div>
      </nav>

      <!-- 공지사항 알림 -->
      <NoticeDropdown v-if="!isMinimalHeader" ref="noticeDropdownRef" />

      <div class="spacer"></div>

      <div v-if="!isMinimalHeader" class="coin-wrapper" ref="coinWrapper" @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave">
        <button type="button" class="glass-btn coin-container" aria-label="AI 티켓 (해몽 가능 횟수)" :aria-expanded="showCoinInfo" @click="handleCoinClick">
          <span class="coin-icon" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v2a2 2 0 0 0-2 2 2 2 0 0 0 2 2v2a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-2a2 2 0 0 0 2-2 2 2 0 0 0-2-2Z"></path>
              <path d="M9 5v14"></path>
              <path d="M15 5v14"></path>
            </svg>
          </span>
          <span class="coin-label">AI 티켓</span>
          <span class="coin-text">{{ displayCoin }}</span>
        </button>

        <transition name="popover">
          <div v-if="showCoinInfo" class="glass-popover coin-popover" role="status">
            <ul class="rule-list">
              <li>
                꿈 해몽 시
                <strong class="highlight-text">1개</strong>
                차감
              </li>
              <li>
                꿈 이미지 생성 시
                <strong class="highlight-text">2개</strong>
                차감
              </li>
            </ul>

            <div class="reset-info">
              <span class="reset-label">다음 충전까지</span>
              <span class="time-pill">{{ countdown }}</span>
            </div>

            <div class="glass-progress">
              <span class="glass-progress-fill" :style="{ width: resetProgress + '%' }"></span>
            </div>

            <p class="reset-footnote">매일 자정(UTC+9)에 티켓이 새로고침 돼요</p>
          </div>
        </transition>
      </div>

      <button v-if="!isMinimalHeader" @click="$emit('logout')" class="glass-btn logout-btn" aria-label="Logout">
        <span class="btn-text">Logout</span>
        <div class="btn-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
        </div>
      </button>

      <button v-if="!isMinimalHeader" @click="$emit('navigate-mypage')" class="glass-btn profile-btn" aria-label="My Page">
        <span class="btn-text">{{ displayName }}</span>
        <div class="btn-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
        </div>
      </button>
    </div>
  </header>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import { useAuthStore } from "../stores/authStore";
import NoticeDropdown from "../components/notice/NoticeDropdown.vue";

defineEmits(["navigate-mypage", "logout"]);

const route = useRoute();
const isMinimalHeader = computed(() => route.meta.minimalHeader === true);

const authStore = useAuthStore();
const { currentUser } = storeToRefs(authStore);

const showMenu = ref(false);
const showCoinInfo = ref(false);
const coinWrapper = ref(null);
const countdown = ref("00:00:00");
const resetProgress = ref(0);
const isTouchDevice = ref(false);
const noticeDropdownRef = ref(null);
let countdownTimer = null;
let hoverTimeout = null;

const displayName = computed(() => {
  const name = currentUser.value?.name;
  return name && String(name).trim() ? name + " 님" : "My Page";
});

const displayCoin = computed(() => {
  const coin = currentUser.value?.coin;
  if (coin === undefined || coin === null) return "-";
  return coin;
});

function toggleMenu() {
  showCoinInfo.value = false;
  noticeDropdownRef.value?.closeDropdown();
  showMenu.value = !showMenu.value;
}

function closeMenu() {
  showMenu.value = false;
}

function toggleCoinInfo() {
  showMenu.value = false;
  noticeDropdownRef.value?.closeDropdown();
  showCoinInfo.value = !showCoinInfo.value;
}

function handleCoinClick() {
  toggleCoinInfo();
}

function handleMouseEnter() {
  if (!isTouchDevice.value) {
    if (hoverTimeout) clearTimeout(hoverTimeout);
    showMenu.value = false;
    noticeDropdownRef.value?.closeDropdown();
    showCoinInfo.value = true;
  }
}

function handleMouseLeave() {
  if (!isTouchDevice.value) {
    hoverTimeout = setTimeout(() => {
      showCoinInfo.value = false;
    }, 150);
  }
}

function handleOutsideClick(event) {
  if (!isTouchDevice.value) return;
  if (!coinWrapper.value) return;
  if (coinWrapper.value.contains(event.target)) return;
  showCoinInfo.value = false;
}

function getKstNow() {
  const now = new Date();
  const utcMs = now.getTime() + now.getTimezoneOffset() * 60000;
  return new Date(utcMs + 9 * 60 * 60000);
}

function updateCountdown() {
  const nowKst = getKstNow();
  const resetAt = new Date(nowKst);
  resetAt.setHours(24, 0, 0, 0);

  const diff = resetAt.getTime() - nowKst.getTime();
  const startOfDay = new Date(nowKst);
  startOfDay.setHours(0, 0, 0, 0);
  const elapsed = nowKst.getTime() - startOfDay.getTime();
  const total = 24 * 60 * 60 * 1000;

  resetProgress.value = Math.min(100, Math.max(0, (elapsed / total) * 100));

  if (diff <= 0) {
    countdown.value = "00:00:00";
    return;
  }

  const hours = String(Math.floor(diff / (1000 * 60 * 60))).padStart(2, "0");
  const minutes = String(Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))).padStart(2, "0");
  const seconds = String(Math.floor((diff % (1000 * 60)) / 1000)).padStart(2, "0");

  countdown.value = `${hours}:${minutes}:${seconds}`;
}

onMounted(() => {
  const hoverQuery = window.matchMedia('(hover: hover) and (pointer: fine)');
  isTouchDevice.value = !hoverQuery.matches;
  
  hoverQuery.addEventListener('change', (e) => {
    isTouchDevice.value = !e.matches;
  });
  
  updateCountdown();
  countdownTimer = setInterval(updateCountdown, 1000);
  document.addEventListener("click", handleOutsideClick);
});

onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
  if (hoverTimeout) {
    clearTimeout(hoverTimeout);
    hoverTimeout = null;
  }
  document.removeEventListener("click", handleOutsideClick);
});
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  padding: 1rem 2rem;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.nav-menu {
  position: relative;
}

.menu-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
}

.menu-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 0.5rem);
  left: 0;
  background: white;
  border-radius: 20px;
  padding: 0.5rem;
  min-width: 200px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: slideDown 0.2s ease;
  z-index: 1000;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  text-decoration: none;
  color: #333;
  font-family: "Nunito", sans-serif;
  font-weight: 600;
  transition: all 0.2s;
}

.menu-item:hover {
  background: linear-gradient(135deg, #f8f9ff, #e8f0fe);
  transform: translateX(5px);
}

.menu-item.router-link-active {
  background: linear-gradient(135deg, var(--color-blue), var(--color-blue-dark));
  color: white;
}

.menu-divider {
  height: 1px;
  background: #F0F0F0;
  margin: 0.3rem 0.5rem;
}

.about-item {
  color: var(--color-purple-dark);
}

.about-item:hover {
  background: linear-gradient(135deg, var(--color-purple-light), var(--color-pink-light));
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
}

.menu-icon svg {
  flex-shrink: 0;
}

.spacer {
  flex: 1;
}

.coin-wrapper {
  position: relative;
}

.coin-container {
  cursor: default;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  transition: all 0.3s ease;
  color: white;
}

.coin-container:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.coin-container:focus-visible {
  outline: 2px solid rgba(255, 255, 255, 0.6);
  outline-offset: 3px;
}

.coin-label {
  font-weight: 700;
  font-size: 0.9rem;
  font-family: "Nunito", sans-serif;
}

.coin-icon {
  font-size: 1rem;
  display: flex;
  align-items: center;
  width: 20px;
}

.coin-icon svg {
  display: block;
  position: absolute;
  top: -9px;
}

.coin-text {
  font-family: "Nunito", sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  color: #f3e366;
}

/* coin-popover: 위치 및 사이즈 오버라이드 */
.coin-popover {
  top: calc(100% + 10px);
  right: 0;
  font-family: 'Nunito', sans-serif !important;
  width: 270px;
  cursor: default;
}

.rule-list {
  list-style: none;
  padding: 0rem 0 0.5rem 0;
  margin: 0;
  display: grid;
  gap: 0.35rem;
  color: #666;
  font-weight: 600;
  line-height: 1.5;
  font-size: 0.95rem;
}

.reset-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.5rem;
  gap: 0.8rem;
}

.reset-label {
  font-size: 0.9rem;
  font-weight: 700;
  color: #666;
}

.coin-popover .glass-progress {
  margin-top: 0.75rem;
}

.reset-footnote {
  margin: 0.5rem 0 0;
  font-size: 0.8rem;
  color: #999;
  text-align: center;
}

@media (max-width: 768px) {
  .app-header {
    padding: 1rem;
  }

  .btn-text {
    display: none;
  }

  .menu-btn,
  .coin-container,
  .profile-btn,
  .logout-btn {
    padding: 0.5rem;
    border-radius: 16px;
    min-width: 42px;
    height: 42px;
  }

  .coin-text {
    font-size: 0.85rem;
  }

  .dropdown-menu {
    left: 0;
    right: auto;
    min-width: 180px;
    max-width: calc(100vw - 2rem);
  }

  .coin-popover {
    right: -20px;
  }

  .coin-popover::before {
    right: 38px;
  }
}

@media (max-width: 480px) {
  .app-header {
    padding: 0.75rem;
  }

  .header-content {
    gap: 0.5rem;
  }

  .menu-btn,
  .coin-container,
  .profile-btn,
  .logout-btn {
    min-width: 38px;
    height: 38px;
    padding: 0.4rem;
  }

  .dropdown-menu {
    min-width: 160px;
    padding: 0.4rem;
  }

  .menu-item {
    padding: 0.6rem 0.8rem;
    font-size: 0.9rem;
  }

  .menu-icon {
    width: 18px;
    height: 18px;
  }

  .menu-icon svg {
    width: 18px;
    height: 18px;
  }
}
</style>
