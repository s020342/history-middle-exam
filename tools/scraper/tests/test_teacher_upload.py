"""teacher_upload.py 单元测试

覆盖 YAML 两种顶层格式、默认值补全、必填校验、目录加载与异常场景。
"""

from __future__ import annotations

from pathlib import Path

import pytest

from scraper.sources.teacher_upload import TeacherUploadSource


def _write_yaml(tmp_path: Path, content: str, name: str = "sample.yaml") -> Path:
    """在 tmp_path 写入 YAML 文件并返回路径。"""
    file_path = tmp_path / name
    file_path.write_text(content, encoding="utf-8")
    return file_path


def test_list_format_yaml_loaded(tmp_path: Path) -> None:
    """顶层列表格式 YAML 应被正确加载。"""
    yaml_content = """
- externalId: HIS-2026-001
  type: SINGLE_CHOICE
  stem: 秦始皇统一六国的年份是？
  options:
    - {key: A, content: 公元前221年}
    - {key: B, content: 公元前206年}
    - {key: C, content: 公元220年}
    - {key: D, content: 公元618年}
  answer: A
  knowledgeIds: [K-CHN-ANC-QIN-HAN]
  primaryKnowledgeId: K-CHN-ANC-QIN-HAN
  difficulty: 2
  author: 张老师
"""
    file_path = _write_yaml(tmp_path, yaml_content)
    items = TeacherUploadSource(file_path).fetch_raw()
    assert len(items) == 1
    assert items[0]["externalId"] == "HIS-2026-001"


def test_questions_object_format_yaml_loaded(tmp_path: Path) -> None:
    """含 questions 字段的对象格式 YAML 应被正确加载。"""
    yaml_content = """
questions:
  - externalId: HIS-2026-002
    type: TRUE_FALSE
    stem: 丝绸之路是张骞出使西域后开辟的中外交通要道。
    options: null
    answer: T
    knowledgeIds: [K-CHN-ANC-QIN-HAN]
    primaryKnowledgeId: K-CHN-ANC-QIN-HAN
    difficulty: 1
    author: 李老师
"""
    file_path = _write_yaml(tmp_path, yaml_content)
    items = TeacherUploadSource(file_path).fetch_raw()
    assert len(items) == 1
    assert items[0]["type"] == "TRUE_FALSE"


def test_default_fields_normalized(tmp_path: Path) -> None:
    """未填 sourceType/license/subject/primaryKnowledgeId 应自动补默认值。"""
    yaml_content = """
- externalId: HIS-2026-003
  type: SINGLE_CHOICE
  stem: 郑和下西洋最早始于哪个皇帝？
  options:
    - {key: A, content: 明太祖}
    - {key: B, content: 明成祖}
    - {key: C, content: 明宣宗}
    - {key: D, content: 明世宗}
  answer: B
  knowledgeIds: [K-CHN-ANC-MING-QING]
  difficulty: 2
  author: 王老师
"""
    file_path = _write_yaml(tmp_path, yaml_content)
    item = TeacherUploadSource(file_path).fetch_raw()[0]
    assert item["sourceType"] == "ORIGINAL"
    assert item["license"] == "CC-BY-SA-4.0"
    assert item["subject"] == "HISTORY"
    assert item["primaryKnowledgeId"] == "K-CHN-ANC-MING-QING"


def test_missing_knowledge_ids_raises(tmp_path: Path) -> None:
    """缺 knowledgeIds 应抛错（考点必须显式标注）。"""
    yaml_content = """
- externalId: HIS-2026-004
  type: SINGLE_CHOICE
  stem: 无考点题
  options:
    - {key: A, content: 选项A}
    - {key: B, content: 选项B}
  answer: A
  difficulty: 2
  author: 张老师
"""
    file_path = _write_yaml(tmp_path, yaml_content)
    with pytest.raises(ValueError, match="knowledgeIds"):
        TeacherUploadSource(file_path).fetch_raw()


def test_directory_input_loads_all(tmp_path: Path) -> None:
    """目录输入应按文件名升序加载所有 YAML 文件。"""
    _write_yaml(tmp_path, "- externalId: A\n  knowledgeIds: [K-METHOD]\n", "a.yaml")
    _write_yaml(tmp_path, "- externalId: B\n  knowledgeIds: [K-METHOD]\n", "b.yaml")
    items = TeacherUploadSource(tmp_path).fetch_raw()
    assert [item["externalId"] for item in items] == ["A", "B"]


def test_no_yaml_files_raises(tmp_path: Path) -> None:
    """目录中无 YAML 文件应抛错。"""
    with pytest.raises(ValueError, match="未在"):
        TeacherUploadSource(tmp_path).fetch_raw()
