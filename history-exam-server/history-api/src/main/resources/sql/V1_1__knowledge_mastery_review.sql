-- ============================================================================
-- 历史中考训练小程序 V1.1「智能化基础」数据库增量脚本
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4 / utf8mb4_0900_ai_ci
-- 说明：新增 4 张表（知识点 / 题目-知识点 / 掌握度 / 复习队列）
--       + knowledge_point 与 question_knowledge 的 seed 数据
--       mastery 与 review_queue 由业务运行期写入，不预置数据
-- ============================================================================

USE `history_exam`;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- 1. 知识点表 knowledge_point
--    考点编码唯一；支持父子层级（parent_id=0 为根）；
--    card_content 存 Markdown 卡片内容，列表查询时不取出（VO 控制）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `knowledge_point`;
CREATE TABLE `knowledge_point` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `code`         VARCHAR(32)     NOT NULL                COMMENT '考点编码，唯一（如 K-CHN-ANC-QIN-HAN）',
    `name`         VARCHAR(64)     NOT NULL                COMMENT '考点名称',
    `parent_id`    BIGINT UNSIGNED NOT NULL DEFAULT 0      COMMENT '父考点 ID，0 表示根节点',
    `level`        TINYINT         NOT NULL DEFAULT 1      COMMENT '层级：1 一级 2 二级 3 三级',
    `is_high_freq` TINYINT         NOT NULL DEFAULT 0      COMMENT '是否高频考点：0 否 1 是',
    `card_content` TEXT                     DEFAULT NULL    COMMENT '知识点卡片内容（Markdown）',
    `card_image`   VARCHAR(255)             DEFAULT NULL    COMMENT '卡片配图 URL',
    `sort`         INT             NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
    `status`       TINYINT         NOT NULL DEFAULT 0      COMMENT '0 上架 1 下架',
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT         NOT NULL DEFAULT 0      COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_high_freq` (`is_high_freq`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识点表';

-- V1.1 seed：7 个高频考点（覆盖 V1.0 已抓取 10 题的考点范围）
-- 卡片内容采用简化 Markdown 纯文本，小程序 V1.1 直接 text 展示，towxml 富文本 V1.2 再加
INSERT INTO `knowledge_point` (`code`, `name`, `parent_id`, `level`, `is_high_freq`, `card_content`, `card_image`, `sort`, `status`) VALUES
    ('K-CHN-ANC-QIN-HAN',    '秦汉时期',      0, 1, 1,
     '## 秦汉时期\n\n- 秦朝：中国首个统一的多民族封建国家，公元前 221 年秦始皇嬴政建立\n- 西汉：刘邦建立，定都长安；汉武帝时期推行「罢黜百家，独尊儒术」\n- 东汉：刘秀建立，定都洛阳\n- 关键事件：焚书坑儒、文景之治、汉武帝大一统、丝绸之路',
     NULL, 1, 0),
    ('K-CHN-ANC-SUI-TANG',   '隋唐时期',      0, 1, 1,
     '## 隋唐时期\n\n- 隋朝：581 年杨坚建立，统一全国；开创科举制与三省六部制\n- 唐朝：618 年李渊建立；贞观之治（太宗）、开元盛世（玄宗）\n- 关键事件：大运河、贞观之治、开元盛世、安史之乱、两税法',
     NULL, 2, 0),
    ('K-CHN-ANC-SONG-YUAN',  '宋元时期',      0, 1, 1,
     '## 宋元时期\n\n- 北宋：960 年赵匡胤建立，结束五代十国分裂\n- 南宋：1127 年赵构建立，偏安江南\n- 元朝：1271 年忽必烈定国号为元，1279 年统一全国\n- 关键事件：杯酒释兵权、王安石变法、靖康之变、行省制度',
     NULL, 3, 0),
    ('K-CHN-ANC-MING-QING',  '明清时期',      0, 1, 1,
     '## 明清时期\n\n- 明朝：1368 年朱元璋建立；废除丞相、设内阁\n- 清朝：1644 年入关；军机处、文字狱\n- 关键事件：废丞相、靖难之役、郑和下西洋、军机处、闭关锁国',
     NULL, 4, 0),
    ('K-CHN-MOD-OPIUM-WAR',  '鸦片战争与近代开端', 0, 1, 1,
     '## 鸦片战争与近代开端\n\n- 1840 年第一次鸦片战争爆发，中国近代史开端\n- 1856 年第二次鸦片战争\n- 关键事件：虎门销烟、南京条约（首个不平等条约）、天津条约、北京条约',
     NULL, 5, 0),
    ('K-CHN-MOD-REPUBLIC',   '辛亥革命与民国', 0, 1, 1,
     '## 辛亥革命与民国\n\n- 1894 年兴中会成立；1905 年同盟会成立\n- 1911 年武昌起义，1912 年中华民国成立\n- 关键事件：三民主义、武昌起义、《中华民国临时约法》、二次革命',
     NULL, 6, 0),
    ('K-CHN-MOD-ANTI-JAPAN', '抗日战争',      0, 1, 1,
     '## 抗日战争\n\n- 1931 年九一八事变，局部抗战开始\n- 1937 年七七事变，全面抗战开始\n- 1945 年 8 月日本投降\n- 关键事件：九一八事变、七七事变、台儿庄战役、百团大战、日本投降',
     NULL, 7, 0);

-- ----------------------------------------------------------------------------
-- 2. 题目-知识点关联表 question_knowledge
--    多对多关系；is_primary=1 表示该题的主考点（一题至多一个主考点）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `question_knowledge`;
CREATE TABLE `question_knowledge` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `question_id`  BIGINT UNSIGNED NOT NULL                COMMENT '题目 ID',
    `knowledge_id` BIGINT UNSIGNED NOT NULL                COMMENT '知识点 ID',
    `is_primary`   TINYINT         NOT NULL DEFAULT 0      COMMENT '是否主考点：0 否 1 是',
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT         NOT NULL DEFAULT 0      COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_knowledge` (`question_id`, `knowledge_id`),
    KEY `idx_knowledge` (`knowledge_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目-知识点关联表';

-- V1.1 seed：按 V1.0 已有 question.external_id 反查 id 后批量插关联（is_primary=1）
-- 用 INSERT ... SELECT ... JOIN 形式，避免硬编码 question.id
INSERT INTO `question_knowledge` (`question_id`, `knowledge_id`, `is_primary`)
SELECT q.id, kp.id, 1
FROM `question` q
INNER JOIN `knowledge_point` kp ON (
    (q.external_id LIKE 'TEACHER-QIN-HAN-%'    AND kp.code = 'K-CHN-ANC-QIN-HAN')    OR
    (q.external_id LIKE 'TEACHER-SUI-TANG-%'   AND kp.code = 'K-CHN-ANC-SUI-TANG')   OR
    (q.external_id LIKE 'TEACHER-SONG-YUAN-%'  AND kp.code = 'K-CHN-ANC-SONG-YUAN')  OR
    (q.external_id LIKE 'TEACHER-MING-QING-%'  AND kp.code = 'K-CHN-ANC-MING-QING')  OR
    (q.external_id LIKE 'TEACHER-OPIUM-WAR-%'  AND kp.code = 'K-CHN-MOD-OPIUM-WAR')  OR
    (q.external_id LIKE 'TEACHER-REPUBLIC-%'    AND kp.code = 'K-CHN-MOD-REPUBLIC')   OR
    (q.external_id LIKE 'TEACHER-ANTI-JAPAN-%' AND kp.code = 'K-CHN-MOD-ANTI-JAPAN')
)
WHERE q.deleted = 0 AND kp.deleted = 0;

-- ----------------------------------------------------------------------------
-- 3. 掌握度表 mastery
--    按 (student_id, question_id) 唯一；level 三档；next_review_at 间隔复习日期
--    upsert 靠 uk_student_question 唯一键；不软删（无 resolved 概念，仅覆盖更新）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `mastery`;
CREATE TABLE `mastery` (
    `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `student_id`         BIGINT UNSIGNED NOT NULL                COMMENT '学生 ID',
    `question_id`        BIGINT UNSIGNED NOT NULL                COMMENT '题目 ID',
    `knowledge_id`       BIGINT UNSIGNED          DEFAULT NULL    COMMENT '知识点 ID（按题目的主考点冗余）',
    `level`              TINYINT         NOT NULL DEFAULT 0      COMMENT '掌握度：0 未掌握 1 部分 2 已掌握',
    `consecutive_correct` INT            NOT NULL DEFAULT 0      COMMENT '连续答对次数（达 3 升级）',
    `next_review_at`     DATE                     DEFAULT NULL    COMMENT '下次复习日（按 level 推算）',
    `created_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            TINYINT         NOT NULL DEFAULT 0      COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_question` (`student_id`, `question_id`),
    KEY `idx_student_review` (`student_id`, `next_review_at`),
    KEY `idx_student_knowledge` (`student_id`, `knowledge_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '掌握度表';

-- ----------------------------------------------------------------------------
-- 4. 复习队列表 review_queue
--    按 (student_id, question_id, due_date) 唯一，用于 Job 防重复生成
--    status：0 待复习 1 已复习 2 已过期
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `review_queue`;
CREATE TABLE `review_queue` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `student_id`   BIGINT UNSIGNED NOT NULL                COMMENT '学生 ID',
    `question_id`  BIGINT UNSIGNED NOT NULL                COMMENT '题目 ID',
    `due_date`     DATE            NOT NULL                COMMENT '应复习日',
    `status`       TINYINT         NOT NULL DEFAULT 0      COMMENT '0 待复习 1 已复习 2 已过期',
    `generated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT         NOT NULL DEFAULT 0      COMMENT '0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_question_date` (`student_id`, `question_id`, `due_date`),
    KEY `idx_student_due` (`student_id`, `due_date`),
    KEY `idx_due_status` (`due_date`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '复习队列表';
