"""来源适配器抽象基类

对应 docs/05 §5.2 sources/ 目录与 §5.3 流程步骤 1-2。
每个来源适配器负责：拉取候选 → 解析为原始题目字典列表 → 由 validator 完成协议与 Schema 校验。
"""

from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any


class BaseSource(ABC):
    """来源适配器抽象基类。

    所有来源（教师原创、开源搬运、AI 命题辅助）需实现 fetch_raw 方法。
    """

    #: 来源标识，用于导出文件名与 manifest.source
    name: str = "base"

    @abstractmethod
    def fetch_raw(self) -> list[dict[str, Any]]:
        """拉取并解析原始题目字典列表。

        :return: 每个元素为符合 docs/05 §3.1 单题结构的字典（尚未校验）
        :raises RuntimeError: 拉取或解析失败
        """
        raise NotImplementedError

    def normalize(self, raw: dict[str, Any]) -> dict[str, Any]:
        """对原始字典做来源侧标准化（默认透传）。

        子类可重写以补充默认字段，如教师原创默认 sourceType=ORIGINAL、license=CC-BY-SA-4.0。

        :param raw: 原始字典
        :return: 标准化后的字典
        """
        return raw
