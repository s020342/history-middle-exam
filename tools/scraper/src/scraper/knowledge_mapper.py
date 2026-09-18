"""考点关键词映射

对应 docs/05 §6.2 映射规则：关键词规则 + 人工辅助（低置信度队列）。
教师录入必须显式标 knowledgeIds，本模块仅做一致性二次校验：
关键词推断与教师标注的主考点不一致时输出警告（转人工审校），不自动覆盖、不阻断。
"""

from __future__ import annotations

from dataclasses import dataclass, field

from .constants import KNOWLEDGE_CODES

# 关键词 → 考点编码映射表，docs/05 §6.1 考点骨架
# V1.0 内置高频考点关键词，可按需扩展；docs/05 §7 高频考点题量加权 1.5 倍
KEYWORD_TO_KNOWLEDGE: dict[str, str] = {
    # 先秦
    "甲骨文": "K-CHN-ANC-PRE-QIN",
    "百家争鸣": "K-CHN-ANC-PRE-QIN",
    # 秦汉
    "秦始皇": "K-CHN-ANC-QIN-HAN",
    "汉武帝": "K-CHN-ANC-QIN-HAN",
    "丝绸之路": "K-CHN-ANC-QIN-HAN",
    "罢黜百家": "K-CHN-ANC-QIN-HAN",
    # 隋唐
    "科举": "K-CHN-ANC-SUI-TANG",
    "唐太宗": "K-CHN-ANC-SUI-TANG",
    "贞观之治": "K-CHN-ANC-SUI-TANG",
    "开元盛世": "K-CHN-ANC-SUI-TANG",
    # 宋元
    "岳飞": "K-CHN-ANC-SONG-YUAN",
    "成吉思汗": "K-CHN-ANC-SONG-YUAN",
    "澶渊之盟": "K-CHN-ANC-SONG-YUAN",
    # 明清
    "郑和": "K-CHN-ANC-MING-QING",
    "朱元璋": "K-CHN-ANC-MING-QING",
    "军机处": "K-CHN-ANC-MING-QING",
    # 近代
    "鸦片战争": "K-CHN-MOD-OPIUM-WAR",
    "南京条约": "K-CHN-MOD-OPIUM-WAR",
    "戊戌变法": "K-CHN-MOD-LATE-QING",
    "辛亥革命": "K-CHN-MOD-REPUBLIC",
    "五四运动": "K-CHN-MOD-REPUBLIC",
    "抗日战争": "K-CHN-MOD-ANTI-JAPAN",
    "七七事变": "K-CHN-MOD-ANTI-JAPAN",
    "解放战争": "K-CHN-MOD-CIVIL-WAR",
}


@dataclass
class MappingHint:
    """考点映射提示（低置信度队列项，交教研员确认）。"""

    question_external_id: str
    teacher_primary: str              # 教师标注的主考点
    keyword_hit: list[str] = field(default_factory=list)  # 关键词命中的考点


def infer_by_keywords(stem: str, analysis: str | None) -> list[str]:
    """根据题干与解析关键词推断考点编码列表（去重，按映射表顺序）。

    :param stem: 题干文本
    :param analysis: 解析文本，可空
    :return: 命中的考点编码列表
    """
    text = f"{stem} {analysis or ''}"
    hits: list[str] = []
    for keyword, code in KEYWORD_TO_KNOWLEDGE.items():
        if keyword in text and code not in hits:
            hits.append(code)
    return hits


def check_consistency(questions: list[dict]) -> list[MappingHint]:
    """批量校验教师标注与关键词推断的一致性，仅返回不一致项供人工抽审。

    判定规则：主考点合法且（无关键词命中，或主考点在命中列表中）视为一致。

    :param questions: 题目字典列表，每项需含 externalId/stem/primaryKnowledgeId
    :return: 不一致映射提示列表
    """
    hints: list[MappingHint] = []
    for q in questions:
        primary = q.get("primaryKnowledgeId", "")
        keyword_hit = infer_by_keywords(q.get("stem", ""), q.get("analysis"))
        is_consistent = primary in KNOWLEDGE_CODES and (
            not keyword_hit or primary in keyword_hit
        )
        if not is_consistent:
            hints.append(
                MappingHint(
                    question_external_id=q.get("externalId", ""),
                    teacher_primary=primary,
                    keyword_hit=keyword_hit,
                )
            )
    return hints
