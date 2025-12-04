<template>
  <div class="auth-wrapper">
    <div class="split-left">
      <h1 class="service-title">몽글몽글</h1>
    </div>
    <div class="split-right">
      <div class="common-card">
        <div class="page-header">
      <h2 class="page-title">{{ isLogin ? '환영합니다' : '회원가입' }}</h2>
      <p class="page-subtitle">{{ isLogin ? '꿈 기록을 시작하려면 로그인하세요' : '꿈의 여정을 시작해보세요' }}</p>
    </div>

    <form @submit.prevent="handleSubmit" class="common-form">
      <div v-if="!isLogin" class="input-group">
        <input 
          v-model="name" 
          type="text" 
          placeholder="이름" 
          class="auth-input"
          required
        />
      </div>

      <div v-if="!isLogin" class="input-group">
        <label class="input-label">생년월일</label>
        <input 
          v-model="birthDate" 
          type="text" 
          placeholder="YYYY-MM-DD"
          class="auth-input date-input"
          required
          maxlength="10"
          @input="formatBirthDate"
          @keypress="onlyNumbers"
        />
      </div>

      <div v-if="!isLogin" class="input-group">
        <label class="input-label">성별</label>
        <div class="radio-group">
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="gender" 
              value="male" 
              class="custom-radio"
              required 
            />
            <span class="radio-label-text">남</span>
          </label>
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="gender" 
              value="female" 
              class="custom-radio"
              required 
            />
            <span class="radio-label-text">여</span>
          </label>
        </div>
      </div>

      <div v-if="!isLogin" class="input-group">
        <label class="input-label">생년월일 달력 유형</label>
        <div class="radio-group calendar-type-group">
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="calendarBase" 
              value="solar" 
              class="custom-radio"
              required 
            />
            <span class="radio-label-text">양력</span>
          </label>
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="calendarBase" 
              value="lunar" 
              class="custom-radio"
              required 
            />
            <span class="radio-label-text">음력</span>
          </label>
        </div>
        <!-- 음력 선택 시 윤달 체크박스 표시 -->
        <div v-if="calendarBase === 'lunar'" class="leap-month-option">
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="isLeapMonth" 
              class="custom-checkbox"
            />
            <span class="checkbox-label-text">윤달에 태어났어요</span>
          </label>
          <p class="helper-text">* 윤달 여부를 모르시면 체크하지 않으셔도 됩니다</p>
        </div>
      </div>

      <div v-if="isLogin" class="input-group">
        <input 
          v-model="loginId" 
          type="text" 
          placeholder="아이디" 
          class="auth-input"
          required
          @input="allowOnlyAlphaNumeric"
        />
      </div>

      <div v-else class="input-group">
        <input 
          v-model="signupId" 
          type="text" 
          placeholder="아이디" 
          class="auth-input"
          required
          @input="allowOnlyAlphaNumeric"
        />
        <p v-if="!isLogin && loginError" class="error-text">{{ loginError }}</p>
      </div>
      
      <div v-if="isLogin" class="input-group">
        <input 
          v-model="loginPassword" 
          type="password" 
          placeholder="비밀번호" 
          class="auth-input"
          required
          @input="allowOnlyAlphaNumeric"
        />
        <p v-if="isLogin && loginError" class="error-text">{{ loginError }}</p>
      </div>

      <div v-else class="input-group">
        <input 
          v-model="signupPassword" 
          type="password" 
          placeholder="비밀번호" 
          class="auth-input"
          required
          @input="allowOnlyAlphaNumeric"
        />
      </div>

      <div v-if="!isLogin" class="input-group">
        <input 
          v-model="confirmPassword" 
          type="password" 
          placeholder="비밀번호 확인" 
          class="auth-input"
          required
          @input="allowOnlyAlphaNumeric"
        />
        <p v-if="passwordError" class="error-text">{{ passwordError }}</p>
      </div>

      <button type="submit" class="submit-btn">
        {{ isLogin ? '로그인' : '회원가입' }}
      </button>
    </form>

    <div class="auth-footer">
      <p class="auth-footer-text">
        {{ isLogin ? "계정이 없으신가요?" : "이미 계정이 있으신가요?" }}
        <button @click="toggleAuthMode" class="toggle-btn">
          {{ isLogin ? '회원가입' : '로그인' }}
        </button>
      </p>
    </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useDreamEntriesStore } from '../stores/dreamEntriesStore';
import { useAuthStore } from '../stores/authStore';
import { usePasswordValidation } from '../composables/usePasswordValidation';
import { useInputValidation } from '../composables/useInputValidation';

const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const authStore = useAuthStore();
const { resetAll } = dreamEntriesStore;
const { allowOnlyAlphaNumeric } = useInputValidation();

const isLogin = ref(true);
const loginId = ref('');
const loginPassword = ref('');
const signupId = ref('');
const signupPassword = ref('');
const confirmPassword = ref('');
const name = ref('');
const birthDate = ref('');
const gender = ref('');
const calendarBase = ref('solar'); // 양력/음력 선택
const isLeapMonth = ref(false); // 윤달 여부
const loginError = ref('');
const isSubmitting = ref(false);

// 실제 API로 전송할 calendarType 계산
function getCalendarType() {
  if (calendarBase.value === 'solar') {
    return 'solar';
  }
  return isLeapMonth.value ? 'lunarLeap' : 'lunarGeneral';
}

const { passwordError, validateMatch } = usePasswordValidation(signupPassword, confirmPassword);

function toggleAuthMode() {
  isLogin.value = !isLogin.value;
  // 모드 전환 시 상태 초기화
  loginError.value = '';
  loginId.value = '';
  loginPassword.value = '';
  signupId.value = '';
  signupPassword.value = '';
  confirmPassword.value = '';
  name.value = '';
  birthDate.value = '';
  gender.value = '';
  calendarBase.value = 'solar';
  isLeapMonth.value = false;
  authStore.clearError();
}

// 생년월일 입력 시 자동 포맷팅 (YYYY-MM-DD)
function formatBirthDate(event) {
  let value = event.target.value.replace(/[^0-9]/g, ''); // 숫자만 추출
  
  if (value.length > 8) {
    value = value.slice(0, 8);
  }
  
  // 자동으로 하이픈 추가
  if (value.length >= 6) {
    value = value.slice(0, 4) + '-' + value.slice(4, 6) + '-' + value.slice(6);
  } else if (value.length >= 4) {
    value = value.slice(0, 4) + '-' + value.slice(4);
  }
  
  birthDate.value = value;
}

// 숫자와 하이픈만 입력 허용
function onlyNumbers(event) {
  const char = String.fromCharCode(event.keyCode || event.which);
  if (!/[0-9]/.test(char)) {
    event.preventDefault();
  }
}

async function handleSubmit() {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    if (!isLogin.value) {
      // 회원가입
      if (!validateMatch()) {
        loginError.value = '비밀번호가 일치하지 않습니다!';
        return;
      }

      // 필수 항목 확인
      if (!signupId.value || !signupPassword.value || !name.value || !birthDate.value || !gender.value || !calendarBase.value) {
        let missing = [];
        if (!signupId.value) missing.push('아이디');
        if (!signupPassword.value) missing.push('비밀번호');
        if (!name.value) missing.push('이름');
        if (!birthDate.value) missing.push('생년월일');
        if (!gender.value) missing.push('성별');
        if (!calendarBase.value) missing.push('달력 유형');
        
        loginError.value = `다음 항목을 입력해주세요: ${missing.join(', ')}`;
        return;
      }

      const signupData = {
        loginId: signupId.value,
        password: signupPassword.value,
        name: name.value,
        birthDate: birthDate.value,
        gender: gender.value,
        calendarType: getCalendarType(),
      };
      console.log('회원가입 데이터:', signupData);
      await authStore.signup(signupData);

      alert('회원가입이 완료되었습니다.');
    } else {
      // 로그인
      await authStore.login({
        loginId: loginId.value,
        password: loginPassword.value,
      });
    }

    loginError.value = '';
    resetAll();
    router.push({ name: 'calendar' });
  } catch (err) {
    loginError.value = authStore.error || '처리 중 오류가 발생했습니다.';
  } finally {
    isSubmitting.value = false;
  }
}

watch([loginId, loginPassword, signupId], () => {
  if (loginError.value) {
    loginError.value = '';
    authStore.clearError();
  }
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Dongle:wght@300;400;700&display=swap');

.auth-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  min-height: 100%;
}

.split-right {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.split-left {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.service-title {
  font-family: 'Dongle', sans-serif;
  font-size: 5rem;
  font-weight: 700;
  /* Text Gradient: Pastel Purple -> Pink -> Blue */
  background: linear-gradient(to right, #CDB4DB, #FFC8DD, #A2D2FF);
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
  color: #8EC5FC;
  font-weight: 700;
  cursor: pointer;
  padding: 0 5px;
  font-size: 0.9rem;
}

.toggle-btn:hover {
  text-decoration: underline;
}

/* 윤달 체크박스 옵션 */
.leap-month-option {
  margin-top: 0.75rem;
  padding: 0.75rem 1rem;
  background: rgba(162, 210, 255, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(162, 210, 255, 0.3);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.custom-checkbox {
  width: 18px;
  height: 18px;
  accent-color: #A2D2FF;
  cursor: pointer;
}

.checkbox-label-text {
  font-size: 0.9rem;
  color: #555;
  font-weight: 500;
}

.helper-text {
  margin: 0.5rem 0 0 0;
  font-size: 0.75rem;
  color: #999;
}

.calendar-type-group {
  margin-bottom: 0;
}
</style>
