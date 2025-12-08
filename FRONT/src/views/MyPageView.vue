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

      <!-- 달력 유형 (양력/음력) -->
      <div class="input-group">
        <div class="radio-group">
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="userInfo.calendarType" 
              value="solar" 
              class="custom-radio"
            />
            <span class="radio-text">양력</span>
          </label>
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="userInfo.calendarType" 
              value="lunar" 
              class="custom-radio"
            />
            <span class="radio-text">음력</span>
          </label>
        </div>
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

      <!-- 아이디 (변경 불가) -->
      <div class="input-group labeled">
        <label class="input-label">아이디</label>
        <input 
          v-model="userInfo.loginId" 
          type="text" 
          class="custom-input"
          placeholder="아이디"
          disabled
        />
      </div>

      <!-- 비밀번호 (변경 시에만 입력) -->
      <div class="input-group labeled">
        <label class="input-label">비밀번호 변경</label>
        <input 
          v-model="userInfo.password" 
          type="password" 
          class="custom-input"
          placeholder="새 비밀번호 (변경 시에만 입력)"
          @input="allowOnlyAlphaNumeric"
        />
      </div>

      <!-- 비밀번호 확인 -->
      <div class="input-group">
        <input 
          v-model="userInfo.confirmPassword" 
          type="password" 
          class="custom-input"
          placeholder="새 비밀번호 확인"
          @input="allowOnlyAlphaNumeric"
        />
        <p v-if="passwordError" class="error-text">{{ passwordError }}</p>
      </div>

      <button class="submit-btn" @click="saveUserInfo" :disabled="isLoading">
        {{ isLoading ? '처리 중...' : '수정 완료' }}
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
const { clearSessionUser } = useUserStorage();
const { allowOnlyAlphaNumeric } = useInputValidation();

const userInfo = reactive({
  loginId: '',
  password: '',
  confirmPassword: '',
  name: '',
  birthDate: '',
  gender: '',
  calendarType: 'solar'
});

const originalLoginId = ref('');
const isLoading = ref(false);

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

async function saveUserInfo() {
  if (!isFormComplete()) {
    alert('모든 항목을 입력해주세요.');
    return;
  }

  // 비밀번호 변경 시에만 검증
  if (userInfo.password && !validateMatch()) {
    alert('비밀번호가 일치하지 않습니다.');
    return;
  }

  isLoading.value = true;
  
  try {
    // 백엔드 API 호출을 위한 데이터 준비
    const updateData = {
      name: userInfo.name,
      birthDate: userInfo.birthDate,
      gender: userInfo.gender === 'male' ? 'M' : 'F',
      calendarType: userInfo.calendarType,
    };
    
    // 비밀번호가 입력된 경우에만 포함
    if (userInfo.password) {
      updateData.password = userInfo.password;
    }
    
    // 백엔드 API를 통해 정보 수정
    await authStore.updateUser(updateData);
    
    // 비밀번호 필드 초기화
    userInfo.password = '';
    userInfo.confirmPassword = '';

    alert('정보가 수정되었습니다.');
    router.push({ name: 'calendar' });
  } catch (error) {
    alert(authStore.error || '정보 수정에 실패했습니다.');
  } finally {
    isLoading.value = false;
  }
}

function isFormComplete() {
  // 비밀번호는 변경하려는 경우에만 필수
  const passwordValid = !userInfo.password || (userInfo.password && userInfo.confirmPassword);
  
  return (
    userInfo.name?.trim() &&
    userInfo.birthDate &&
    userInfo.gender &&
    userInfo.calendarType &&
    userInfo.loginId?.trim() &&
    passwordValid
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

onMounted(async () => {
  isLoading.value = true;
  
  try {
    // 백엔드에서 최신 사용자 정보 가져오기
    const response = await authStore.fetchCurrentUser();
    
    if (response) {
      // gender 변환: 백엔드는 'M'/'F', 프론트엔드는 'male'/'female'
      const genderMapping = { 'M': 'male', 'F': 'female' };
      
      userInfo.loginId = response.loginId || '';
      userInfo.name = response.name || '';
      userInfo.birthDate = response.birthDate || '';
      userInfo.gender = genderMapping[response.gender] || response.gender || '';
      userInfo.calendarType = response.calendarType || 'solar';
      
      // 비밀번호 필드는 비워둠 (보안 및 UX)
      userInfo.password = '';
      userInfo.confirmPassword = '';
      originalLoginId.value = response.loginId || '';
    }
  } catch (error) {
    console.error('사용자 정보 조회 실패:', error);
    // 토큰이 없거나 유효하지 않은 경우 로그인 페이지로 이동
    router.push({ name: 'auth' });
  } finally {
    isLoading.value = false;
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
