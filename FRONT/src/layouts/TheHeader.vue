<template>
  <header class="app-header">
    <div class="header-content">
      <!-- 네비게이션 메뉴 -->
      <nav class="nav-menu">
        <button @click="toggleMenu" class="menu-btn" aria-label="Menu">
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
            <span class="menu-icon">📅</span>
            <span>캘린더</span>
          </router-link>
          <router-link to="/visualization" class="menu-item" @click="closeMenu">
            <span class="menu-icon">✨</span>
            <span>꿈 시각화</span>
          </router-link>
          <router-link to="/gallery" class="menu-item" @click="closeMenu">
            <span class="menu-icon">🖼️</span>
            <span>갤러리</span>
          </router-link>
          <router-link to="/monthly-analysis" class="menu-item" @click="closeMenu">
            <span class="menu-icon">📊</span>
            <span>월별 분석</span>
          </router-link>
        </div>
      </nav>

      <div class="spacer"></div>

      <button @click="$emit('logout')" class="logout-btn" aria-label="Logout">
        <span class="btn-text">Logout</span>
        <div class="btn-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
        </div>
      </button>

      <button @click="$emit('navigate-mypage')" class="profile-btn" aria-label="My Page">
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
import { ref, computed } from "vue";
import { storeToRefs } from "pinia";
import { useAuthStore } from "../stores/authStore";

defineEmits(["navigate-mypage", "logout"]);

const authStore = useAuthStore();
const { currentUser } = storeToRefs(authStore);

const showMenu = ref(false);

const displayName = computed(() => {
  const name = currentUser.value?.name;
  return name && String(name).trim() ? name + " 님" : "My Page";
});

function toggleMenu() {
  showMenu.value = !showMenu.value;
}

function closeMenu() {
  showMenu.value = false;
}
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
  background: linear-gradient(135deg, #a2d2ff, #bde0fe);
  color: white;
}

.menu-icon {
  font-size: 1.3rem;
}

.spacer {
  flex: 1;
}

.profile-btn,
.logout-btn {
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
  height: 42px;
}

.profile-btn:hover,
.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.btn-text {
  font-family: "Nunito", sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
}

.btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .app-header {
    padding: 1rem;
  }

  .btn-text {
    display: none;
  }

  .menu-btn,
  .profile-btn,
  .logout-btn {
    padding: 0.5rem;
    border-radius: 50%;
    width: 42px;
  }

  .dropdown-menu {
    left: 0;
    right: auto;
    min-width: 180px;
    max-width: calc(100vw - 2rem);
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
  .profile-btn,
  .logout-btn {
    width: 38px;
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
    font-size: 1.1rem;
  }
}
</style>
