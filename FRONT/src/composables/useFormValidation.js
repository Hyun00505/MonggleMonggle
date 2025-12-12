import { ref, reactive, computed, watch } from 'vue';

/**
 * 통합 폼 유효성 검사 composable
 * - 회원가입/마이페이지에서 공통으로 사용
 */
export function useFormValidation() {
  // 에러 상태 관리
  const errors = reactive({
    name: '',
    loginId: '',
    password: '',
    confirmPassword: '',
    birthDate: '',
    gender: '',
    calendarType: '',
  });

  // 전체 폼 유효성
  const isFormValid = computed(() => {
    return Object.values(errors).every(error => !error);
  });

  // ========== 유효성 검사 규칙 ==========

  /**
   * 이름 검사
   * - 필수, 2~20자
   */
  function validateName(value) {
    if (!value || !value.trim()) {
      errors.name = '이름을 입력해주세요.';
      return false;
    }
    if (value.trim().length < 2) {
      errors.name = '이름은 2자 이상이어야 합니다.';
      return false;
    }
    if (value.trim().length > 20) {
      errors.name = '이름은 20자 이하여야 합니다.';
      return false;
    }
    errors.name = '';
    return true;
  }

  /**
   * 아이디 검사
   * - 필수, 영문+숫자만, 4~20자
   */
  function validateLoginId(value) {
    if (!value || !value.trim()) {
      errors.loginId = '아이디를 입력해주세요.';
      return false;
    }
    if (!/^[A-Za-z0-9]+$/.test(value)) {
      errors.loginId = '아이디는 영문과 숫자만 사용 가능합니다.';
      return false;
    }
    if (value.length < 4) {
      errors.loginId = '아이디는 4자 이상이어야 합니다.';
      return false;
    }
    if (value.length > 20) {
      errors.loginId = '아이디는 20자 이하여야 합니다.';
      return false;
    }
    errors.loginId = '';
    return true;
  }

  /**
   * 비밀번호 검사
   * - 필수, 영문+숫자+특수문자 허용, 6~20자
   */
  function validatePassword(value, isRequired = true) {
    if (!value) {
      if (isRequired) {
        errors.password = '비밀번호를 입력해주세요.';
        return false;
      }
      errors.password = '';
      return true;
    }
    // 영문, 숫자, 특수문자만 허용 (공백 제외)
    if (!/^[A-Za-z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?`~]+$/.test(value)) {
      errors.password = '비밀번호는 영문, 숫자, 특수문자만 사용 가능합니다.';
      return false;
    }
    if (value.length < 8) {
      errors.password = '비밀번호는 8자 이상이어야 합니다.';
      return false;
    }
    if (value.length > 20) {
      errors.password = '비밀번호는 20자 이하여야 합니다.';
      return false;
    }
    errors.password = '';
    return true;
  }

  /**
   * 비밀번호 확인 검사
   * - 비밀번호와 일치해야 함
   */
  function validateConfirmPassword(password, confirmPassword) {
    if (!confirmPassword) {
      if (password) {
        errors.confirmPassword = '비밀번호 확인을 입력해주세요.';
        return false;
      }
      errors.confirmPassword = '';
      return true;
    }
    if (password !== confirmPassword) {
      errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
      return false;
    }
    errors.confirmPassword = '';
    return true;
  }

  /**
   * 생년월일 검사
   * - 필수, 유효한 날짜, 미래 날짜 불가
   */
  function validateBirthDate(value) {
    if (!value) {
      errors.birthDate = '생년월일을 입력해주세요.';
      return false;
    }

    // YYYY-MM-DD 형식 검사
    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
    if (!dateRegex.test(value)) {
      errors.birthDate = '생년월일 형식이 올바르지 않습니다. (YYYY-MM-DD)';
      return false;
    }

    // 유효한 날짜인지 검사
    const date = new Date(value);
    if (isNaN(date.getTime())) {
      errors.birthDate = '유효하지 않은 날짜입니다.';
      return false;
    }

    // 입력된 값과 파싱된 날짜가 일치하는지 (2025-02-30 같은 경우 방지)
    const [year, month, day] = value.split('-').map(Number);
    if (date.getFullYear() !== year || date.getMonth() + 1 !== month || date.getDate() !== day) {
      errors.birthDate = '존재하지 않는 날짜입니다.';
      return false;
    }

    // 미래 날짜 검사
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (date > today) {
      errors.birthDate = '미래 날짜는 선택할 수 없습니다.';
      return false;
    }

    // 너무 오래된 날짜 검사 (1900년 이전)
    if (year < 1900) {
      errors.birthDate = '1900년 이후의 날짜를 입력해주세요.';
      return false;
    }

    errors.birthDate = '';
    return true;
  }

  /**
   * 성별 검사
   * - 필수, male 또는 female
   */
  function validateGender(value) {
    if (!value) {
      errors.gender = '성별을 선택해주세요.';
      return false;
    }
    if (!['male', 'female'].includes(value)) {
      errors.gender = '올바른 성별을 선택해주세요.';
      return false;
    }
    errors.gender = '';
    return true;
  }

  /**
   * 달력 유형 검사
   * - 필수, solar 또는 lunar
   */
  function validateCalendarType(value) {
    if (!value) {
      errors.calendarType = '달력 유형을 선택해주세요.';
      return false;
    }
    if (!['solar', 'lunar'].includes(value)) {
      errors.calendarType = '올바른 달력 유형을 선택해주세요.';
      return false;
    }
    errors.calendarType = '';
    return true;
  }

  // ========== 회원가입 전체 검사 ==========

  function validateSignupForm(formData) {
    const nameValid = validateName(formData.name);
    const loginIdValid = validateLoginId(formData.loginId);
    const passwordValid = validatePassword(formData.password, true);
    const confirmPasswordValid = validateConfirmPassword(formData.password, formData.confirmPassword);
    const birthDateValid = validateBirthDate(formData.birthDate);
    const genderValid = validateGender(formData.gender);
    const calendarTypeValid = validateCalendarType(formData.calendarType);

    return nameValid && loginIdValid && passwordValid && confirmPasswordValid && 
           birthDateValid && genderValid && calendarTypeValid;
  }

  // ========== 마이페이지 수정 전체 검사 ==========

  function validateUpdateForm(formData, hasPasswordChange = false) {
    const nameValid = validateName(formData.name);
    const birthDateValid = validateBirthDate(formData.birthDate);
    const genderValid = validateGender(formData.gender);
    const calendarTypeValid = validateCalendarType(formData.calendarType);

    let passwordValid = true;
    let confirmPasswordValid = true;

    if (hasPasswordChange) {
      passwordValid = validatePassword(formData.password, true);
      confirmPasswordValid = validateConfirmPassword(formData.password, formData.confirmPassword);
    }

    return nameValid && birthDateValid && genderValid && calendarTypeValid && 
           passwordValid && confirmPasswordValid;
  }

  // ========== 로그인 검사 ==========

  function validateLoginForm(formData) {
    let isValid = true;

    if (!formData.loginId || !formData.loginId.trim()) {
      errors.loginId = '아이디를 입력해주세요.';
      isValid = false;
    } else {
      errors.loginId = '';
    }

    if (!formData.password) {
      errors.password = '비밀번호를 입력해주세요.';
      isValid = false;
    } else {
      errors.password = '';
    }

    return isValid;
  }

  // ========== 입력 필터링 ==========

  /**
   * 영문+숫자만 허용하는 입력 필터 (아이디용)
   */
  function filterAlphaNumeric(event) {
    const value = event.target.value;
    const filtered = value.replace(/[^A-Za-z0-9]/g, '');
    
    if (value !== filtered) {
      event.target.value = filtered;
      event.target.dispatchEvent(new Event('input', { bubbles: true }));
    }
    return filtered;
  }

  /**
   * 영문+숫자+특수문자 허용, 공백 제거 (비밀번호용)
   */
  function filterPassword(event) {
    const value = event.target.value;
    // 공백만 제거
    const filtered = value.replace(/\s/g, '');
    
    if (value !== filtered) {
      event.target.value = filtered;
      event.target.dispatchEvent(new Event('input', { bubbles: true }));
    }
    return filtered;
  }

  // ========== 데이터 변환 유틸리티 ==========

  /**
   * 프론트엔드 gender → 백엔드 gender
   * male → M, female → F
   */
  function genderToBackend(gender) {
    return gender === 'male' ? 'M' : 'F';
  }

  /**
   * 백엔드 gender → 프론트엔드 gender
   * M → male, F → female
   */
  function genderToFrontend(gender) {
    return gender === 'M' ? 'male' : 'female';
  }

  /**
   * 프론트엔드 calendarType → 백엔드 calendarType
   * lunar → lunarGeneral (백엔드 호환)
   */
  function calendarTypeToBackend(calendarType) {
    if (calendarType === 'lunar') {
      return 'lunarGeneral';
    }
    return calendarType;
  }

  /**
   * 백엔드 calendarType → 프론트엔드 calendarType
   */
  function calendarTypeToFrontend(calendarType) {
    if (calendarType === 'lunarGeneral' || calendarType === 'lunarLeap') {
      return 'lunar';
    }
    return calendarType;
  }

  // ========== 에러 초기화 ==========

  function clearErrors() {
    Object.keys(errors).forEach(key => {
      errors[key] = '';
    });
  }

  function clearError(field) {
    if (errors[field] !== undefined) {
      errors[field] = '';
    }
  }

  return {
    // 에러 상태
    errors,
    isFormValid,

    // 개별 검사
    validateName,
    validateLoginId,
    validatePassword,
    validateConfirmPassword,
    validateBirthDate,
    validateGender,
    validateCalendarType,

    // 전체 폼 검사
    validateSignupForm,
    validateUpdateForm,
    validateLoginForm,

    // 입력 필터
    filterAlphaNumeric,
    filterPassword,

    // 데이터 변환
    genderToBackend,
    genderToFrontend,
    calendarTypeToBackend,
    calendarTypeToFrontend,

    // 에러 관리
    clearErrors,
    clearError,
  };
}

