-- 특정 user_id의 role을 ADMIN으로 변경
USE dream_db;

UPDATE users 
SET role = 'ADMIN' , coin = 999
WHERE user_id = 2;