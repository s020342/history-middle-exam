"""题库 JSON + manifest 导出

对应 docs/05 §3.3 文件命名与 §5.3 流程步骤 8。
产出两个文件：
1. <date>-<source>.json：题目批次 JSON（docs/05 §1.3 题库数据统一 CC-BY-SA 4.0）
2. manifest.json：累积清单，记录所有批次的文件名、来源、协议、题数
"""

from __future__ import annotations

import json
from datetime import datetime
from pathlib import Path
from typing import Any


def _build_batch_payload(
    questions: list[dict[str, Any]],
    source: str,
    license_value: str,
) -> dict[str, Any]:
    """构建符合 QuestionBatch Schema 的批次载荷。

    :param questions: 已校验的题目字典列表
    :param source: 来源标识
    :param license_value: 批次主协议
    :return: 批次字典
    """
    return {
        "subject": "HISTORY",
        "source": source,
        "license": license_value,
        "generatedAt": datetime.now().astimezone().isoformat(timespec="seconds"),
        "questions": questions,
    }


def export_batch(
    questions: list[dict[str, Any]],
    output_dir: str | Path,
    source: str,
    license_value: str,
) -> Path:
    """写出题目批次 JSON 文件并更新 manifest。

    文件名格式：YYYY-MM-DD-<source>.json（docs/05 §3.3）。

    :param questions: 已校验的题目字典列表
    :param output_dir: 输出目录
    :param source: 来源标识，如 teacher-original
    :param license_value: 批次主协议
    :return: 写出的 JSON 文件路径
    """
    out_dir = Path(output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    date_str = datetime.now().strftime("%Y-%m-%d")
    file_name = f"{date_str}-{source}.json"
    file_path = out_dir / file_name

    payload = _build_batch_payload(questions, source, license_value)
    # ensure_ascii=False 保留中文可读性；indent=2 便于人工复核（docs/05 §8.2）
    with file_path.open("w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)

    _update_manifest(out_dir, file_name, payload)
    return file_path


def _update_manifest(out_dir: Path, file_name: str, payload: dict[str, Any]) -> None:
    """更新或创建 manifest.json 清单（docs/05 §3.3：文件、题数、协议、来源）。

    同名文件已存在则更新其记录，否则追加；manifest 结构为 { files: [...] }。

    :param out_dir: 输出目录
    :param file_name: 本次写出的文件名
    :param payload: 本次批次载荷
    """
    manifest_path = out_dir / "manifest.json"
    if manifest_path.exists():
        with manifest_path.open("r", encoding="utf-8") as f:
            manifest = json.load(f)
    else:
        manifest = {"files": []}

    files = [item for item in manifest.get("files", []) if item.get("name") != file_name]
    files.append(
        {
            "name": file_name,
            "source": payload["source"],
            "license": payload["license"],
            "generatedAt": payload["generatedAt"],
            "count": len(payload["questions"]),
        }
    )
    manifest["files"] = files

    with manifest_path.open("w", encoding="utf-8") as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)
