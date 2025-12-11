-- 조회하고 싶은 사용자 ID 설정
SET @userId := 1;  -- ← 여기 숫자를 원하는 user_id로 변경

/* 1. users: 유저 기본 정보 */
SELECT *
FROM users
WHERE user_id = @userId;


/* 2. dreams + emotion_scores + dream_results
   유저의 모든 꿈 + 감정 정보 + AI 해몽 결과까지 한 번에 조회 */
SELECT
    d.dream_id,
    d.user_id,
    d.dream_date,
    d.title,
    d.content,
    d.created_date         AS dream_created_date,
    d.updated_date         AS dream_updated_date,
    d.deleted_date         AS dream_deleted_date,
    e.emotion_id,
    e.emotion_name,
    e.score                AS emotion_score,
    r.id                   AS result_id,
    r.dream_interpretation,
    r.today_fortune_summary,
    r.lucky_color_name,
    r.lucky_color_number,
    r.lucky_color_reason,
    r.lucky_item_name,
    r.lucky_item_reason,
    r.image_url,
    r.created_date         AS result_created_date,
    r.updated_date         AS result_updated_date,
    r.deleted_date         AS result_deleted_date
FROM dreams d
JOIN emotion_scores e
    ON d.emotion_id = e.emotion_id
LEFT JOIN dream_results r
    ON r.dream_id = d.dream_id
WHERE d.user_id = @userId
ORDER BY d.dream_date, d.dream_id;


/* 3. dream_monthly_analysis: 월별 꿈 분석 */
SELECT *
FROM dream_monthly_analysis
WHERE user_id = @userId
ORDER BY year, month;


/* 4. dream_monthly_memo: 월별 메모 */
SELECT *
FROM dream_monthly_memo
WHERE user_id = @userId
ORDER BY year, month, memo_id;
