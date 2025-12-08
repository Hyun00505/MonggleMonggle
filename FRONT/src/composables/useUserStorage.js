import { ref } from 'vue';

const STORAGE_KEY = 'dreamUsers';
const USER_INFO_KEY = 'currentUser'; // 로그인 시 authService에서 저장하는 키와 동일하게 통일

export function useUserStorage() {
  
  // --- Database Helper Methods ---
  
  function loadAllUsers() {
    if (typeof window === 'undefined') return [];
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return stored ? JSON.parse(stored) : [];
    } catch (error) {
      console.error('Failed to parse users from storage', error);
      return [];
    }
  }

  function saveAllUsers(users) {
    if (typeof window === 'undefined') return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(users));
  }

  function findUserById(loginId) {
    const users = loadAllUsers();
    return users.find(user => user.loginId === loginId);
  }

  function checkIdExists(loginId) {
    const users = loadAllUsers();
    return users.some(user => user.loginId === loginId);
  }

  function registerUser(userData) {
    const users = loadAllUsers();
    if (users.some(user => user.loginId === userData.loginId)) {
      throw new Error('이미 사용 중인 아이디입니다.');
    }
    users.push(userData);
    saveAllUsers(users);
  }

  function updateUserInDb(userData, originalId = null) {
    const users = loadAllUsers();
    const targetId = originalId || userData.loginId;
    const index = users.findIndex(u => u.loginId === targetId);
    
    if (index === -1) {
      throw new Error('사용자를 찾을 수 없습니다.');
    }

    // If ID is being changed
    if (originalId && originalId !== userData.loginId) {
      if (users.some(u => u.loginId === userData.loginId)) {
        throw new Error('이미 사용 중인 아이디입니다.');
      }
    }

    users[index] = { ...users[index], ...userData };
    saveAllUsers(users);
  }

  function deleteUserFromDb(loginId) {
    const users = loadAllUsers();
    const filtered = users.filter(u => u.loginId !== loginId);
    saveAllUsers(filtered);
  }

  // --- Session Helper Methods ---

  function getSessionUser() {
    if (typeof window === 'undefined') return null;
    const stored = localStorage.getItem(USER_INFO_KEY);
    return stored ? JSON.parse(stored) : null;
  }

  function setSessionUser(userData) {
    if (typeof window === 'undefined') return;
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userData));
  }

  function clearSessionUser() {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(USER_INFO_KEY);
  }

  return {
    loadAllUsers,
    saveAllUsers, 
    findUserById,
    checkIdExists,
    registerUser,
    updateUserInDb,
    deleteUserFromDb,
    getSessionUser,
    setSessionUser,
    clearSessionUser
  };
}

