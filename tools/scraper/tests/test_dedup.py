"""dedup.py 单元测试"""

from __future__ import annotations

from scraper.dedup import dedup_by_stem


def test_unique_stems_kept() -> None:
    """题干差异大时应全部保留。"""
    stems = ["秦始皇统一六国", "汉武帝罢黜百家", "唐太宗贞观之治"]
    result = dedup_by_stem(stems)
    assert result.unique == [0, 1, 2]
    assert result.duplicates == []


def test_identical_stem_dropped() -> None:
    """完全重复题干（相似度 1.0 > 0.92）应保留首题、丢弃后续，并记录撞到的保留索引。"""
    stems = [
        "秦始皇统一六国的年份是？",
        "秦始皇统一六国的年份是？",
    ]
    result = dedup_by_stem(stems)
    assert result.unique == [0]
    assert result.duplicates == [(1, 0)]


def test_short_similar_pair_below_default_threshold_kept() -> None:
    """短题干 4-gram 相似度约 0.7，低于默认阈值 0.92，应保留（docs/05 §4.2 语义）。"""
    stems = [
        "秦始皇统一六国的年份是？",
        "秦始皇统一六国的年份是多少？",
    ]
    result = dedup_by_stem(stems)
    assert result.unique == [0, 1]
    assert result.duplicates == []


def test_low_threshold_catches_similar_pair() -> None:
    """降低阈值后相似题干对应被拦截，验证 LSH 阈值语义生效。"""
    stems = [
        "秦始皇统一六国的年份是？",
        "秦始皇统一六国的年份是多少？",
    ]
    result = dedup_by_stem(stems, threshold=0.5)
    assert result.unique == [0]
    assert result.duplicates == [(1, 0)]


def test_empty_input_returns_empty() -> None:
    """空输入应返回空结果。"""
    result = dedup_by_stem([])
    assert result.unique == []
    assert result.duplicates == []
