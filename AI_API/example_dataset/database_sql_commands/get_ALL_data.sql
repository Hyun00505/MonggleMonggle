USE dream_db;
SELECT * FROM users;

-- ====================================
-- 1. 전체 데이터 통합 조회 (꿈 + 해몽 + 이미지)
-- ====================================
SELECT 
    u.user_id,
    u.name AS '사용자명',
    d.dream_id,
    d.dream_date AS '꿈 날짜',
    d.title AS '꿈 제목',
    d.content AS '꿈 내용',
    e.emotion_name AS '감정',
    dr.dream_interpretation AS '꿈 해석',
    dr.today_fortune_summary AS '오늘의 운세',
    dr.lucky_color_name AS '행운의 색상',
    dr.lucky_item_name AS '행운의 아이템',
    dr.image_url AS '이미지 URL',
    d.created_date AS '작성일'
FROM dreams d
LEFT JOIN users u ON d.user_id = u.user_id
LEFT JOIN emotion_scores e ON d.emotion_id = e.emotion_id
LEFT JOIN dream_results dr ON d.dream_id = dr.dream_id
WHERE d.deleted_date IS NULL
ORDER BY d.dream_date DESC;

-- ====================================
-- 2. 특정 사용자의 꿈 + 이미지 조회
-- ====================================
SELECT 
    d.dream_date AS '날짜',
    d.title AS '제목',
    LEFT(d.content, 100) AS '내용 미리보기',
    dr.lucky_color_name AS '행운의 색',
    dr.image_url AS '이미지',
    CASE 
        WHEN dr.id IS NOT NULL THEN '해몽 완료'
        ELSE '해몽 안함'
    END AS '해몽 상태'
FROM dreams d
LEFT JOIN dream_results dr ON d.dream_id = dr.dream_id
WHERE d.user_id = 4  -- 사용자 ID 변경 가능
  AND d.deleted_date IS NULL
ORDER BY d.dream_date DESC;

-- ====================================
-- 3. 이미지가 있는 꿈만 조회
-- ====================================
SELECT 
    d.dream_id,
    d.dream_date AS '날짜',
    d.title AS '제목',
    dr.image_url AS '이미지 URL',
    dr.lucky_color_name AS '행운의 색'
FROM dreams d
JOIN dream_results dr ON d.dream_id = dr.dream_id
WHERE dr.image_url IS NOT NULL 
  AND dr.image_url != ''
  AND d.deleted_date IS NULL
ORDER BY d.dream_date DESC;

-- ====================================
-- 4. 월별 꿈 통계 조회
-- ====================================
SELECT 
    DATE_FORMAT(d.dream_date, '%Y-%m') AS '월',
    COUNT(*) AS '꿈 개수',
    ROUND(AVG(e.score), 2) AS '평균 감정 점수',
    SUM(CASE WHEN dr.id IS NOT NULL THEN 1 ELSE 0 END) AS '해몽 완료',
    SUM(CASE WHEN dr.image_url IS NOT NULL AND dr.image_url != '' THEN 1 ELSE 0 END) AS '이미지 생성'
FROM dreams d
LEFT JOIN emotion_scores e ON d.emotion_id = e.emotion_id
LEFT JOIN dream_results dr ON d.dream_id = dr.dream_id
WHERE d.deleted_date IS NULL
GROUP BY DATE_FORMAT(d.dream_date, '%Y-%m')
ORDER BY 월 DESC;

-- ====================================
-- 5. 사용자별 전체 통계
-- ====================================
SELECT 
    u.user_id,
    u.name AS '사용자명',
    u.login_id AS '로그인 ID',
    COUNT(DISTINCT d.dream_id) AS '총 꿈 개수',
    COUNT(DISTINCT dr.id) AS '해몽 완료',
    SUM(CASE WHEN dr.image_url IS NOT NULL AND dr.image_url != '' THEN 1 ELSE 0 END) AS '이미지 생성'
FROM users u
LEFT JOIN dreams d ON u.user_id = d.user_id AND d.deleted_date IS NULL
LEFT JOIN dream_results dr ON d.dream_id = dr.dream_id AND dr.deleted_date IS NULL
WHERE u.deleted_date IS NULL
GROUP BY u.user_id, u.name, u.login_id;

-- ====================================
-- 6. 최근 생성된 이미지 URL 확인
-- ====================================
SELECT 
    dr.dream_id,
    d.title AS '꿈 제목',
    dr.image_url AS '이미지 URL',
    dr.created_date AS '생성일'
FROM dream_results dr
JOIN dreams d ON dr.dream_id = d.dream_id
WHERE dr.image_url IS NOT NULL 
  AND dr.image_url != ''
ORDER BY dr.created_date DESC
LIMIT 10;