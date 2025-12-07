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
          <div v-if="!isLogin" class="input-group">
            <input v-model="name" type="text" placeholder="이름" class="auth-input" required />
          </div>

          <div v-if="!isLogin" class="input-group">
            <label class="input-label">생년월일</label>
            <div class="date-input-wrapper">
              <input v-model="birthDate" type="text" placeholder="YYYY-MM-DD" class="auth-input date-input" required maxlength="10" @input="formatBirthDate" @keydown="handleBirthDateKeydown" />
              <input ref="datePickerRef" type="date" class="hidden-date-picker" @change="onDatePickerChange" max="9999-12-31" />
              <button type="button" class="calendar-btn" @click="openDatePicker">📅</button>
            </div>
          </div>

          <div v-if="!isLogin" class="input-group">
            <label class="input-label">성별</label>
            <div class="radio-group">
              <label class="radio-label">
                <input type="radio" v-model="gender" value="male" class="custom-radio" required />
                <span class="radio-label-text">남</span>
              </label>
              <label class="radio-label">
                <input type="radio" v-model="gender" value="female" class="custom-radio" required />
                <span class="radio-label-text">여</span>
              </label>
            </div>
          </div>

          <div v-if="!isLogin" class="input-group">
            <label class="input-label">생년월일 달력 유형</label>
            <div class="radio-group calendar-type-group">
              <label class="radio-label">
                <input type="radio" v-model="calendarBase" value="solar" class="custom-radio" required />
                <span class="radio-label-text">양력</span>
              </label>
              <label class="radio-label">
                <input type="radio" v-model="calendarBase" value="lunar" class="custom-radio" required />
                <span class="radio-label-text">음력</span>
              </label>
            </div>
          </div>

          <div v-if="isLogin" class="input-group">
            <input v-model="loginId" type="text" placeholder="아이디" class="auth-input" required @input="allowOnlyAlphaNumeric" />
          </div>

          <div v-else class="input-group">
            <input v-model="signupId" type="text" placeholder="아이디" class="auth-input" required @input="allowOnlyAlphaNumeric" />
            <p v-if="!isLogin && loginError" class="error-text">{{ loginError }}</p>
          </div>

          <div v-if="isLogin" class="input-group">
            <input v-model="loginPassword" type="password" placeholder="비밀번호" class="auth-input" required @input="allowOnlyAlphaNumeric" />
            <p v-if="isLogin && loginError" class="error-text">{{ loginError }}</p>
          </div>

          <div v-else class="input-group">
            <input v-model="signupPassword" type="password" placeholder="비밀번호" class="auth-input" required @input="allowOnlyAlphaNumeric" />
          </div>

          <div v-if="!isLogin" class="input-group">
            <input v-model="confirmPassword" type="password" placeholder="비밀번호 확인" class="auth-input" required @input="allowOnlyAlphaNumeric" />
            <p v-if="passwordError" class="error-text">{{ passwordError }}</p>
          </div>

          <button type="submit" class="submit-btn">
            {{ isLogin ? "로그인" : "회원가입" }}
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
import { ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useDreamEntriesStore } from "../stores/dreamEntriesStore";
import { useAuthStore } from "../stores/authStore";
import { usePasswordValidation } from "../composables/usePasswordValidation";
import { useInputValidation } from "../composables/useInputValidation";

const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const authStore = useAuthStore();
const { allowOnlyAlphaNumeric } = useInputValidation();

const isLogin = ref(true);
const loginId = ref("");
const loginPassword = ref("");
const signupId = ref("");
const signupPassword = ref("");
const confirmPassword = ref("");
const name = ref("");
const birthDate = ref("");
const gender = ref("");
const calendarBase = ref("solar"); // 양력/음력 선택
const loginError = ref("");
const isSubmitting = ref(false);
const datePickerRef = ref(null);

// 실제 API로 전송할 calendarType 계산
function getCalendarType() {
  return calendarBase.value === "solar" ? "solar" : "lunar";
}

// 생년월일 입력 시 자동 포맷팅 (YYYY-MM-DD)
function formatBirthDate(event) {
  const input = event.target;

  // 숫자만 추출
  let digits = input.value.replace(/\D/g, "");

  // 최대 8자리까지만
  if (digits.length > 8) {
    digits = digits.slice(0, 8);
  }

  // 포맷팅
  let formatted = digits;
  if (digits.length > 6) {
    formatted = digits.slice(0, 4) + "-" + digits.slice(4, 6) + "-" + digits.slice(6);
  } else if (digits.length > 4) {
    formatted = digits.slice(0, 4) + "-" + digits.slice(4);
  }

  birthDate.value = formatted;

  // 커서를 항상 끝으로 이동
  setTimeout(() => {
    input.setSelectionRange(formatted.length, formatted.length);
  }, 0);
}

// 백스페이스 키 처리 - 하이픈 앞에서 멈추는 버그 수정
function handleBirthDateKeydown(event) {
  if (event.key === "Backspace") {
    const input = event.target;
    const cursorPos = input.selectionStart;
    const selectionEnd = input.selectionEnd;
    const value = birthDate.value;

    // 선택된 텍스트가 없고, 커서가 하이픈 바로 뒤에 있으면
    if (cursorPos === selectionEnd && cursorPos > 0 && value[cursorPos - 1] === "-") {
      event.preventDefault();
      // 숫자만 추출해서 마지막 숫자 하나 제거 후 다시 포맷팅
      let digits = value.replace(/\D/g, "");
      digits = digits.slice(0, -1);

      let formatted = digits;
      if (digits.length > 6) {
        formatted = digits.slice(0, 4) + "-" + digits.slice(4, 6) + "-" + digits.slice(6);
      } else if (digits.length > 4) {
        formatted = digits.slice(0, 4) + "-" + digits.slice(4);
      }

      birthDate.value = formatted;
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
  birthDate.value = event.target.value;
}

const { passwordError, validateMatch } = usePasswordValidation(signupPassword, confirmPassword);

function toggleAuthMode() {
  isLogin.value = !isLogin.value;
  // 모드 전환 시 상태 초기화
  loginError.value = "";
  loginId.value = "";
  loginPassword.value = "";
  signupId.value = "";
  signupPassword.value = "";
  confirmPassword.value = "";
  name.value = "";
  birthDate.value = "";
  gender.value = "";
  calendarBase.value = "solar";
  authStore.clearError();
}

async function handleSubmit() {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    if (!isLogin.value) {
      // 회원가입
      if (!validateMatch()) {
        loginError.value = "비밀번호가 일치하지 않습니다!";
        return;
      }

      // 필수 항목 확인
      if (!signupId.value || !signupPassword.value || !name.value || !birthDate.value || !gender.value || !calendarBase.value) {
        let missing = [];
        if (!signupId.value) missing.push("아이디");
        if (!signupPassword.value) missing.push("비밀번호");
        if (!name.value) missing.push("이름");
        if (!birthDate.value) missing.push("생년월일");
        if (!gender.value) missing.push("성별");
        if (!calendarBase.value) missing.push("달력 유형");

        loginError.value = `다음 항목을 입력해주세요: ${missing.join(", ")}`;
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
      console.log("회원가입 데이터:", signupData);
      await authStore.signup(signupData);

      alert("회원가입이 완료되었습니다.");
    } else {
      // 로그인
      await authStore.login({
        loginId: loginId.value,
        password: loginPassword.value,
      });
    }

    loginError.value = "";

    // 로그인 성공 시 서버에서 현재 월의 꿈 데이터 불러오기
    const now = new Date();
    await dreamEntriesStore.fetchDreamsByMonth(now.getFullYear(), now.getMonth() + 1);

    router.push({ name: "calendar" });
  } catch (err) {
    loginError.value = authStore.error || "처리 중 오류가 발생했습니다.";
  } finally {
    isSubmitting.value = false;
  }
}

watch([loginId, loginPassword, signupId], () => {
  if (loginError.value) {
    loginError.value = "";
    authStore.clearError();
  }
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Dongle:wght@300;400;700&display=swap");

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
  /* Text Gradient: Pastel Purple -> Pink -> Blue */
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
  right: 40px;
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
