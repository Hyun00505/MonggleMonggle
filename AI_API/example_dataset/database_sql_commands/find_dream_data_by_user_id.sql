USE `dream_db`;

SELECT 
    d.dream_id,
    d.dream_date,
    d.title,
    d.content,
    e.emotion_name,
    e.score AS emotion_score,
    d.created_date
FROM dreams d
JOIN emotion_scores e ON d.emotion_id = e.emotion_id
WHERE d.user_id = 8  -- 원하는 user_id로 변경
  AND d.deleted_date IS NULL
ORDER BY d.dream_date DESC;