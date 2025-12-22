<template>
  <div class="landing-section">
    <!-- Hero Section -->
    <div class="hero">
      <h1 class="service-title">몽글몽글</h1>
      <p class="tagline">당신의 꿈을 기록하고,<br/>AI가 해몽해드려요</p>
      
      <div class="floating-elements">
        <div class="float-star star-1">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
        </div>
        <div class="float-star star-2">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 0l2.5 7.5H22l-6 4.5 2.5 7.5-6-4.5-6 4.5 2.5-7.5-6-4.5h7.5z"/>
          </svg>
        </div>
        <div class="float-cloud cloud-1">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19.35 10.04A7.49 7.49 0 0012 4C9.11 4 6.6 5.64 5.35 8.04A5.994 5.994 0 000 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z"/>
          </svg>
        </div>
        <div class="float-cloud cloud-2">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="currentColor">
            <path d="M21 16.5c0 .38-.21.71-.53.88l-7.9 4.44c-.16.12-.36.18-.57.18s-.41-.06-.57-.18l-7.9-4.44A.991.991 0 013 16.5v-9c0-.38.21-.71.53-.88l7.9-4.44c.16-.12.36-.18.57-.18s.41.06.57.18l7.9 4.44c.32.17.53.5.53.88v9zM12 4.15L5 8.09v7.82l7 3.94 7-3.94V8.09l-7-3.94z"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- Features Section -->
    <div class="features">
      <div class="feature-card" v-for="(feature, index) in features" :key="index" :style="{ animationDelay: `${index * 0.1}s` }">
        <div class="feature-icon" v-html="feature.icon"></div>
        <h3 class="feature-title">{{ feature.title }}</h3>
        <p class="feature-desc">{{ feature.description }}</p>
      </div>
    </div>

    <!-- Stats Section -->
    <div class="stats">
      <div class="stat-item" v-for="(stat, index) in stats" :key="index">
        <span class="stat-icon" v-html="stat.icon"></span>
        <span class="stat-value">{{ stat.value }}</span>
        <span class="stat-label">{{ stat.label }}</span>
      </div>
    </div>

    <!-- CTA Section -->
    <div class="cta-section">
      <p class="cta-text">오늘 밤 꾼 꿈, 어떤 의미일까요?</p>
      <button @click="$emit('start')" class="cta-btn">
        <span>시작하기</span>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M5 12h14M12 5l7 7-7 7"/>
        </svg>
      </button>
    </div>

    <!-- Scroll Indicator (Mobile) -->
    <div class="scroll-indicator" @click="$emit('scroll-to-form')">
      <span>로그인</span>
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 5v14M5 12l7 7 7-7"/>
      </svg>
    </div>
  </div>
</template>

<script setup>
defineEmits(['start', 'scroll-to-form']);

// SVG Icons
const icons = {
  book: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>',
  crystal: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M12 2a7 7 0 0 0 0 14 7 7 0 0 0 0-14z" fill="currentColor" opacity="0.3"></path><path d="M2 12h4M18 12h4M12 2v4M12 18v4"></path></svg>',
  palette: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="13.5" cy="6.5" r="1.5" fill="currentColor"></circle><circle cx="17.5" cy="10.5" r="1.5" fill="currentColor"></circle><circle cx="8.5" cy="7.5" r="1.5" fill="currentColor"></circle><circle cx="6.5" cy="12.5" r="1.5" fill="currentColor"></circle><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.555C21.965 6.012 17.461 2 12 2z"></path></svg>',
  chart: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>',
  rainbow: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 17a10 10 0 0 0-20 0"></path><path d="M6 17a6 6 0 0 1 12 0"></path><path d="M10 17a2 2 0 0 1 4 0"></path></svg>',
  trophy: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"></path><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"></path><path d="M4 22h16"></path><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"></path><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"></path><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"></path></svg>',
  moon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>',
  sparkles: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"></path><path d="M5 3v4"></path><path d="M19 17v4"></path><path d="M3 5h4"></path><path d="M17 19h4"></path></svg>',
  gift: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 12 20 22 4 22 4 12"></polyline><rect x="2" y="7" width="20" height="5"></rect><line x1="12" y1="22" x2="12" y2="7"></line><path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"></path><path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"></path></svg>'
};

const features = [
  {
    icon: icons.book,
    title: '꿈 일기',
    description: '매일의 꿈을 캘린더에 기록하고 관리해요'
  },
  {
    icon: icons.crystal,
    title: 'AI 해몽',
    description: 'AI가 당신의 꿈을 분석하고 해석해드려요'
  },
  {
    icon: icons.palette,
    title: '꿈 시각화',
    description: 'AI가 꿈 속 장면을 이미지로 그려드려요'
  },
  {
    icon: icons.chart,
    title: '월별 분석',
    description: '한 달간의 꿈 패턴을 분석해드려요'
  },
  {
    icon: icons.rainbow,
    title: '행운의 색',
    description: '오늘의 운세와 함께 행운의 색을 알려드려요'
  },
  {
    icon: icons.trophy,
    title: '랭킹',
    description: '꿈 기록 순위를 확인해보세요'
  }
];

const stats = [
  { icon: icons.moon, value: '∞', label: '꿈' },
  { icon: icons.sparkles, value: 'AI', label: '해몽' },
  { icon: icons.gift, value: '무료', label: '시작' }
];
</script>

<style scoped>
.landing-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  min-height: 100%;
  color: white;
  position: relative;
  overflow: hidden;
}

/* Hero Section */
.hero {
  text-align: center;
  margin-bottom: 3rem;
  position: relative;
}

.service-title {
  font-family: 'Dongle', sans-serif;
  font-size: 6rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-purple), var(--color-pink), var(--color-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  line-height: 1.1;
  letter-spacing: 3px;
  filter: drop-shadow(0 4px 8px rgba(255, 255, 255, 0.2));
  animation: titleFloat 3s ease-in-out infinite;
}

@keyframes titleFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.tagline {
  font-size: 1.3rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 0.5rem;
  line-height: 1.6;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* Floating Elements */
.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: -1;
}

.float-star, .float-cloud {
  position: absolute;
  opacity: 0.7;
  color: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
}

.float-star svg {
  filter: drop-shadow(0 0 8px rgba(255, 235, 59, 0.6));
  color: #FFD54F;
}

.float-cloud svg {
  filter: drop-shadow(0 0 6px rgba(255, 255, 255, 0.4));
}

.star-1 {
  top: -30px;
  right: 20%;
  animation: twinkle 2s ease-in-out infinite, floatY 4s ease-in-out infinite;
}

.star-2 {
  top: 50%;
  left: 10%;
  font-size: 1.5rem;
  animation: twinkle 2.5s ease-in-out infinite 0.5s, floatY 5s ease-in-out infinite 0.5s;
}

.cloud-1 {
  bottom: 20%;
  right: 5%;
  font-size: 2.5rem;
  animation: floatX 6s ease-in-out infinite;
}

.cloud-2 {
  top: 10%;
  left: 5%;
  animation: floatX 7s ease-in-out infinite 1s;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

@keyframes floatY {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

@keyframes floatX {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(15px); }
}

/* Features Section */
.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  width: 100%;
  max-width: 600px;
  margin-bottom: 2.5rem;
}

.feature-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 1.2rem 0.8rem;
  text-align: center;
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-card:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.feature-icon {
  margin-bottom: 0.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}

.feature-icon :deep(svg) {
  width: 32px;
  height: 32px;
}

.feature-title {
  font-size: 0.95rem;
  font-weight: 700;
  margin: 0 0 0.3rem 0;
  color: white;
}

.feature-desc {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  line-height: 1.4;
}

/* Stats Section */
.stats {
  display: flex;
  justify-content: center;
  gap: 2.5rem;
  margin-bottom: 2.5rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
}

.stat-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}

.stat-icon :deep(svg) {
  width: 24px;
  height: 24px;
}

.stat-value {
  font-family: 'Nunito', sans-serif;
  font-size: 1.8rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-pink), var(--color-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 600;
}

/* CTA Section */
.cta-section {
  text-align: center;
}

.cta-text {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 1rem;
  font-weight: 600;
}

.cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, var(--color-purple), var(--color-pink));
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(205, 180, 219, 0.4);
}

.cta-btn:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 15px 40px rgba(205, 180, 219, 0.5);
}

.cta-btn svg {
  transition: transform 0.3s ease;
}

.cta-btn:hover svg {
  transform: translateX(5px);
}

/* Scroll Indicator (Mobile Only) */
.scroll-indicator {
  display: none;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  margin-top: 2rem;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-10px); }
  60% { transform: translateY(-5px); }
}

/* Desktop Layout */
@media (min-width: 1024px) {
  .landing-section {
    justify-content: center;
    padding: 2rem 3rem;
  }

  .service-title {
    font-size: 8rem;
  }

  .tagline {
    font-size: 1.5rem;
  }

  .features {
    max-width: 700px;
    gap: 1.2rem;
  }

  .feature-card {
    padding: 1.5rem 1rem;
  }

  .feature-icon :deep(svg) {
    width: 36px;
    height: 36px;
  }

  .feature-title {
    font-size: 1rem;
  }

  .feature-desc {
    font-size: 0.8rem;
  }

  .cta-section {
    display: none; /* Desktop에서는 오른쪽 로그인 폼 사용 */
  }
}

/* Tablet */
@media (max-width: 1023px) and (min-width: 768px) {
  .features {
    grid-template-columns: repeat(3, 1fr);
    max-width: 500px;
  }
}

/* Mobile */
@media (max-width: 767px) {
  .landing-section {
    padding: 1.5rem;
    min-height: auto;
    padding-bottom: 2rem;
  }

  .hero {
    margin-bottom: 2rem;
  }

  .service-title {
    font-size: 4rem;
  }

  .tagline {
    font-size: 1.1rem;
  }

  .features {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem;
    margin-bottom: 2rem;
  }

  .feature-card {
    padding: 1rem 0.6rem;
  }

  .feature-icon :deep(svg) {
    width: 28px;
    height: 28px;
  }

  .feature-title {
    font-size: 0.85rem;
  }

  .feature-desc {
    font-size: 0.7rem;
  }

  .stats {
    gap: 1.5rem;
    margin-bottom: 2rem;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .scroll-indicator {
    display: flex;
  }
}

/* Small Mobile */
@media (max-width: 390px) {
  .service-title {
    font-size: 3.5rem;
  }

  .tagline {
    font-size: 1rem;
  }

  .features {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.6rem;
  }

  .feature-card {
    padding: 0.8rem 0.5rem;
  }

  .feature-icon {
    margin-bottom: 0.3rem;
  }
  
  .feature-icon :deep(svg) {
    width: 24px;
    height: 24px;
  }

  .feature-title {
    font-size: 0.8rem;
  }

  .feature-desc {
    font-size: 0.65rem;
  }
}
</style>

