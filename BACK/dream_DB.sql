-- Active: 1764825127477@@127.0.0.1@3306@dream_db
-- 1. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS `dream_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `dream_db`;

-- 2. 회원 정보 테이블 (users)
CREATE TABLE `users` (
    `user_id`       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '사용자 ID',
    `login_id`      VARCHAR(255)    NOT NULL COMMENT '로그인 아이디',
    `password`      VARCHAR(255)    NOT NULL COMMENT '비밀번호 (암호화)',
    `name`          VARCHAR(100)    NOT NULL COMMENT '이름',
    `birth_date`    DATE            NOT NULL COMMENT '생년월일',
    `gender`        CHAR(1)         NOT NULL COMMENT '성별 (M: 남성, F: 여성)',
    `calendar_type` VARCHAR(20)     NOT NULL DEFAULT 'solar' COMMENT '달력 유형 (solar, lunarGeneral, lunarLeap)',
    `created_date`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일',
    `updated_date`  DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    `deleted_date`  DATETIME        NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_users_login_id` (`login_id`)
) COMMENT = '회원 정보';

-- 3. 감정 점수 테이블 (emotion_scores)
CREATE TABLE `emotion_scores` (
    `emotion_id`   TINYINT         NOT NULL AUTO_INCREMENT COMMENT '감정 ID',
    `emotion_name` VARCHAR(20)     NOT NULL COMMENT '감정 이름',
    `score`        INT             NOT NULL COMMENT '감정 점수 (1-5)',
    PRIMARY KEY (`emotion_id`)
) COMMENT = '감정 점수';

-- 4. 꿈 기록 테이블 (dreams)
CREATE TABLE `dreams` (
    `dream_id`     BIGINT          NOT NULL AUTO_INCREMENT COMMENT '꿈 ID',
    `user_id`      BIGINT          NOT NULL COMMENT '사용자 ID',
    `emotion_id`   TINYINT         NOT NULL COMMENT '감정 ID',
    `dream_date`   DATE            NOT NULL COMMENT '꿈 꾼 날짜',
    `title`        TEXT            NOT NULL COMMENT '꿈 제목',
    `content`      TEXT            NOT NULL COMMENT '꿈 내용',
    `created_date` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `deleted_date` DATETIME        NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (`dream_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`emotion_id`) REFERENCES `emotion_scores` (`emotion_id`)
) COMMENT = '꿈 기록';

-- 5. AI 분석 결과 테이블 (dream_results)
CREATE TABLE `dream_results` (
    `id`                    BIGINT         NOT NULL AUTO_INCREMENT COMMENT '결과 ID',
    `dream_id`              BIGINT         NOT NULL COMMENT '꿈 ID',
    `dream_interpretation`  TEXT           NOT NULL COMMENT 'AI 꿈 해몽 결과',
    `today_fortune_summary` TEXT           NOT NULL COMMENT '오늘의 운세 종합',
    `lucky_color_name`      VARCHAR(50)    NOT NULL COMMENT '행운의 색 이름',
    `lucky_color_number`    INT            NOT NULL COMMENT '행운의 색 번호 (1-7)',
    `lucky_color_reason`    TEXT           NOT NULL COMMENT '행운의 색 추천 이유',
    `lucky_item_name`       VARCHAR(100)   NOT NULL COMMENT '행운의 아이템 이름',
    `lucky_item_reason`     TEXT           NOT NULL COMMENT '행운의 아이템 추천 이유',
    `image_url`             VARCHAR(255)   NULL COMMENT '꿈 이미지 URL',
    `created_date`          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `deleted_date`          DATETIME       NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dream_results_dream_id` (`dream_id`),
    FOREIGN KEY (`dream_id`) REFERENCES `dreams` (`dream_id`) ON DELETE CASCADE
) COMMENT = 'AI 분석 결과';

-- 6. 월별 꿈 분석 테이블 (dream_monthly_analysis)
CREATE TABLE `dream_monthly_analysis` (
    `analysis_id`       BIGINT         NOT NULL AUTO_INCREMENT COMMENT '분석 ID',
    `user_id`           BIGINT         NOT NULL COMMENT '사용자 ID',
    `year`              INT            NOT NULL COMMENT '연도',
    `month`             INT            NOT NULL COMMENT '월',
    `dream_count`       INT            NOT NULL COMMENT '해당 월 전체 꿈 개수',
    `avg_emotion_score` DECIMAL(5,2)   NOT NULL COMMENT '월별 감정 평균 점수',
    `monthly_report`    LONGTEXT       NULL COMMENT 'FastAPI에서 생성한 월간 리포트 (마크다운)',
    `created_date`      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_date`      DATETIME       NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (`analysis_id`),
    UNIQUE KEY `uk_monthly_analysis` (`user_id`, `year`, `month`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) COMMENT = '월별 꿈 분석';

-- 7. 월별 메모 테이블 (dream_monthly_memo)
CREATE TABLE `dream_monthly_memo` (
    `memo_id`      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '메모 ID',
    `analysis_id`  BIGINT          NOT NULL COMMENT '분석 ID',
    `memo_content` TEXT            NOT NULL COMMENT '메모 내용',
    `created_date` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_date` DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    `deleted_date` DATETIME        NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (`memo_id`),
    UNIQUE KEY `uk_monthly_memo_analysis_id` (`analysis_id`),
    FOREIGN KEY (`analysis_id`) REFERENCES `dream_monthly_analysis` (`analysis_id`) ON DELETE CASCADE
) COMMENT = '월별 메모';

-- 8. 초기 데이터 삽입 - 감정 점수
INSERT INTO `emotion_scores` (`emotion_id`, `emotion_name`, `score`) VALUES
(1, '기쁨', 5),
(2, '만족', 4),
(3, '평범', 3),
(4, '불안', 2),
(5, '슬픔', 1);
