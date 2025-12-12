<template>
  <div class="auth-wrapper">
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
          <!-- 이름 -->
          <div v-if="!isLogin" class="input-group">
            <input 
              v-model="formData.name" 
              type="text" 
              placeholder="이름" 
              class="auth-input" 
              @blur="validation.validateName(formData.name)"
            />
            <p v-if="validation.errors.name" class="error-text">{{ validation.errors.name }}</p>
          </div>

          <!-- 생년월일 -->
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

          <!-- 양력/음력 -->
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

          <!-- 성별 -->
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
            <input 
              v-model="formData.loginId" 
              type="text" 
              placeholder="아이디" 
              class="auth-input" 
              @input="validation.filterAlphaNumeric"
            />
            <p v-if="validation.errors.loginId" class="error-text">{{ validation.errors.loginId }}</p>
          </div>

          <!-- 아이디 (회원가입) -->
          <div v-else class="input-group">
            <input 
              v-model="formData.loginId" 
              type="text" 
              placeholder="아이디 (영문, 숫자 4~20자)" 
              class="auth-input" 
              @input="validation.filterAlphaNumeric"
              @blur="validation.validateLoginId(formData.loginId)"
            />
            <p v-if="validation.errors.loginId" class="error-text">{{ validation.errors.loginId }}</p>
          </div>

          <!-- 비밀번호 (로그인) -->
          <div v-if="isLogin" class="input-group">
            <input 
              v-model="formData.password" 
              type="password" 
              placeholder="비밀번호" 
              class="auth-input" 
              @input="validation.filterPassword"
            />
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
              @input="validation.filterPassword"
              @blur="validation.validateConfirmPassword(formData.password, formData.confirmPassword)"
            />
            <p v-if="validation.errors.confirmPassword" class="error-text">{{ validation.errors.confirmPassword }}</p>
          </div>

          <button type="submit" class="submit-btn" :disabled="isSubmitting">
            {{ isSubmitting ? '처리 중...' : (isLogin ? "로그인" : "회원가입") }}
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
</template>

<script setup>
import { ref, reactive, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { useAuthStore } from "../stores/authStore";
import { useFormValidation } from "../composables/useFormValidation";

const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const authStore = useAuthStore();
const validation = useFormValidation();

const isLogin = ref(true);
const isSubmitting = ref(false);
const serverError = ref("");
const datePickerRef = ref(null);

// 폼 데이터
const formData = reactive({
  name: "",
  loginId: "",
  password: "",
  confirmPassword: "",
  birthDate: "",
  gender: "",
  calendarType: "",
});

// 오늘 날짜 (YYYY-MM-DD 형식)
const today = computed(() => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
});

// 생년월일 입력 시 자동 포맷팅 (YYYY-MM-DD)
function formatBirthDate(event) {
  const input = event.target;
  let digits = input.value.replace(/\D/g, "");

  if (digits.length > 8) {
    digits = digits.slice(0, 8);
  }

  let formatted = digits;
  if (digits.length > 6) {
    formatted = digits.slice(0, 4) + "-" + digits.slice(4, 6) + "-" + digits.slice(6);
  } else if (digits.length > 4) {
    formatted = digits.slice(0, 4) + "-" + digits.slice(4);
  }

  formData.birthDate = formatted;

  setTimeout(() => {
    input.setSelectionRange(formatted.length, formatted.length);
  }, 0);
}

// 백스페이스 키 처리
function handleBirthDateKeydown(event) {
  if (event.key === "Backspace") {
    const input = event.target;
    const cursorPos = input.selectionStart;
    const selectionEnd = input.selectionEnd;
    const value = formData.birthDate;

    if (cursorPos === selectionEnd && cursorPos > 0 && value[cursorPos - 1] === "-") {
      event.preventDefault();
      let digits = value.replace(/\D/g, "");
      digits = digits.slice(0, -1);

      let formatted = digits;
      if (digits.length > 6) {
        formatted = digits.slice(0, 4) + "-" + digits.slice(4, 6) + "-" + digits.slice(6);
      } else if (digits.length > 4) {
        formatted = digits.slice(0, 4) + "-" + digits.slice(4);
      }

      formData.birthDate = formatted;
      setTimeout(() => {
        input.setSelectionRange(formatted.length, formatted.length);
      }, 0);
    }
  }
}

// 캘린더 열기
function openDatePicker() {
  if (datePickerRef.value) {
    datePickerRef.value.showPicker();
  }
}

// 캘린더에서 날짜 선택 시
function onDatePickerChange(event) {
  formData.birthDate = event.target.value;
  validation.validateBirthDate(formData.birthDate);
}

// 폼 초기화
function resetForm() {
  formData.name = "";
  formData.loginId = "";
  formData.password = "";
  formData.confirmPassword = "";
  formData.birthDate = "";
  formData.gender = "";
  formData.calendarType = "";
  validation.clearErrors();
  serverError.value = "";
}

// 모드 전환
function toggleAuthMode() {
  isLogin.value = !isLogin.value;
  resetForm();
  authStore.clearError();
}

// 폼 제출
async function handleSubmit() {
  if (isSubmitting.value) return;
  
  serverError.value = "";

  // 유효성 검사
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
      // 회원가입
      const signupData = {
        loginId: formData.loginId,
        password: formData.password,
        name: formData.name,
        birthDate: formData.birthDate,
        gender: validation.genderToBackend(formData.gender),
        calendarType: validation.calendarTypeToBackend(formData.calendarType),
      };
      
      await authStore.signup(signupData);
      alert("회원가입이 완료되었습니다.");
    } else {
      // 로그인
      await authStore.login({
        loginId: formData.loginId,
        password: formData.password,
      });
    }

    // 로그인 성공 시 꿈 데이터 불러오기
    const now = new Date();
    await dreamEntriesStore.fetchDreamsByMonth(now.getFullYear(), now.getMonth() + 1);

    router.push({ name: "calendar" });
  } catch (err) {
    serverError.value = authStore.error || "처리 중 오류가 발생했습니다.";
  } finally {
    isSubmitting.value = false;
  }
}

// 입력 시 서버 에러 초기화
watch([() => formData.loginId, () => formData.password], () => {
  if (serverError.value) {
    serverError.value = "";
    authStore.clearError();
  }
});
</script>

<style scoped>
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
  font-family: "Dongle", sans-serif;
  font-size: 5rem;
  font-weight: 700;
  background: linear-gradient(to right, #cdb4db, #ffc8dd, #a2d2ff);
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
</style>
