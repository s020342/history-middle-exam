-- ============================================================================
-- 历史中考训练小程序 V1.0 MVP 数据库初始化脚本
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4 / utf8mb4_0900_ai_ci
-- 说明：仅包含 V1.0 MVP 必需的表（学生/题库/训练/错题/订阅）
--       其余表（knowledge_point/mastery/review_queue/quiz_*）按迭代节奏在 V1.1/V1.2 落库
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `history_exam`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `history_exam`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. 学生表 student
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
    `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `openid`          VARCHAR(64)      NOT NULL                COMMENT '微信 openid',
    `unionid`         VARCHAR(64)               DEFAULT NULL    COMMENT '微信 unionid，可空',
    `nickname`        VARCHAR(64)               DEFAULT NULL    COMMENT '昵称',
    `avatar_url`      VARCHAR(255)              DEFAULT NULL    COMMENT '头像 URL',
    `grade`           TINYINT          NOT NULL DEFAULT 9       COMMENT '年级，默认 9（初三）',
    `phone`           VARCHAR(20)               DEFAULT NULL    COMMENT '家长绑定手机号',
    `status`          TINYINT          NOT NULL DEFAULT 0       COMMENT '0 正常 1 禁用',
    `subscribed`      TINYINT          NOT NULL DEFAULT 0       COMMENT '0 否 1 是（冗余自订阅记录）',
    `subscribed_until` DATETIME                  DEFAULT NULL    COMMENT '订阅到期时间',
    `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT          NOT NULL DEFAULT 0       COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生表';

-- ----------------------------------------------------------------------------
-- 2. 错因标签表 error_cause
--    wrong_record.error_cause_id 引用本表，故 V1.0 一并落库
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `error_cause`;
CREATE TABLE `error_cause` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `code`       VARCHAR(32)     NOT NULL                COMMENT '错因编码',
    `name`       VARCHAR(32)     NOT NULL                COMMENT '错因名称',
    `sort`       INT             NOT NULL DEFAULT 0     COMMENT '排序',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    TINYINT         NOT NULL DEFAULT 0     COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错因标签表';

-- V1.0 内置错因（KNOWLEDGE_MISSING 知识缺失 / CONCEPT_CONFUSED 概念混淆 / QUESTION_TRAP 题目陷阱）
INSERT INTO `error_cause` (`code`, `name`, `sort`) VALUES
    ('KNOWLEDGE_MISSING', '知识缺失', 1),
    ('CONCEPT_CONFUSED',  '概念混淆', 2),
    ('QUESTION_TRAP',     '题目陷阱', 3);

-- ----------------------------------------------------------------------------
-- 3. 题目表 question
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
    `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `type`          TINYINT          NOT NULL                COMMENT '1 选择 2 判断',
    `stem`          TEXT             NOT NULL                COMMENT '题干（可含图）',
    `difficulty`    TINYINT          NOT NULL DEFAULT 2      COMMENT '1 易 2 中 3 难',
    `source_type`   TINYINT          NOT NULL                COMMENT '1 真题原文(禁用) 2 真题改编 3 模拟 4 原创 5 开源搬运',
    `source_year`   SMALLINT                  DEFAULT NULL    COMMENT '真题年份，可空',
    `source_desc`   VARCHAR(128)              DEFAULT NULL    COMMENT '来源描述',
    `answer`        VARCHAR(16)      NOT NULL                COMMENT '正确答案（选项 key 或 T/F）',
    `analysis`      TEXT                      DEFAULT NULL    COMMENT '解析',
    `status`        TINYINT          NOT NULL DEFAULT 0       COMMENT '0 上架 1 下架 2 审核中',
    `review_count`  INT              NOT NULL DEFAULT 0       COMMENT '作答次数（冗余）',
    `correct_rate`  DECIMAL(5,2)              DEFAULT 0.00    COMMENT '正确率（0-100，冗余）',
    `external_id`   VARCHAR(64)               DEFAULT NULL    COMMENT '抓取侧唯一 ID',
    `license`       VARCHAR(32)               DEFAULT NULL    COMMENT 'CC-BY-SA-4.0 / MIT / ORIGINAL',
    `author`        VARCHAR(64)               DEFAULT NULL    COMMENT '原作者/投稿人',
    `source_url`    VARCHAR(255)              DEFAULT NULL    COMMENT '来源链接',
    `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT          NOT NULL DEFAULT 0       COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_external_id` (`external_id`),
    KEY `idx_type_status` (`type`, `status`),
    KEY `idx_source` (`source_type`),
    KEY `idx_difficulty` (`difficulty`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目表';

-- ----------------------------------------------------------------------------
-- 4. 选项表 question_option
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `question_option`;
CREATE TABLE `question_option` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `question_id` BIGINT UNSIGNED NOT NULL                COMMENT '题目 ID',
    `option_key`  VARCHAR(4)       NOT NULL                COMMENT 'A/B/C/D 或 T/F',
    `content`     VARCHAR(255)     NOT NULL                COMMENT '选项内容',
    `sort`        INT              NOT NULL DEFAULT 0     COMMENT '排序',
    `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT          NOT NULL DEFAULT 0     COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    KEY `idx_question` (`question_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '选项表';

-- ----------------------------------------------------------------------------
-- 5. 训练会话表 train_session
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `train_session`;
CREATE TABLE `train_session` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `student_id`  BIGINT UNSIGNED NOT NULL                COMMENT '学生 ID',
    `mode`        TINYINT          NOT NULL                COMMENT '1 自由 2 错题 3 复习',
    `total`       INT              NOT NULL DEFAULT 0     COMMENT '题数',
    `answered`    INT              NOT NULL DEFAULT 0     COMMENT '已答',
    `correct`     INT              NOT NULL DEFAULT 0     COMMENT '答对',
    `started_at`  DATETIME                  DEFAULT NULL    COMMENT '开始时间',
    `finished_at` DATETIME                  DEFAULT NULL    COMMENT '结束时间',
    `status`      TINYINT          NOT NULL DEFAULT 0     COMMENT '0 进行 1 完成 2 中断',
    `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT          NOT NULL DEFAULT 0     COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    KEY `idx_student_status` (`student_id`, `status`),
    KEY `idx_started` (`started_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '训练会话表';

-- ----------------------------------------------------------------------------
-- 6. 训练答题记录表 train_record
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `train_record`;
CREATE TABLE `train_record` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `session_id`     BIGINT UNSIGNED NOT NULL                COMMENT '会话 ID',
    `student_id`     BIGINT UNSIGNED NOT NULL                COMMENT '学生 ID',
    `question_id`    BIGINT UNSIGNED NOT NULL                COMMENT '题目 ID',
    `user_answer`    VARCHAR(16)      NOT NULL                COMMENT '学生答案',
    `is_correct`     TINYINT          NOT NULL                COMMENT '0 错 1 对',
    `duration_ms`    INT              NOT NULL DEFAULT 0     COMMENT '答题用时（毫秒）',
    `error_cause_id` BIGINT UNSIGNED          DEFAULT NULL    COMMENT '错因 ID',
    `answered_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
    `created_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT          NOT NULL DEFAULT 0     COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    KEY `idx_session` (`session_id`),
    KEY `idx_student_question` (`student_id`, `question_id`),
    KEY `idx_answered` (`answered_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '训练答题记录表';

-- ----------------------------------------------------------------------------
-- 7. 错题记录表 wrong_record
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `wrong_record`;
CREATE TABLE `wrong_record` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `student_id`     BIGINT UNSIGNED NOT NULL                COMMENT '学生 ID',
    `question_id`    BIGINT UNSIGNED NOT NULL                COMMENT '题目 ID',
    `error_cause_id` BIGINT UNSIGNED          DEFAULT NULL    COMMENT '错因 ID',
    `error_count`    INT              NOT NULL DEFAULT 1     COMMENT '累计错次',
    `last_wrong_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近答错时间',
    `resolved`       TINYINT          NOT NULL DEFAULT 0     COMMENT '0 未解决 1 已解决',
    `resolved_at`    DATETIME                  DEFAULT NULL    COMMENT '解决时间',
    `created_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT          NOT NULL DEFAULT 0     COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_question` (`student_id`, `question_id`),
    KEY `idx_student_unresolved` (`student_id`, `resolved`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错题记录表';

-- ----------------------------------------------------------------------------
-- 8. 订阅方案表 subscription_plan
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `subscription_plan`;
CREATE TABLE `subscription_plan` (
    `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `code`             VARCHAR(32)      NOT NULL                COMMENT '方案编码 MONTHLY',
    `name`             VARCHAR(32)      NOT NULL                COMMENT '方案名称',
    `price_fen`        INT              NOT NULL                COMMENT '价格（分）',
    `duration_days`    INT              NOT NULL                COMMENT '订阅时长（天）',
    `free_daily_limit` INT              NOT NULL DEFAULT 10     COMMENT '免费体验每日题量',
    `status`           TINYINT          NOT NULL DEFAULT 0     COMMENT '0 上架 1 下架',
    `created_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT          NOT NULL DEFAULT 0     COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订阅方案表';

-- V1.0 默认月订阅方案：19.9 元/月，免费体验每日 10 题
INSERT INTO `subscription_plan` (`code`, `name`, `price_fen`, `duration_days`, `free_daily_limit`, `status`)
VALUES ('MONTHLY', '月订阅', 1990, 30, 10, 0);

-- ----------------------------------------------------------------------------
-- 9. 订阅订单表 subscription_order
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `subscription_order`;
CREATE TABLE `subscription_order` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_no`       VARCHAR(32)      NOT NULL                COMMENT '商户订单号',
    `student_id`     BIGINT UNSIGNED NOT NULL                COMMENT '学生 ID',
    `plan_id`        BIGINT UNSIGNED NOT NULL                COMMENT '订阅方案 ID',
    `amount_fen`     INT              NOT NULL                COMMENT '支付金额（分）',
    `status`         TINYINT          NOT NULL DEFAULT 0     COMMENT '0 待支付 1 已支付 2 已关闭 3 已退款',
    `prepay_id`      VARCHAR(64)               DEFAULT NULL    COMMENT '微信预支付 ID',
    `transaction_id` VARCHAR(64)               DEFAULT NULL    COMMENT '微信支付单号',
    `paid_at`        DATETIME                  DEFAULT NULL    COMMENT '支付时间',
    `expire_at`      DATETIME         NOT NULL                COMMENT '订单过期时间',
    `created_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT          NOT NULL DEFAULT 0     COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_student_status` (`student_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订阅订单表';

-- ----------------------------------------------------------------------------
-- 10. 订阅权益记录表 subscription_record
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `subscription_record`;
CREATE TABLE `subscription_record` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `student_id` BIGINT UNSIGNED NOT NULL                COMMENT '学生 ID',
    `order_id`   BIGINT UNSIGNED NOT NULL                COMMENT '订单 ID',
    `start_at`   DATETIME         NOT NULL                COMMENT '订阅开始时间',
    `end_at`     DATETIME         NOT NULL                COMMENT '订阅结束时间',
    `status`     TINYINT          NOT NULL DEFAULT 0     COMMENT '0 有效 1 失效',
    `created_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    TINYINT          NOT NULL DEFAULT 0     COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    KEY `idx_student_status` (`student_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订阅权益记录表';

SET FOREIGN_KEY_CHECKS = 1;
