"""validator.py 单元测试

覆盖 docs/05 §5.4 协议校验规则与 §8.1 自动校验规则的核心场景：
题型、来源禁用、许可证白名单、考点白名单、选项/答案一致性、来源追溯。
"""

from __future__ import annotations

import pytest
from pydantic import ValidationError

from scraper.validator import validate_question


def _valid_question() -> dict:
    """构造一份合法选择题字典，作为各测试用例的基线。"""
    return {
        "externalId": "HIS-2026-001",
        "subject": "HISTORY",
        "type": "SINGLE_CHOICE",
        "stem": "秦始皇统一六国的年份是？",
        "options": [
            {"key": "A", "content": "公元前221年"},
            {"key": "B", "content": "公元前206年"},
            {"key": "C", "content": "公元220年"},
            {"key": "D", "content": "公元618年"},
        ],
        "answer": "A",
        "analysis": "公元前221年，秦王嬴政统一六国。",
        "knowledgeIds": ["K-CHN-ANC-QIN-HAN"],
        "primaryKnowledgeId": "K-CHN-ANC-QIN-HAN",
        "difficulty": 2,
        "sourceType": "ORIGINAL",
        "author": "张老师",
        "license": "CC-BY-SA-4.0",
    }


def test_valid_single_choice_passes() -> None:
    """合法选择题应通过校验。"""
    q = validate_question(_valid_question())
    assert q.externalId == "HIS-2026-001"
    assert q.type == "SINGLE_CHOICE"


def test_valid_true_false_passes() -> None:
    """合法判断题应通过校验。"""
    data = _valid_question()
    data["type"] = "TRUE_FALSE"
    data["options"] = None
    data["answer"] = "T"
    q = validate_question(data)
    assert q.type == "TRUE_FALSE"


def test_past_exam_raw_forbidden() -> None:
    """sourceType=PAST_EXAM_RAW（真题原文）应被禁止。"""
    data = _valid_question()
    data["sourceType"] = "PAST_EXAM_RAW"
    with pytest.raises(ValidationError):
        validate_question(data)


def test_invalid_license_rejected() -> None:
    """非白名单许可证应被拒绝。"""
    data = _valid_question()
    data["license"] = "ALL_RIGHTS_RESERVED"
    with pytest.raises(ValidationError):
        validate_question(data)


def test_unknown_knowledge_id_rejected() -> None:
    """未知考点编码应被拒绝。"""
    data = _valid_question()
    data["knowledgeIds"] = ["K-UNKNOWN-XXX"]
    data["primaryKnowledgeId"] = "K-UNKNOWN-XXX"
    with pytest.raises(ValidationError):
        validate_question(data)


def test_primary_must_be_in_knowledge_ids() -> None:
    """主考点必须在 knowledgeIds 中。"""
    data = _valid_question()
    data["primaryKnowledgeId"] = "K-CHN-MOD-REPUBLIC"
    with pytest.raises(ValidationError):
        validate_question(data)


def test_answer_not_in_options_rejected() -> None:
    """选择题答案不在选项 key 中应被拒绝。"""
    data = _valid_question()
    data["answer"] = "E"
    with pytest.raises(ValidationError):
        validate_question(data)


def test_duplicate_option_keys_rejected() -> None:
    """选项 key 重复应被拒绝。"""
    data = _valid_question()
    data["options"].append({"key": "A", "content": "重复选项"})
    with pytest.raises(ValidationError):
        validate_question(data)


def test_too_few_options_rejected() -> None:
    """选择题少于 2 个选项应被拒绝。"""
    data = _valid_question()
    data["options"] = data["options"][:1]
    with pytest.raises(ValidationError):
        validate_question(data)


def test_true_false_with_options_rejected() -> None:
    """判断题携带 options 应被拒绝。"""
    data = _valid_question()
    data["type"] = "TRUE_FALSE"
    data["answer"] = "T"
    with pytest.raises(ValidationError):
        validate_question(data)


def test_true_false_invalid_answer_rejected() -> None:
    """判断题答案非 T/F 应被拒绝。"""
    data = _valid_question()
    data["type"] = "TRUE_FALSE"
    data["options"] = None
    data["answer"] = "对"
    with pytest.raises(ValidationError):
        validate_question(data)


def test_no_author_and_no_url_rejected() -> None:
    """author 与 sourceUrl 均为空应被拒绝（来源追溯铁律）。"""
    data = _valid_question()
    data["author"] = None
    data["sourceUrl"] = None
    with pytest.raises(ValidationError):
        validate_question(data)


def test_url_only_without_author_passes() -> None:
    """有 sourceUrl 无 author 也应通过（来源追溯满足其一即可）。"""
    data = _valid_question()
    data["author"] = None
    data["sourceUrl"] = "https://example.com/source"
    q = validate_question(data)
    assert q.sourceUrl == "https://example.com/source"
