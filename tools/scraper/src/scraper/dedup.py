"""题干 MinHash 去重

对应 docs/05 §4.2 去重策略：题干相似度 > 0.92（MinHash）转人工审核，本实现直接丢弃重复项。
采用字符级 n-gram 构建 MinHash 指纹，避免引入中文分词依赖。
"""

from __future__ import annotations

from dataclasses import dataclass, field

from datasketch import MinHash, MinHashLSH

from .constants import DEDUP_SIMILARITY_THRESHOLD

# MinHash 排列数，固定 128 在精度与性能间取得平衡
_NUM_PERM = 128
# 字符级 n-gram 长度：中文题干 20~50 字，4-gram 可捕捉局部短语特征
_NGRAM_SIZE = 4


@dataclass
class DedupResult:
    """去重结果。

    unique: 去重后保留的题目索引列表（按原顺序）
    duplicates: 被丢弃题目索引及其撞到的保留索引列表，便于人工复核
    """

    unique: list[int] = field(default_factory=list)
    duplicates: list[tuple[int, int]] = field(default_factory=list)


def _build_minhash(text: str) -> MinHash:
    """为题干文本构建 MinHash 指纹。

    先去除空白字符，再按字符级 4-gram 更新指纹，对中文题干稳健且无分词依赖。

    :param text: 题干文本
    :return: MinHash 对象
    """
    mh = MinHash(num_perm=_NUM_PERM)
    cleaned = text.replace("\n", "").replace(" ", "")
    for i in range(len(cleaned) - _NGRAM_SIZE + 1):
        mh.update(cleaned[i : i + _NGRAM_SIZE].encode("utf-8"))
    return mh


def dedup_by_stem(stems: list[str], threshold: float = DEDUP_SIMILARITY_THRESHOLD) -> DedupResult:
    """按题干相似度去重，保留首次出现的题目。

    :param stems: 题干文本列表
    :param threshold: 相似度阈值，默认 0.92（docs/05 §4.2）
    :return: 去重结果
    """
    lsh = MinHashLSH(threshold=threshold, num_perm=_NUM_PERM)
    result = DedupResult()

    for idx, stem in enumerate(stems):
        mh = _build_minhash(stem)
        # LSH 查询是否已存在相似题干：命中即视为重复，未命中则登记保留
        hits = lsh.query(mh)
        if hits:
            result.duplicates.append((idx, hits[0]))
        else:
            lsh.insert(idx, mh)
            result.unique.append(idx)

    return result
