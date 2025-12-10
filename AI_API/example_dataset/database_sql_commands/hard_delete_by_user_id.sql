USE `dream_db`;

-- 1. 안전 업데이트 모드 해제 (필수)
SET SQL_SAFE_UPDATES = 0;

START TRANSACTION;

-- =============================================
-- [STEP 1] 자식 테이블 데이터 삭제 (Foreign Key 오류 방지)
-- =============================================

-- 1-1. 꿈 분석 결과 삭제 (가장 하위 데이터)
DELETE dr
FROM dream_results dr
INNER JOIN dreams d ON dr.dream_id = d.dream_id
WHERE d.user_id = 6;

-- 1-2. 꿈 일기 삭제 (중간 데이터)
DELETE FROM dreams
WHERE user_id = 6;

-- =============================================
-- [STEP 2] 부모 테이블(사용자 계정) 삭제
-- =============================================

-- 2. 사용자 테이블에서 계정 완전 삭제
DELETE FROM users
WHERE user_id = 6;

COMMIT;

-- 3. 안전 업데이트 모드 복구
SET SQL_SAFE_UPDATES = 1;

-- =============================================
-- [확인] 데이터가 조회되지 않아야 정상
-- =============================================
SELECT * FROM users WHERE user_id = 6;