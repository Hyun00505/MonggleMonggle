-- 0. 기존 DB 삭제 (선택)
-- DROP DATABASE IF EXISTS `dream_db`;

-- 1. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS `dream_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `dream_db`;

-- 2. 회원 정보 테이블 (users)
CREATE TABLE `users` (
    `user_id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '사용자 ID',
    `login_id`             VARCHAR(255)  NOT NULL COMMENT '로그인 아이디',
    `password`             VARCHAR(255)  NOT NULL COMMENT '비밀번호 (암호화)',
    `name`                 VARCHAR(100)  NOT NULL COMMENT '이름',
    `birth_date`           DATE          NOT NULL COMMENT '생년월일',
    `gender`               CHAR(1)       NOT NULL COMMENT '성별 (M, F)',
    `calendar_type`        VARCHAR(20)   NOT NULL DEFAULT 'solar' COMMENT '달력 유형',
    `coin`                 INT           NOT NULL DEFAULT 5 COMMENT '하루 해몽 가능 횟수',
    `last_coin_reset_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 코인 리셋',
    `role`                 VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT '권한(USER, ADMIN)',
    `created_date`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일',
    `updated_date`         DATETIME      NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    `deleted_date`         DATETIME      NULL COMMENT '삭제일 (Soft Delete)',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_users_login_id` (`login_id`)
) COMMENT='회원 정보';

-- 3. 감정 점수 테이블 (emotion_scores)
CREATE TABLE `emotion_scores` (
    `emotion_id`    TINYINT      NOT NULL AUTO_INCREMENT COMMENT '감정 ID',
    `emotion_name`  VARCHAR(20)  NOT NULL COMMENT '감정 이름',
    `score`         INT          NOT NULL COMMENT '감정 점수 (1~5)',
    PRIMARY KEY (`emotion_id`)
) COMMENT='감정 점수';

-- 4. 꿈 기록 테이블 (dreams)
CREATE TABLE `dreams` (
    `dream_id`      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '꿈 ID',
    `user_id`       BIGINT      NOT NULL COMMENT '사용자 ID',
    `emotion_id`    TINYINT     NOT NULL COMMENT '감정 ID',
    `dream_date`    DATE        NOT NULL COMMENT '꿈 꾼 날짜',
    `title`         TEXT        NOT NULL COMMENT '꿈 제목',
    `content`       TEXT        NOT NULL COMMENT '꿈 내용',
    `created_date`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date`  DATETIME    NULL,
    PRIMARY KEY (`dream_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`emotion_id`) REFERENCES `emotion_scores` (`emotion_id`)
) COMMENT='꿈 기록';

-- 5. AI 분석 결과 테이블 (dream_results)
CREATE TABLE `dream_results` (
    `id`                     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '결과 ID',
    `dream_id`               BIGINT        NOT NULL COMMENT '꿈 ID',
    `dream_interpretation`   TEXT          NOT NULL COMMENT 'AI 해몽',
    `today_fortune_summary`  TEXT          NOT NULL COMMENT '오늘의 운세',
    `lucky_color_name`       VARCHAR(50)   NOT NULL COMMENT '행운의 색',
    `lucky_color_number`     INT           NOT NULL COMMENT '행운의 색 번호',
    `lucky_color_reason`     TEXT          NOT NULL COMMENT '색 추천 이유',
    `lucky_item_name`        VARCHAR(100)  NOT NULL COMMENT '행운 아이템',
    `lucky_item_reason`      TEXT          NOT NULL COMMENT '아이템 추천 이유',
    `image_url`              VARCHAR(255)  NULL COMMENT '이미지 URL',
    `is_liked`               TINYINT       NOT NULL DEFAULT 0 COMMENT '찜 여부',
    `created_date`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date`           DATETIME      NULL ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date`           DATETIME      NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dream_results_dream_id` (`dream_id`),
    FOREIGN KEY (`dream_id`) REFERENCES `dreams` (`dream_id`) ON DELETE CASCADE
) COMMENT='AI 분석 결과';

-- 6. 월별 꿈 분석 테이블
CREATE TABLE `dream_monthly_analysis` (
    `analysis_id`        BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT        NOT NULL,
    `year`               INT           NOT NULL,
    `month`              INT           NOT NULL,
    `dream_count`        INT           NOT NULL,
    `avg_emotion_score`  DECIMAL(5,2)  NOT NULL,
    `monthly_report`     LONGTEXT      NULL,
    `created_date`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date`       DATETIME      NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`analysis_id`),
    UNIQUE KEY `uk_monthly_analysis` (`user_id`, `year`, `month`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) COMMENT='월별 꿈 분석';

-- 7. 월별 메모 테이블
CREATE TABLE `dream_monthly_memo` (
    `memo_id`       BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT      NOT NULL,
    `year`          INT         NOT NULL,
    `month`         INT         NOT NULL,
    `memo_content`  TEXT        NOT NULL,
    `created_date`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date`  DATETIME    NULL ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date`  DATETIME    NULL,
    PRIMARY KEY (`memo_id`),
    INDEX `idx_memo_user_year_month` (`user_id`, `year`, `month`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) COMMENT='월별 메모';

-- 8. 공지사항
CREATE TABLE `notice` (
    `notice_id`     BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT      NOT NULL COMMENT '관리자 ID',
    `title`         TEXT        NOT NULL,
    `content`       TEXT        NOT NULL,
    `view_count`    INT         NOT NULL DEFAULT 0,
    `created_date`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date`  DATETIME    NULL ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date`  DATETIME    NULL,
    PRIMARY KEY (`notice_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) COMMENT='공지사항';

-- 9. 공지사항 댓글
CREATE TABLE `notice_comment` (
    `comment_id`    BIGINT      NOT NULL AUTO_INCREMENT,
    `notice_id`     BIGINT      NOT NULL,
    `user_id`       BIGINT      NOT NULL,
    `content`       TEXT        NOT NULL,
    `created_date`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_date`  DATETIME    NULL,
    PRIMARY KEY (`comment_id`),
    FOREIGN KEY (`notice_id`) REFERENCES `notice` (`notice_id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) COMMENT='공지사항 댓글';

-- 10. 공지사항 좋아요
CREATE TABLE `notice_likes` (
    `like_id`       BIGINT      NOT NULL AUTO_INCREMENT,
    `notice_id`     BIGINT      NOT NULL,
    `user_id`       BIGINT      NOT NULL,
    `created_date`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`like_id`),
    UNIQUE KEY `uk_notice_like` (`notice_id`, `user_id`),
    FOREIGN KEY (`notice_id`) REFERENCES `notice` (`notice_id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) COMMENT='공지사항 좋아요';

-- 11. 감정 점수 초기 데이터
INSERT INTO `emotion_scores` (`emotion_id`, `emotion_name`, `score`) VALUES
(1, '매우 나쁨', 1),
(2, '나쁨', 2),
(3, '보통', 3),
(4, '좋음', 4),
(5, '매우 좋음', 5);
