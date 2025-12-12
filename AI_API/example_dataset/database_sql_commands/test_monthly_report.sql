-- ============================================
-- 월별 리포트 테스트용 SQL 쿼리
-- ============================================

USE `dream_db`;

-- ============================================
-- 1. 현재 사용자 목록 확인
-- ============================================
SELECT 
    user_id,
    login_id,
    name,
    birth_date,
    created_date
FROM users
ORDER BY user_id;

-- ============================================
-- 2. 특정 사용자의 꿈 일기 확인 (월별)
-- ============================================
-- 사용 예시: user_id = 1, 2025년 11월
SELECT 
    d.dream_id,
    d.user_id,
    d.dream_date,
    d.title,
    LEFT(d.content, 50) AS content_preview,
    d.emotion_id,
    CASE 
        WHEN dr.id IS NOT NULL THEN 'O'
        ELSE 'X'
    END AS has_result
FROM dreams d
LEFT JOIN dream_results dr ON d.dream_id = dr.dream_id AND dr.deleted_date IS NULL
WHERE d.user_id = 1  -- ⬅️ 여기에 사용자 ID 입력
  AND d.deleted_date IS NULL
  AND YEAR(d.dream_date) = 2025  -- ⬅️ 여기에 연도 입력
  AND MONTH(d.dream_date) = 11   -- ⬅️ 여기에 월 입력
ORDER BY d.dream_date;

-- ============================================
-- 3. 특정 월의 월별 리포트 확인
-- ============================================
-- 사용 예시: user_id = 1, 2025년 11월
SELECT 
    analysis_id,
    user_id,
    year,
    month,
    dream_count,
    avg_emotion_score,
    LEFT(monthly_report, 100) AS report_preview,
    created_date,
    updated_date
FROM dream_monthly_analysis
WHERE user_id = 1      -- ⬅️ 여기에 사용자 ID 입력
  AND year = 2025      -- ⬅️ 여기에 연도 입력
  AND month = 11;      -- ⬅️ 여기에 월 입력

-- ============================================
-- 4. 월별 리포트 삭제 (테스트용)
-- ============================================
-- ⚠️ 주의: 이 쿼리를 실행하면 해당 월의 리포트가 삭제됩니다!
-- 사용 예시: user_id = 1, 2025년 11월
DELETE FROM dream_monthly_analysis
WHERE user_id = 1      -- ⬅️ 여기에 사용자 ID 입력
  AND year = 2025      -- ⬅️ 여기에 연도 입력
  AND month = 11;      -- ⬅️ 여기에 월 입력

-- ============================================
-- 5. 삭제 확인
-- ============================================
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ 삭제 완료! 이제 월별 분석 페이지에 접속하면 자동 생성됩니다.'
        ELSE '❌ 아직 데이터가 남아있습니다.'
    END AS deletion_status
FROM dream_monthly_analysis
WHERE user_id = 1      -- ⬅️ 여기에 사용자 ID 입력
  AND year = 2025      -- ⬅️ 여기에 연도 입력
  AND month = 11;      -- ⬅️ 여기에 월 입력

-- ============================================
-- 6. 모든 월별 리포트 조회 (전체 현황 확인)
-- ============================================
SELECT 
    u.user_id,
    u.name AS user_name,
    ma.year,
    ma.month,
    ma.dream_count,
    ma.avg_emotion_score,
    ma.created_date,
    ma.updated_date
FROM dream_monthly_analysis ma
INNER JOIN users u ON ma.user_id = u.user_id
ORDER BY u.user_id, ma.year DESC, ma.month DESC;

-- ============================================
-- 7. 특정 사용자의 월별 꿈 개수 확인 (리포트 없이도)
-- ============================================
-- 실제로 꿈 일기가 있는지 확인용
SELECT 
    user_id,
    YEAR(dream_date) AS year,
    MONTH(dream_date) AS month,
    COUNT(*) AS dream_count,
    AVG(emotion_id) AS avg_emotion
FROM dreams
WHERE user_id = 1      -- ⬅️ 여기에 사용자 ID 입력
  AND deleted_date IS NULL
GROUP BY user_id, YEAR(dream_date), MONTH(dream_date)
ORDER BY year DESC, month DESC;
