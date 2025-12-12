/**
 * 날짜 관련 유틸리티 함수 모음
 */

/**
 * Date 객체를 YYYY-MM-DD 형식의 문자열로 변환
 * @param {Date} date - 변환할 날짜 객체
 * @returns {string} YYYY-MM-DD 형식의 문자열 (date가 없으면 빈 문자열)
 */
export function formatDateKey(date) {
  if (!date) return "";
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/**
 * 주어진 날짜가 오늘인지 확인
 * @param {Date} date - 확인할 날짜 객체
 * @returns {boolean} 오늘이면 true
 */
export function isTodayDate(date) {
  if (!date) return false;
  const today = new Date();
  return (
    date.getFullYear() === today.getFullYear() &&
    date.getMonth() === today.getMonth() &&
    date.getDate() === today.getDate()
  );
}

/**
 * 주어진 날짜가 미래인지 확인
 * @param {Date} date - 확인할 날짜 객체
 * @returns {boolean} 미래이면 true
 */
export function isFutureDate(date) {
  const today = new Date();
  const target = new Date(date);
  today.setHours(0, 0, 0, 0);
  target.setHours(0, 0, 0, 0);
  return target.getTime() > today.getTime();
}

