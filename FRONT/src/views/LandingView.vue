<template>
  <div class="landing-page" ref="pageRef">
    <!-- 배경 오버레이 (기존 CloudBackground 위에 살짝 색 조정) -->
    <div class="bg-overlay"></div>

    <!-- 네비게이션 도트 -->
    <div class="nav-dots">
      <button 
        v-for="(_, index) in 5" 
        :key="index"
        :class="['dot', { active: currentSection === index }]"
        @click="scrollToSection(index)"
      ></button>
    </div>

    <!-- Section 1: Hero (로그인 상태) -->
    <section v-if="isLoggedIn" class="section section-hero" data-index="0">
      <div class="hero-content" :class="{ 'animate-in': visibleSections.has(0) }">
        <h1 class="hero-title">몽글몽글</h1>
        <p class="hero-tagline">당신의 꿈을 기록하고,<br/>AI가 해몽해드려요</p>
      </div>
      <div class="scroll-hint" :class="{ show: currentSection === 0 }">
        <div class="mouse">
          <div class="wheel"></div>
        </div>
        <span>Scroll</span>
      </div>
    </section>

    <!-- Section 1: Auth (비로그인 상태) -->
    <section v-else class="section section-auth" data-index="0">
      <div class="auth-wrapper" :class="{ 'animate-in': visibleSections.has(0) }">
        <div class="split-left">
          <h1 class="service-title">몽글몽글</h1>
        </div>
        <div class="split-right">
          <div class="common-card">
            <div class="page-header">
              <h2 class="page-title">{{ isLogin ? "환영합니다" : "회원가입" }}</h2>
              <p class="page-subtitle">{{ isLogin ? "꿈 기록을 시작하려면 로그인하세요" : "꿈의 여정을 시작해보세요" }}</p>
            </div>

            <form @submit.prevent="handleSubmit" class="common-form">
              <!-- 이름 (회원가입) -->
              <div v-if="!isLogin" class="input-group">
                <input v-model="formData.name" type="text" placeholder="이름" class="auth-input" @blur="validation.validateName(formData.name)" />
                <p v-if="validation.errors.name" class="error-text">{{ validation.errors.name }}</p>
              </div>

              <!-- 생년월일 (회원가입) -->
              <div v-if="!isLogin" class="input-group">
                <label class="input-label">생년월일</label>
                <div class="date-input-wrapper">
                  <input
                    v-model="formData.birthDate"
                    type="text"
                    placeholder="YYYY-MM-DD"
                    class="auth-input date-input"
                    maxlength="10"
                    @input="formatBirthDate"
                    @keydown="handleBirthDateKeydown"
                    @blur="validation.validateBirthDate(formData.birthDate)"
                  />
                  <input ref="datePickerRef" type="date" class="hidden-date-picker" @change="onDatePickerChange" :max="today" />
                  <button type="button" class="calendar-btn" @click="openDatePicker">📅</button>
                </div>
                <p v-if="validation.errors.birthDate" class="error-text">{{ validation.errors.birthDate }}</p>
              </div>

              <!-- 양력/음력 (회원가입) -->
              <div v-if="!isLogin" class="input-group">
                <div class="radio-group calendar-type-group">
                  <label class="radio-label">
                    <input type="radio" v-model="formData.calendarType" value="solar" class="custom-radio" />
                    <span class="radio-label-text">양력</span>
                  </label>
                  <label class="radio-label">
                    <input type="radio" v-model="formData.calendarType" value="lunar" class="custom-radio" />
                    <span class="radio-label-text">음력</span>
                  </label>
                </div>
                <p v-if="validation.errors.calendarType" class="error-text">{{ validation.errors.calendarType }}</p>
              </div>

              <!-- 성별 (회원가입) -->
              <div v-if="!isLogin" class="input-group">
                <label class="input-label">성별</label>
                <div class="radio-group">
                  <label class="radio-label">
                    <input type="radio" v-model="formData.gender" value="male" class="custom-radio" />
                    <span class="radio-label-text">남</span>
                  </label>
                  <label class="radio-label">
                    <input type="radio" v-model="formData.gender" value="female" class="custom-radio" />
                    <span class="radio-label-text">여</span>
                  </label>
                </div>
                <p v-if="validation.errors.gender" class="error-text">{{ validation.errors.gender }}</p>
              </div>

              <!-- 아이디 (로그인) -->
              <div v-if="isLogin" class="input-group">
                <input v-model="formData.loginId" type="text" placeholder="아이디" class="auth-input" autocomplete="username" @input="validation.filterAlphaNumeric" />
                <p v-if="validation.errors.loginId" class="error-text">{{ validation.errors.loginId }}</p>
              </div>

              <!-- 아이디 (회원가입) -->
              <div v-else class="input-group">
                <input
                  v-model="formData.loginId"
                  type="text"
                  placeholder="아이디 (영문, 숫자 4~20자)"
                  class="auth-input"
                  autocomplete="username"
                  @input="validation.filterAlphaNumeric"
                  @blur="validation.validateLoginId(formData.loginId)"
                />
                <p v-if="validation.errors.loginId" class="error-text">{{ validation.errors.loginId }}</p>
              </div>

              <!-- 비밀번호 (로그인) -->
              <div v-if="isLogin" class="input-group">
                <input v-model="formData.password" type="password" placeholder="비밀번호" class="auth-input" autocomplete="current-password" @input="validation.filterPassword" />
                <p v-if="validation.errors.password" class="error-text">{{ validation.errors.password }}</p>
                <p v-if="serverError" class="error-text">{{ serverError }}</p>
              </div>

              <!-- 비밀번호 (회원가입) -->
              <div v-else class="input-group">
                <input
                  v-model="formData.password"
                  type="password"
                  placeholder="비밀번호 (영문, 숫자, 특수문자 8~20자)"
                  class="auth-input"
                  autocomplete="new-password"
                  @input="validation.filterPassword"
                  @blur="validation.validatePassword(formData.password)"
                />
                <p v-if="validation.errors.password" class="error-text">{{ validation.errors.password }}</p>
              </div>

              <!-- 비밀번호 확인 (회원가입만) -->
              <div v-if="!isLogin" class="input-group">
                <input
                  v-model="formData.confirmPassword"
                  type="password"
                  placeholder="비밀번호 확인"
                  class="auth-input"
                  autocomplete="new-password"
                  @input="validation.filterPassword"
                  @blur="validation.validateConfirmPassword(formData.password, formData.confirmPassword)"
                />
                <p v-if="validation.errors.confirmPassword" class="error-text">{{ validation.errors.confirmPassword }}</p>
              </div>

              <button type="submit" class="submit-btn" :disabled="isSubmitting">
                {{ isSubmitting ? "처리 중..." : isLogin ? "로그인" : "회원가입" }}
              </button>
            </form>

            <div class="auth-footer">
              <p class="auth-footer-text">
                {{ isLogin ? "계정이 없으신가요?" : "이미 계정이 있으신가요?" }}
                <button @click="toggleAuthMode" class="toggle-btn">
                  {{ isLogin ? "회원가입" : "로그인" }}
                </button>
              </p>
            </div>
          </div>
        </div>
      </div>
      <div class="scroll-hint" :class="{ show: currentSection === 0 }">
        <div class="mouse">
          <div class="wheel"></div>
        </div>
        <span>Scroll</span>
      </div>
    </section>

    <!-- Section 2: Name Meaning -->
    <section class="section section-meaning" data-index="1">
      <div class="meaning-content" :class="{ 'animate-in': visibleSections.has(1) }">
        <div class="formula-wrap">
          <div class="formula-item" style="--delay: 0s">
            <span class="formula-char">夢</span>
            <span class="formula-label">꿈 몽</span>
          </div>
          <div class="formula-op" style="--delay: 0.2s">+</div>
          <div class="formula-item" style="--delay: 0.4s">
            <span class="formula-char">글</span>
            <span class="formula-label">기록하다</span>
          </div>
          <div class="formula-op" style="--delay: 0.6s">=</div>
          <div class="formula-item result" style="--delay: 0.8s">
            <span class="formula-char">몽글</span>
          </div>
        </div>
        <p class="meaning-text" style="--delay: 1s">
          <strong>몽글몽글</strong>은 <em>'꿈(夢)'</em>과 <em>'글'</em>이 만나<br/>
          당신의 꿈을 기록하고 해석하는 공간이에요
        </p>
        <div class="mongler-badge" style="--delay: 1.2s">
          <span class="badge-icon" v-html="monglerIcon"></span>
          꿈을 기록하는 당신은 <strong>몽글러</strong>
        </div>
      </div>
    </section>

    <!-- Section 3: Features -->
    <section class="section section-features" data-index="2">
      <div class="features-content" :class="{ 'animate-in': visibleSections.has(2) }">
        <h2 class="section-title">주요 기능</h2>
        <div class="features-grid">
          <div 
            class="feature-card" 
            v-for="(feature, index) in features" 
            :key="index"
            :style="{ '--delay': `${index * 0.1}s` }"
          >
            <div class="feature-icon" v-html="feature.icon"></div>
            <div class="feature-info">
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Section 4: How to Use -->
    <section class="section section-steps" data-index="3">
      <div class="steps-content" :class="{ 'animate-in': visibleSections.has(3) }">
        <h2 class="section-title">이용 방법</h2>
        <div class="steps-timeline">
          <div 
            class="step-item" 
            v-for="(step, index) in steps" 
            :key="index"
            :style="{ '--delay': `${index * 0.15}s` }"
          >
            <div class="step-number">
              <span>{{ index + 1 }}</span>
            </div>
            <div class="step-line"></div>
            <div class="step-card">
              <h3>{{ step.title }}</h3>
              <p>{{ step.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Section 5: CTA -->
    <section class="section section-cta" data-index="4">
      <div class="cta-content" :class="{ 'animate-in': visibleSections.has(4) }">
        <h2 class="cta-title">AI 티켓 안내</h2>
        <div class="ticket-cards">
          <div class="ticket-card" style="--delay: 0s">
            <div class="ticket-icon" v-html="ticketIcons.crystal"></div>
            <span class="ticket-name">꿈 해몽</span>
            <span class="ticket-cost">1개</span>
          </div>
          <div class="ticket-card" style="--delay: 0.1s">
            <div class="ticket-icon" v-html="ticketIcons.palette"></div>
            <span class="ticket-name">이미지 생성</span>
            <span class="ticket-cost">2개</span>
          </div>
          <div class="ticket-card" style="--delay: 0.2s">
            <div class="ticket-icon" v-html="ticketIcons.clock"></div>
            <span class="ticket-name">매일 충전</span>
            <span class="ticket-cost">자정 리셋</span>
          </div>
        </div>
        <div class="cta-action" style="--delay: 0.4s">
          <p>지금 바로 꿈을 기록해보세요!</p>
          <button v-if="isLoggedIn" class="cta-btn" @click="goToCalendar">
            <span>캘린더로 이동</span>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </button>
          <button v-else class="cta-btn" @click="scrollToSection(0)">
            <span>시작하기</span>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useDreamEntriesStore } from '../stores/dreamEntriesStore';
import { useAuthStore } from '../stores/authStore';
import { useFormValidation } from '../composables/useFormValidation';
import { useConfirm } from '../composables/useConfirm';

const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const authStore = useAuthStore();
const validation = useFormValidation();
const { confirm } = useConfirm();

const pageRef = ref(null);
const currentSection = ref(0);
const visibleSections = ref(new Set([0]));

// 로그인 상태 확인
const isLoggedIn = computed(() => !!localStorage.getItem('accessToken'));

// Auth 관련
const isLogin = ref(true);
const isSubmitting = ref(false);
const serverError = ref('');
const datePickerRef = ref(null);

const formData = reactive({
  name: '',
  loginId: '',
  password: '',
  confirmPassword: '',
  birthDate: '',
  gender: '',
  calendarType: '',
});

// 오늘 날짜 (YYYY-MM-DD 형식)
const today = computed(() => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
});

// Icons
const monglerIcon = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><polygon points="22 8 19.6 9.5 20.1 12.2 17.8 10.4 15.4 12.1 16 9.4 13.7 7.9 16.5 7.7 17.8 5 19.1 7.7 22 8" fill="currentColor"></polygon></svg>';

const icons = {
  book: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#E9D5FF" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>',
  crystal: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#E9D5FF" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><path d="M2 12h4M18 12h4M12 2v4M12 18v4"></path></svg>',
  palette: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#E9D5FF" stroke-width="2"><circle cx="13.5" cy="6.5" r="1.5" fill="#F5D0FE"></circle><circle cx="17.5" cy="10.5" r="1.5" fill="#F5D0FE"></circle><circle cx="8.5" cy="7.5" r="1.5" fill="#F5D0FE"></circle><circle cx="6.5" cy="12.5" r="1.5" fill="#F5D0FE"></circle><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.555C21.965 6.012 17.461 2 12 2z"></path></svg>',
  chart: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#E9D5FF" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>',
  rainbow: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#E9D5FF" stroke-width="2"><path d="M22 17a10 10 0 0 0-20 0"></path><path d="M6 17a6 6 0 0 1 12 0"></path><path d="M10 17a2 2 0 0 1 4 0"></path></svg>',
  trophy: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#E9D5FF" stroke-width="2"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"></path><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"></path><path d="M4 22h16"></path><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"></path><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"></path><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"></path></svg>'
};

const ticketIcons = {
  crystal: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" stroke="#C4B5FD" stroke-width="1.5"/><circle cx="12" cy="12" r="6" fill="url(#crystalGrad)" opacity="0.6"/><path d="M12 2v4M12 18v4M2 12h4M18 12h4" stroke="#C4B5FD" stroke-width="1.5"/><defs><linearGradient id="crystalGrad" x1="6" y1="6" x2="18" y2="18"><stop stop-color="#A78BFA"/><stop offset="1" stop-color="#F0ABFC"/></linearGradient></defs></svg>',
  palette: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><circle cx="13.5" cy="6.5" r="2" fill="#F9A8D4"/><circle cx="17.5" cy="10.5" r="2" fill="#A78BFA"/><circle cx="8.5" cy="7.5" r="2" fill="#FCD34D"/><circle cx="6.5" cy="12.5" r="2" fill="#6EE7B7"/><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.555C21.965 6.012 17.461 2 12 2z" stroke="#C4B5FD" stroke-width="1.5"/></svg>',
  clock: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" stroke="#C4B5FD" stroke-width="1.5"/><circle cx="12" cy="12" r="3" fill="url(#clockGrad)" opacity="0.5"/><path d="M12 6v6l4 2" stroke="#E9D5FF" stroke-width="2" stroke-linecap="round"/><defs><linearGradient id="clockGrad" x1="9" y1="9" x2="15" y2="15"><stop stop-color="#A78BFA"/><stop offset="1" stop-color="#F0ABFC"/></linearGradient></defs></svg>'
};

const features = [
  { icon: icons.book, title: '꿈 일기', description: '매일의 꿈을 캘린더에 기록' },
  { icon: icons.crystal, title: 'AI 해몽', description: 'AI가 꿈의 의미를 해석' },
  { icon: icons.palette, title: '꿈 시각화', description: '꿈을 이미지로 시각화' },
  { icon: icons.chart, title: '월별 분석', description: '꿈 패턴 분석 리포트' },
  { icon: icons.rainbow, title: '오늘의 운세', description: '매일 운세와 행운의 색' },
  { icon: icons.trophy, title: '랭킹', description: '꿈 기록 순위 확인' }
];

const steps = [
  { title: '꿈을 기록해요', description: '캘린더에서 날짜를 선택하고 꿈을 적어보세요' },
  { title: 'AI가 해몽해요', description: 'AI가 꿈을 분석하여 의미를 알려드려요' },
  { title: '꿈을 시각화해요', description: 'AI가 꿈 속 장면을 이미지로 그려드려요' },
  { title: '월별로 확인해요', description: '한 달간의 꿈 패턴을 리포트로 받아보세요' }
];

let observer = null;

function scrollToSection(index) {
  const sections = pageRef.value?.querySelectorAll('.section');
  if (sections && sections[index]) {
    sections[index].scrollIntoView({ behavior: 'smooth' });
  }
}

function goToCalendar() {
  router.push({ name: 'calendar' });
}

// Auth 관련 함수들
function formatBirthDate(event) {
  const input = event.target;
  let digits = input.value.replace(/\D/g, '');

  if (digits.length > 8) {
    digits = digits.slice(0, 8);
  }

  let formatted = digits;
  if (digits.length > 6) {
    formatted = digits.slice(0, 4) + '-' + digits.slice(4, 6) + '-' + digits.slice(6);
  } else if (digits.length > 4) {
    formatted = digits.slice(0, 4) + '-' + digits.slice(4);
  }

  formData.birthDate = formatted;

  setTimeout(() => {
    input.setSelectionRange(formatted.length, formatted.length);
  }, 0);
}

function handleBirthDateKeydown(event) {
  if (event.key === 'Backspace') {
    const input = event.target;
    const cursorPos = input.selectionStart;
    const selectionEnd = input.selectionEnd;
    const value = formData.birthDate;

    if (cursorPos === selectionEnd && cursorPos > 0 && value[cursorPos - 1] === '-') {
      event.preventDefault();
      let digits = value.replace(/\D/g, '');
      digits = digits.slice(0, -1);

      let formatted = digits;
      if (digits.length > 6) {
        formatted = digits.slice(0, 4) + '-' + digits.slice(4, 6) + '-' + digits.slice(6);
      } else if (digits.length > 4) {
        formatted = digits.slice(0, 4) + '-' + digits.slice(4);
      }

      formData.birthDate = formatted;
      setTimeout(() => {
        input.setSelectionRange(formatted.length, formatted.length);
      }, 0);
    }
  }
}

function openDatePicker() {
  if (datePickerRef.value) {
    datePickerRef.value.showPicker();
  }
}

function onDatePickerChange(event) {
  formData.birthDate = event.target.value;
  validation.validateBirthDate(formData.birthDate);
}

function resetForm() {
  formData.name = '';
  formData.loginId = '';
  formData.password = '';
  formData.confirmPassword = '';
  formData.birthDate = '';
  formData.gender = '';
  formData.calendarType = '';
  validation.clearErrors();
  serverError.value = '';
}

function toggleAuthMode() {
  isLogin.value = !isLogin.value;
  resetForm();
  authStore.clearError();
}

async function handleSubmit() {
  if (isSubmitting.value) return;

  serverError.value = '';

  if (isLogin.value) {
    if (!validation.validateLoginForm(formData)) {
      return;
    }
  } else {
    if (!validation.validateSignupForm(formData)) {
      return;
    }
  }

  isSubmitting.value = true;

  try {
    if (!isLogin.value) {
      const signupData = {
        loginId: formData.loginId,
        password: formData.password,
        name: formData.name,
        birthDate: formData.birthDate,
        gender: validation.genderToBackend(formData.gender),
        calendarType: validation.calendarTypeToBackend(formData.calendarType),
      };

      await authStore.signup(signupData);
      await confirm({
        title: '회원가입 완료',
        message: '회원가입이 완료되었습니다.\n환영합니다!',
        type: 'success',
        confirmText: '확인',
        showCancel: false
      });
    } else {
      await authStore.login({
        loginId: formData.loginId,
        password: formData.password,
      });
    }
    router.push({ name: 'calendar' });
  } catch (err) {
    serverError.value = authStore.error || '처리 중 오류가 발생했습니다.';
  } finally {
    isSubmitting.value = false;
  }
}

// 입력 시 서버 에러 초기화
watch([() => formData.loginId, () => formData.password], () => {
  if (serverError.value) {
    serverError.value = '';
    authStore.clearError();
  }
});

onMounted(() => {
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        const index = parseInt(entry.target.dataset.index);
        if (entry.isIntersecting) {
          visibleSections.value.add(index);
          if (entry.intersectionRatio > 0.5) {
            currentSection.value = index;
          }
        }
      });
    },
    { threshold: [0.3, 0.5, 0.7] }
  );

  pageRef.value?.querySelectorAll('.section').forEach((section) => {
    observer.observe(section);
  });
});

onUnmounted(() => {
  observer?.disconnect();
});
</script>

<style scoped>
.landing-page {
  position: fixed;
  inset: 0;
  overflow-y: auto;
  overflow-x: hidden;
  scroll-snap-type: y mandatory;
  scroll-behavior: smooth;
  z-index: 50;
}

/* 배경 오버레이 */
.bg-overlay {
  position: fixed;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(80, 70, 120, 0.3) 0%,
    rgba(120, 100, 160, 0.2) 50%,
    rgba(180, 150, 200, 0.15) 100%
  );
  pointer-events: none;
  z-index: -1;
}

/* 네비게이션 도트 */
.nav-dots {
  position: fixed;
  right: 2rem;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 14px;
  z-index: 200;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.4);
  background: transparent;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  padding: 0;
}

.dot:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.2);
}

.dot.active {
  background: white;
  border-color: white;
  transform: scale(1.3);
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
}

/* 섹션 공통 */
.section {
  min-height: 100vh;
  width: 100%;
  scroll-snap-align: start;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 2rem;
}

/* ========== Section 1: Auth ========== */
.section-auth {
  position: relative;
}

.section-hero {
  text-align: center;
  position: relative;
}

.hero-content {
  opacity: 0;
  transform: translateY(30px);
  transition: all 1s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  z-index: 1;
}

.hero-content.animate-in {
  opacity: 1;
  transform: translateY(0);
}

/* Auth 레이아웃 */
.auth-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
  width: 100%;
  max-width: 1200px;
  position: relative;
  z-index: 1;
  opacity: 0;
  transform: translateY(30px);
  transition: all 1s cubic-bezier(0.16, 1, 0.3, 1);
}

.auth-wrapper.animate-in {
  opacity: 1;
  transform: translateY(0);
}

/* 예전 AuthView.vue 스타일 적용 */
.split-left {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.split-right {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.service-title {
  font-family: 'Dongle', sans-serif;
  font-size: 5rem;
  font-weight: 700;
  background: linear-gradient(to right, var(--color-purple), var(--color-pink), var(--color-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: transparent;
  margin: 0 0 3rem 0;
  line-height: 1.2;
  letter-spacing: 2px;
  text-align: center;
  filter: drop-shadow(0 2px 4px rgba(255, 255, 255, 0.3));
}

/* 데스크톱 레이아웃 */
@media (min-width: 1024px) {
  .auth-wrapper {
    flex-direction: row;
    align-items: center;
    justify-content: center;
    max-width: 100%;
    gap: 0;
  }

  .split-left {
    width: 50%;
    height: 100%;
  }

  .split-right {
    width: 50%;
    height: 100%;
    justify-content: flex-start;
    padding-left: 30px;
  }

  .service-title {
    margin: 0;
    font-size: 8rem;
  }

  .common-card {
    width: 100%;
    max-width: 480px;
  }
}

.auth-footer {
  margin-top: 2rem;
  font-size: 0.9rem;
  color: #999;
}

.auth-footer-text {
  font-weight: 600;
}

.toggle-btn {
  background: none;
  border: none;
  color: #8ec5fc;
  font-weight: 700;
  cursor: pointer;
  padding: 0 5px;
  font-size: 0.9rem;
}

.toggle-btn:hover {
  text-decoration: underline;
}

/* 달력 유형 그룹 */
.calendar-type-group {
  margin-bottom: 0;
}

/* 생년월일 입력 wrapper */
.date-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.date-input-wrapper .date-input {
  flex: 1;
  padding-right: 40px;
}

.hidden-date-picker {
  position: absolute;
  right: 220px;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}

.calendar-btn {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 5px;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.calendar-btn:hover {
  opacity: 1;
}

/* Hero 섹션 (로그인 상태) */
.hero-title {
  font-family: 'Dongle', sans-serif;
  font-size: 6rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-purple), var(--color-pink), var(--color-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  line-height: 1.1;
  letter-spacing: 5px;
  filter: drop-shadow(0 4px 8px rgba(255, 255, 255, 0.2));
  animation: titleFloat 3s ease-in-out infinite;
}

@keyframes titleFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.hero-tagline {
  font-size: 1.2rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 0.5rem;
  line-height: 1.7;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* 스크롤 힌트 */
.scroll-hint {
  position: absolute;
  bottom: 3rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.8rem;
  opacity: 0;
  transition: opacity 0.5s;
  color: rgba(255, 255, 255, 0.6);
}

.scroll-hint.show {
  opacity: 1;
}

.mouse {
  width: 26px;
  height: 40px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  position: relative;
}

.wheel {
  width: 4px;
  height: 8px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 2px;
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  animation: scrollWheel 1.5s infinite;
}

@keyframes scrollWheel {
  0% { opacity: 1; transform: translateX(-50%) translateY(0); }
  100% { opacity: 0; transform: translateX(-50%) translateY(12px); }
}

.scroll-hint span {
  font-size: 0.8rem;
  letter-spacing: 2px;
  text-transform: uppercase;
}

/* 데스크톱 레이아웃 */
@media (min-width: 1024px) {
  .auth-wrapper {
    flex-direction: row;
    justify-content: center;
    gap: 4rem;
  }

  .auth-left {
    flex: 1;
    text-align: center;
  }

  .hero-title {
    font-size: 8rem;
  }

  .hero-tagline {
    font-size: 1.4rem;
  }
}

/* ========== Section 2: Meaning ========== */
.section-meaning {
  background: rgba(0, 0, 0, 0.2);
}

.meaning-content {
  text-align: center;
  max-width: 700px;
}

.formula-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  flex-wrap: wrap;
  margin-bottom: 3rem;
}

.formula-item, .formula-op {
  opacity: 0;
  transform: translateY(30px) scale(0.8);
}

.animate-in .formula-item,
.animate-in .formula-op {
  animation: popIn 0.6s var(--delay) forwards cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes popIn {
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.formula-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.formula-char {
  font-size: 4.5rem;
  font-weight: 700;
  color: white;
  text-shadow: 0 0 30px rgba(167, 139, 250, 0.6);
  line-height: 1;
}

.formula-item.result .formula-char {
  font-family: 'Dongle', sans-serif;
  font-size: 6rem;
  background: linear-gradient(135deg, var(--color-purple), var(--color-pink), var(--color-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 4px 8px rgba(255, 255, 255, 0.2));
  animation: titleFloat 3s ease-in-out infinite;
}

.formula-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
}

.formula-op {
  font-size: 3rem;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
}

.meaning-text {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.85);
  line-height: 2;
  margin-bottom: 2rem;
  opacity: 0;
  transform: translateY(20px);
}

.animate-in .meaning-text {
  animation: fadeUp 0.6s var(--delay) forwards;
}

@keyframes fadeUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.meaning-text strong {
  color: #a78bfa;
  font-weight: 700;
}

.meaning-text em {
  font-style: normal;
  color: #f0abfc;
}

.mongler-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.7rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 1rem 2rem;
  border-radius: 50px;
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
  opacity: 0;
  transform: translateY(20px);
}

.animate-in .mongler-badge {
  animation: fadeUp 0.6s var(--delay) forwards;
}

.mongler-badge strong {
  color: #f0abfc;
  font-weight: 800;
}

.badge-icon {
  display: flex;
  color: #f0abfc;
}

/* ========== Section 3: Features ========== */
.section-features {
  background: rgba(167, 139, 250, 0.15);
}

.features-content {
  max-width: 900px;
  width: 100%;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: white;
  text-align: center;
  margin-bottom: 3rem;
  text-shadow: 0 0 30px rgba(167, 139, 250, 0.5);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.2rem;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  padding: 1.5rem;
  opacity: 0;
  transform: translateY(30px) scale(0.9);
  transition: all 0.3s ease;
}

.animate-in .feature-card {
  animation: cardIn 0.5s var(--delay) forwards;
}

@keyframes cardIn {
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.feature-card:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-5px) scale(1.02);
  border-color: rgba(167, 139, 250, 0.5);
}

.feature-icon {
  flex-shrink: 0;
  filter: drop-shadow(0 0 8px rgba(233, 213, 255, 0.5));
}

.feature-icon :deep(svg) {
  width: 32px;
  height: 32px;
}

.feature-info h3 {
  font-size: 1rem;
  font-weight: 700;
  color: white;
  margin: 0 0 0.3rem 0;
}

.feature-info p {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.65);
  margin: 0;
}

/* ========== Section 4: Steps ========== */
.section-steps {
  background: rgba(240, 171, 252, 0.1);
}

.steps-content {
  max-width: 700px;
  width: 100%;
}

.steps-timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
  opacity: 0;
  transform: translateX(-30px);
}

.animate-in .step-item {
  animation: slideRight 0.6s var(--delay) forwards;
}

@keyframes slideRight {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.step-number {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #a78bfa, #f0abfc);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 0 30px rgba(167, 139, 250, 0.4);
}

.step-number span {
  font-size: 1.3rem;
  font-weight: 800;
  color: white;
}

.step-line {
  position: absolute;
  left: 25px;
  top: 50px;
  width: 2px;
  height: calc(100% - 50px);
  background: linear-gradient(180deg, rgba(167, 139, 250, 0.5), transparent);
}

.step-item:last-child .step-line {
  display: none;
}

.step-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.step-card:hover {
  background: rgba(255, 255, 255, 0.12);
  transform: translateX(10px);
}

.step-card h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: white;
  margin: 0 0 0.4rem 0;
}

.step-card p {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

/* ========== Section 5: CTA ========== */
.section-cta {
  background: rgba(0, 0, 0, 0.25);
}

.cta-content {
  text-align: center;
  max-width: 800px;
}

.cta-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: white;
  margin-bottom: 2.5rem;
  text-shadow: 0 0 30px rgba(167, 139, 250, 0.5);
}

.ticket-cards {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  flex-wrap: wrap;
  margin-bottom: 3rem;
}

.ticket-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 2rem;
  min-width: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.8rem;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.3s ease;
}

.animate-in .ticket-card {
  animation: fadeUp 0.5s var(--delay) forwards;
}

.ticket-card:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: translateY(-8px);
  border-color: rgba(167, 139, 250, 0.5);
}

.ticket-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  filter: drop-shadow(0 0 10px rgba(167, 139, 250, 0.4));
}

.ticket-icon :deep(svg) {
  width: 40px;
  height: 40px;
}

.ticket-name {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.7);
}

.ticket-cost {
  font-size: 1.4rem;
  font-weight: 800;
  color: white;
}

.cta-action {
  opacity: 0;
  transform: translateY(20px);
}

.animate-in .cta-action {
  animation: fadeUp 0.6s var(--delay) forwards;
}

.cta-action p {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 1.5rem;
}

.cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.8rem;
  background: linear-gradient(135deg, #a78bfa, #f0abfc);
  color: white;
  border: none;
  padding: 1.2rem 3rem;
  border-radius: 50px;
  font-size: 1.2rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 40px rgba(167, 139, 250, 0.4);
}

.cta-btn:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 20px 50px rgba(167, 139, 250, 0.5);
}

.cta-btn svg {
  transition: transform 0.3s ease;
}

.cta-btn:hover svg {
  transform: translateX(5px);
}

/* ========== Responsive ========== */
@media (max-width: 768px) {
  .hero-title {
    font-size: 4.5rem;
  }

  .hero-tagline {
    font-size: 1.1rem;
  }

  .formula-char {
    font-size: 2.8rem;
  }

  .formula-item.result .formula-char {
    font-size: 3.5rem;
  }

  .formula-label {
    font-size: 0.85rem;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }

  .nav-dots {
    right: 1rem;
  }

  .section-title {
    font-size: 1.8rem;
  }

  .meaning-text {
    font-size: 1rem;
  }

  .ticket-cards {
    gap: 1rem;
  }

  .ticket-card {
    padding: 1.5rem;
    min-width: 140px;
  }

  .ticket-name {
    font-size: 0.85rem;
  }

  .ticket-cost {
    font-size: 0.75rem;
  }

  .step-card h3 {
    font-size: 1rem;
  }

  .step-card p {
    font-size: 0.85rem;
  }

  .feature-info h3 {
    font-size: 0.95rem;
  }

  .feature-info p {
    font-size: 0.8rem;
  }
}

@media (max-width: 500px) {
  .hero-title {
    font-size: 3.2rem;
    letter-spacing: 2px;
  }

  .hero-tagline {
    font-size: 0.95rem;
  }

  .formula-wrap {
    gap: 0.6rem;
  }

  .formula-char {
    font-size: 2rem;
  }

  .formula-item.result .formula-char {
    font-size: 2.5rem;
  }

  .formula-op {
    font-size: 1.6rem;
  }

  .formula-label {
    font-size: 0.75rem;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .meaning-text {
    font-size: 0.9rem;
  }

  .mongler-badge {
    font-size: 0.9rem;
    padding: 0.8rem 1.2rem;
  }

  .features-grid {
    grid-template-columns: 1fr 1fr;
  }

  .feature-card {
    flex-direction: column;
    text-align: center;
    padding: 1rem 0.8rem;
  }

  .feature-info h3 {
    font-size: 0.85rem;
  }

  .feature-info p {
    font-size: 0.7rem;
  }

  .step-number {
    width: 36px;
    height: 36px;
  }

  .step-number span {
    font-size: 1rem;
  }

  .step-card h3 {
    font-size: 0.9rem;
  }

  .step-card p {
    font-size: 0.75rem;
  }

  .ticket-card {
    min-width: 90px;
    padding: 1rem;
  }

  .ticket-icon :deep(svg) {
    width: 28px;
    height: 28px;
  }

  .ticket-name {
    font-size: 0.75rem;
  }

  .ticket-cost {
    font-size: 0.65rem;
  }

  .cta-action p {
    font-size: 0.9rem;
  }

  .cta-btn {
    padding: 0.9rem 1.8rem;
    font-size: 0.9rem;
  }
}
</style>
