<template>
  <div class="common-card">
    <div class="page-header">
      <h2 class="page-title">마이페이지</h2>
      <p class="page-subtitle">내 정보를 수정해보세요</p>
    </div>

    <div class="common-form">
      <!-- 이름 -->
      <div class="input-group labeled">
        <label class="input-label">이름</label>
        <input 
          v-model="userInfo.name" 
          type="text" 
          class="custom-input"
          placeholder="이름"
        />
      </div>

      <!-- 생년월일 -->
      <div class="input-group labeled">
        <label class="input-label">생년월일</label>
        <input 
          v-model="userInfo.birthDate" 
          type="date" 
          data-placeholder="YYYY-MM-DD"
          class="custom-input date-input"
          required
          @click="showDatePicker"
        />
      </div>

      <!-- 성별 -->
      <div class="input-group labeled">
        <label class="input-label">성별</label>
        <div class="radio-group">
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="userInfo.gender" 
              value="male" 
              class="custom-radio"
            />
            <span class="radio-text">남</span>
          </label>
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="userInfo.gender" 
              value="female" 
              class="custom-radio"
            />
            <span class="radio-text">여</span>
          </label>
        </div>
      </div>

      <!-- 아이디 -->
      <div class="input-group labeled">
        <label class="input-label">아이디</label>
        <input 
          v-model="userInfo.loginId" 
          type="text" 
          class="custom-input"
          placeholder="아이디"
          @input="allowOnlyAlphaNumeric"
        />
      </div>

      <!-- 비밀번호 -->
      <div class="input-group">
        <input 
          v-model="userInfo.password" 
          type="password" 
          class="custom-input"
          placeholder="비밀번호"
          @input="allowOnlyAlphaNumeric"
        />
      </div>

      <!-- 비밀번호 확인 -->
      <div class="input-group">
        <input 
          v-model="userInfo.confirmPassword" 
          type="password" 
          class="custom-input"
          placeholder="비밀번호 확인"
          @input="allowOnlyAlphaNumeric"
        />
        <p v-if="passwordError" class="error-text">{{ passwordError }}</p>
      </div>

      <button class="submit-btn" @click="saveUserInfo">
        수정 완료
      </button>

      <div class="footer-actions">
        <span class="withdraw-text" @click="handleWithdraw">서비스 탈퇴</span>
        <span class="back-text" @click="handleBack">뒤로가기</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, toRef, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useDreamEntriesStore } from '../stores/dreamEntriesStore';
import { useAuthStore } from '../stores/authStore';
import { useUserStorage } from '../composables/useUserStorage';
import { usePasswordValidation } from '../composables/usePasswordValidation';
import { useInputValidation } from '../composables/useInputValidation';

const router = useRouter();
const dreamEntriesStore = useDreamEntriesStore();
const authStore = useAuthStore();
const { resetAll } = dreamEntriesStore;
const { setSessionUser, clearSessionUser, getSessionUser, updateUserInDb } = useUserStorage();
const { allowOnlyAlphaNumeric } = useInputValidation();

const userInfo = reactive({
  loginId: '',
  password: '',
  confirmPassword: '',
  name: '',
  birthDate: '',
  gender: ''
});

const originalLoginId = ref('');

// Create refs for validation composable
const passwordRef = toRef(userInfo, 'password');
const confirmPasswordRef = toRef(userInfo, 'confirmPassword');

const { passwordError, validateMatch } = usePasswordValidation(passwordRef, confirmPasswordRef);

function showDatePicker(event) {
  try {
    event.target.showPicker();
  } catch (error) {
    // ignore
  }
}

// This function is now handled by the composable and watcher, 
// but the template calls it on @input.
// We can remove @input="checkPasswordMatch" from template and rely on the watcher in composable
// or we can keep it if we want manual trigger.
// The composable watches changes, so we don't need manual @input handler if we use the error from composable.

function handleBack() {
  router.push({ name: 'calendar' });
}

function saveUserInfo() {
  if (!isFormComplete()) {
    alert('모든 항목을 입력해주세요.');
    return;
  }

  // 비밀번호 변경 시에만 검증
  if (!validateMatch()) {
    alert('비밀번호가 일치하지 않습니다.');
    return;
  }

  // 변경할 데이터 준비
  const dataToSave = { ...userInfo };
  delete dataToSave.confirmPassword; // 확인용 필드 제거

  // 비밀번호를 입력하지 않았다면 기존 비밀번호 유지해야 함
  // 이를 위해 세션(또는 DB)에서 현재 사용자 정보를 다시 가져와야 할 수 있음
  // 여기서는 userInfo에 password가 비어있으면 기존 정보를 유지하는 로직이 필요
  
  const currentUser = getSessionUser();
  if (!userInfo.password && currentUser) {
    dataToSave.password = currentUser.password;
  }
  
  try {
    // DB 업데이트 (기존 아이디 기준)
    updateUserInDb(dataToSave, originalLoginId.value);
    
    // 세션 업데이트
    setSessionUser(dataToSave);
    
    // 성공 시 원본 아이디 업데이트
    originalLoginId.value = dataToSave.loginId;
    
    // 비밀번호 필드 초기화
    userInfo.password = '';
    userInfo.confirmPassword = '';

    alert('정보가 수정되었습니다.');
    router.push({ name: 'calendar' });
  } catch (error) {
    alert(error.message);
  }
}

function isFormComplete() {
  return (
    userInfo.name?.trim() &&
    userInfo.birthDate &&
    userInfo.gender &&
    userInfo.loginId?.trim() &&
    userInfo.password &&
    userInfo.confirmPassword
  );
}

async function handleWithdraw() {
  if (confirm('정말로 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.')) {
    try {
      await authStore.deleteAccount();
      resetAll();
      clearSessionUser();
      alert('서비스 탈퇴가 완료되었습니다.');
      router.push({ name: 'auth' });
    } catch (err) {
      alert(authStore.error || '탈퇴 처리 중 오류가 발생했습니다.');
    }
  }
}

onMounted(() => {
  const savedInfo = getSessionUser();
  if (savedInfo) {
    // Merge saved info
    Object.assign(userInfo, savedInfo);
    // 비밀번호 필드는 비워둠 (보안 및 UX)
    userInfo.password = '';
    userInfo.confirmPassword = '';
    originalLoginId.value = savedInfo.loginId;
  }
});
</script>

<style scoped>
.input-group.labeled {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
</style>
