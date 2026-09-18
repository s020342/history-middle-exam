"""CLI 入口

对应 docs/05 §5.3 抓取流程的对外命令：
- validate <file>        校验单个 YAML/JSON 题目文件
- teacher-import <path>  教师原创录入：加载 → 校验 → 去重 → 考点提示 → 导出 JSON 与 manifest
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import click
import yaml
from rich.console import Console
from rich.table import Table

from .dedup import dedup_by_stem
from .exporter import export_batch
from .knowledge_mapper import check_consistency
from .sources.teacher_upload import DEFAULT_TEACHER_LICENSE, TeacherUploadSource
from .validator import validate_question

console = Console()


def _load_raw_questions(file_path: str) -> list[dict[str, Any]]:
    """加载 YAML/JSON 文件为原始题目字典列表。

    :param file_path: 文件路径
    :return: 原始题目字典列表
    :raises click.ClickException: 文件不存在、后缀不支持、解析失败或顶层结构非法
    """
    path = Path(file_path)
    if not path.exists():
        raise click.ClickException(f"文件不存在: {path}")

    suffix = path.suffix.lower()
    if suffix in (".yaml", ".yml"):
        loader = yaml.safe_load
    elif suffix == ".json":
        loader = json.load
    else:
        raise click.ClickException(f"仅支持 YAML/JSON，当前后缀: {suffix}")

    try:
        with path.open("r", encoding="utf-8") as f:
            raw = loader(f)
    except (yaml.YAMLError, json.JSONDecodeError) as e:
        raise click.ClickException(f"解析失败: {e}") from e

    if isinstance(raw, list):
        return raw
    if isinstance(raw, dict) and isinstance(raw.get("questions"), list):
        return raw["questions"]
    raise click.ClickException("顶层必须为列表或含 questions 列表的对象")


def _validate_all(raw_items: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """逐题校验，打印结果摘要；存在失败项时中止流程。

    :param raw_items: 原始题目字典列表
    :return: 校验通过的题目字典列表
    :raises click.Abort: 存在校验失败项
    """
    valid: list[dict[str, Any]] = []
    errors: list[tuple[int, Exception]] = []
    for idx, item in enumerate(raw_items):
        try:
            validate_question(item)
            valid.append(item)
        except Exception as e:  # noqa: BLE001 - CLI 层聚合所有校验错误后统一展示
            errors.append((idx, e))

    table = Table(title="校验结果")
    table.add_column("结果", style="bold")
    table.add_column("数量")
    table.add_row("通过", str(len(valid)), style="green")
    table.add_row("失败", str(len(errors)), style="red")
    console.print(table)
    for idx, err in errors:
        console.print(f"[red]第 {idx + 1} 题[/red]: {err}")

    if errors:
        raise click.Abort()
    return valid


@click.group()
def cli() -> None:
    """历史中考题库抓取与录入工具 CLI。"""


@cli.command("validate")
@click.argument("file", type=click.Path(exists=True))
def validate_cmd(file: str) -> None:
    """校验题目文件：YAML 按教师录入语义补默认值后校验，JSON 按导出产物严格校验。"""
    if Path(file).suffix.lower() in (".yaml", ".yml"):
        # 教师原始稿允许缺省 subject/sourceType/license，走 normalize 补默认值
        raw_items = TeacherUploadSource(file).fetch_raw()
    else:
        raw_items = _load_raw_questions(file)
    _validate_all(raw_items)


@cli.command("teacher-import")
@click.argument("input_path", type=click.Path(exists=True))
@click.option("--output", "output_dir", required=True, help="输出目录")
def teacher_import_cmd(input_path: str, output_dir: str) -> None:
    """教师原创录入：加载 → 校验 → 去重 → 考点提示 → 导出 JSON 与 manifest。"""
    source = TeacherUploadSource(input_path)
    raw_items = source.fetch_raw()
    console.print(f"[cyan]加载[/cyan] {len(raw_items)} 题")

    # 步骤 1：Schema + 协议校验（docs/05 §5.3 步骤 3/7）
    valid = _validate_all(raw_items)

    # 步骤 2：MinHash 去重（docs/05 §5.3 步骤 5）
    dedup_result = dedup_by_stem([q.get("stem", "") for q in valid])
    unique_questions = [valid[i] for i in dedup_result.unique]
    if dedup_result.duplicates:
        console.print(f"[yellow]去重[/yellow] 丢弃 {len(dedup_result.duplicates)} 题（相似度 > 0.92）")
        for dup_idx, kept_idx in dedup_result.duplicates:
            console.print(f"  重复: 第 {dup_idx + 1} 题 与第 {kept_idx + 1} 题相似")

    # 步骤 3：考点一致性检查，仅警告不阻断（docs/05 §5.3 步骤 6，低置信度转人工）
    for hint in check_consistency(unique_questions):
        console.print(
            f"[yellow]考点提示[/yellow] {hint.question_external_id} "
            f"教师标={hint.teacher_primary} 关键词命中={hint.keyword_hit}"
        )

    # 步骤 4：导出 JSON + manifest（docs/05 §5.3 步骤 8）
    file_path = export_batch(
        unique_questions,
        output_dir,
        source=source.name,
        license_value=DEFAULT_TEACHER_LICENSE,
    )
    console.print(f"[green]导出[/green] {file_path}（共 {len(unique_questions)} 题）")


if __name__ == "__main__":
    cli()
