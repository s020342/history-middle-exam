"""抓取侧常量与枚举

字段取值严格对齐：
- docs/03-数据库设计文档.md question 表（type/source_type/status/difficulty）
- docs/05-数据抓取方案文档.md §3.1 JSON Schema、§4.1 source_type 扩展枚举、§5.4 协议校验规则
"""

from enum import IntEnum


class QuestionType(IntEnum):
    """题型枚举，对应 question.type 字段（1 选择 2 判断）。"""

    SINGLE_CHOICE = 1  # 选择题
    TRUE_FALSE = 2     # 判断题


class SourceType(IntEnum):
    """来源类型枚举，对应 question.source_type 字段（docs/05 §4.1）。

    真题原文(1) 版权归属命题机构，明确禁用，不参与抓取流程。
    """

    PAST_EXAM_RAW = 1       # 真题原文（禁用，不抓取）
    PAST_EXAM_ADAPTED = 2   # 真题改编
    SIMULATION = 3          # 模拟
    ORIGINAL = 4            # 原创
    OPEN_SOURCE = 5         # 开源搬运


class QuestionStatus(IntEnum):
    """题目状态枚举，对应 question.status 字段（0 上架 1 下架 2 审核中）。"""

    ONLINE = 0      # 上架
    OFFLINE = 1     # 下架
    REVIEWING = 2   # 审核中


class Difficulty(IntEnum):
    """难度枚举，对应 question.difficulty 字段（1 易 2 中 3 难）。"""

    EASY = 1
    MEDIUM = 2
    HARD = 3


# 允许的许可证白名单，docs/05 §5.4 协议校验规则
ALLOWED_LICENSES: frozenset[str] = frozenset({
    "CC0",
    "CC-BY-4.0",
    "CC-BY-SA-4.0",
    "MIT",
    "Apache-2.0",
    "ORIGINAL",
})

# 禁用的来源类型（仅 PAST_EXAM_RAW），docs/05 §5.4 协议校验规则
FORBIDDEN_SOURCE_TYPES: frozenset[str] = frozenset({"PAST_EXAM_RAW"})

# JSON 字符串到数据库枚举的映射表，供后端 /api/admin/question/import 使用
SOURCE_TYPE_JSON_TO_DB: dict[str, int] = {
    "PAST_EXAM_RAW": int(SourceType.PAST_EXAM_RAW),
    "PAST_EXAM_ADAPTED": int(SourceType.PAST_EXAM_ADAPTED),
    "SIMULATION": int(SourceType.SIMULATION),
    "ORIGINAL": int(SourceType.ORIGINAL),
    "OPEN_SOURCE": int(SourceType.OPEN_SOURCE),
}

QUESTION_TYPE_JSON_TO_DB: dict[str, int] = {
    "SINGLE_CHOICE": int(QuestionType.SINGLE_CHOICE),
    "TRUE_FALSE": int(QuestionType.TRUE_FALSE),
}

# 历史主考点编码白名单，docs/05 §6.1 考点骨架
# knowledge_point 表 V1.1 才落库，V1.0 阶段考点信息仅保留在题库 JSON 中
KNOWLEDGE_CODES: tuple[str, ...] = (
    "K-CHN-ANC-PRE-QIN",
    "K-CHN-ANC-QIN-HAN",
    "K-CHN-ANC-WEI-JIN",
    "K-CHN-ANC-SUI-TANG",
    "K-CHN-ANC-SONG-YUAN",
    "K-CHN-ANC-MING-QING",
    "K-CHN-MOD-OPIUM-WAR",
    "K-CHN-MOD-LATE-QING",
    "K-CHN-MOD-REPUBLIC",
    "K-CHN-MOD-ANTI-JAPAN",
    "K-CHN-MOD-CIVIL-WAR",
    "K-CHN-CON",
    "K-WORLD-ANCIENT",
    "K-WORLD-MEDIEVAL",
    "K-WORLD-MODERN",
    "K-WORLD-CONTEMPORARY",
    "K-METHOD",
)

# 选择题合法选项 key 集合
SINGLE_CHOICE_KEYS: frozenset[str] = frozenset({"A", "B", "C", "D"})
# 判断题合法答案集合
TRUE_FALSE_ANSWERS: frozenset[str] = frozenset({"T", "F"})

# MinHash 去重阈值：相似度 > 该值视为重复，docs/05 §4.2
DEDUP_SIMILARITY_THRESHOLD: float = 0.92

# 人工抽审比例：每 100 题抽 10 题，docs/05 §8.2
HUMAN_REVIEW_RATIO: float = 0.10
