"""教师原创录入适配器

对应 docs/05 §2.3 自建渠道（主力，推荐 ≥ 60% 题量），是 V1.0 100 题目标的主力流程。
教师以 YAML 文件提交题目（YAML 支持注释、对教师友好），本适配器负责
加载、补默认值，产出原始字典列表；JSON 导出由 exporter 负责。
"""

from __future__ import annotations

from pathlib import Path
from typing import Any

import yaml

from .base import BaseSource

# 教师原创默认协议（docs/05 §1.3：题库数据统一 CC-BY-SA 4.0）
DEFAULT_TEACHER_LICENSE = "CC-BY-SA-4.0"
# 教师原创默认来源类型（docs/05 §4.1 source_type=4 原创）
DEFAULT_TEACHER_SOURCE_TYPE = "ORIGINAL"


class TeacherUploadSource(BaseSource):
    """教师原创录入适配器。

    支持两种 YAML 文件格式：
    1. 顶层列表：每项为一题字典；
    2. 顶层对象含 questions 字段（列表），与 QuestionBatch 结构一致。
    """

    name = "teacher-original"

    def __init__(self, input_path: str | Path) -> None:
        """初始化教师录入适配器。

        :param input_path: 教师提交的 YAML 文件或目录路径
        """
        self.input_path = Path(input_path)

    def fetch_raw(self) -> list[dict[str, Any]]:
        """加载 YAML 并返回原始题目字典列表。

        :return: 原始题目字典列表（已经过 normalize 补默认值）
        :raises ValueError: 输入路径无 YAML 文件、解析失败或顶层结构非法
        """
        files = self._list_yaml_files()
        if not files:
            raise ValueError(f"未在 {self.input_path} 找到任何 YAML 文件")

        questions: list[dict[str, Any]] = []
        for file_path in files:
            raw = self._load_yaml(file_path)
            items = self._extract_items(raw, file_path)
            for item in items:
                questions.append(self.normalize(item))
        return questions

    def normalize(self, raw: dict[str, Any]) -> dict[str, Any]:
        """补全教师原创默认字段。

        - subject 缺省补 HISTORY；
        - sourceType 缺省补 ORIGINAL；
        - license 缺省补 CC-BY-SA-4.0；
        - primaryKnowledgeId 缺省取 knowledgeIds 首项；
        - knowledgeIds 必须显式填写（教师必须标考点，避免无主考点题目进库）。

        :param raw: 教师提交的原始字典
        :return: 补默认值后的字典
        :raises ValueError: 缺少 knowledgeIds
        """
        normalized = dict(raw)
        normalized.setdefault("subject", "HISTORY")
        normalized.setdefault("sourceType", DEFAULT_TEACHER_SOURCE_TYPE)
        normalized.setdefault("license", DEFAULT_TEACHER_LICENSE)
        # 考点必须显式填写，不允许默认值（docs/05 §8.1：至少 1 个有效 knowledgeId）
        if not normalized.get("knowledgeIds"):
            raise ValueError(f"教师原创题必须显式填写 knowledgeIds: {normalized.get('externalId')}")
        normalized.setdefault("primaryKnowledgeId", normalized["knowledgeIds"][0])
        return normalized

    def _list_yaml_files(self) -> list[Path]:
        """枚举输入路径下的 YAML 文件。

        :return: YAML 文件路径列表；单文件输入返回该文件，目录输入按文件名升序返回
        """
        path = self.input_path
        if path.is_file():
            return [path]
        if path.is_dir():
            return sorted(path.glob("*.y*ml"))
        return []

    def _load_yaml(self, file_path: Path) -> Any:
        """读取 YAML 文件并解析为 Python 对象。

        :param file_path: YAML 文件路径
        :return: 解析后的对象（列表或字典）
        :raises ValueError: YAML 语法解析失败
        """
        try:
            with file_path.open("r", encoding="utf-8") as f:
                return yaml.safe_load(f)
        except yaml.YAMLError as e:
            raise ValueError(f"YAML 解析失败 {file_path}: {e}") from e

    def _extract_items(self, raw: Any, file_path: Path) -> list[dict[str, Any]]:
        """从 YAML 顶层对象提取题目列表。

        :param raw: 解析后的 YAML 对象
        :param file_path: 文件路径，仅用于报错信息
        :return: 题目字典列表
        :raises ValueError: 顶层结构既不是列表也不含 questions 列表
        """
        if isinstance(raw, list):
            return raw
        if isinstance(raw, dict) and isinstance(raw.get("questions"), list):
            return raw["questions"]
        raise ValueError(f"{file_path} 顶层必须为列表或含 questions 列表的对象")
