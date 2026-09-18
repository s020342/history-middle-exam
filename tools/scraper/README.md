# 历史中考题库抓取与录入工具 (V1.0 MVP)

> 关联文档：`docs/05-数据抓取方案文档.md`、`docs/03-数据库设计文档.md`、`docs/04-接口文档.md`

## 范围

V1.0 抓取侧仅交付两件事：

| 能力 | 模块 | 说明 |
|------|------|------|
| Schema 与 Pydantic 校验 | `scraper.validator` | 题库 JSON 入库前的字段、选项、答案、许可证、来源校验 |
| 教师原创录入流程 | `scraper.sources.teacher_upload` | 教师以 YAML 提交题目 → 校验 → 去重 → 导出 JSON |

不在 V1.0 范围：网络爬虫（github_source 为 V1.1 扩展）、AI 命题、UGC 平台。

## 安装

```bash
cd tools/scraper
python3.11 -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
```

## 使用

### 1. 教师以 YAML 提交原创题

样例文件：`teachers/sample.yaml`。每题字段对齐 `docs/05` §3.1 JSON Schema；
`sourceType` / `license` / `subject` / `primaryKnowledgeId` 缺省时自动补默认值。

### 2. 校验 + 去重 + 导出

```bash
# 校验单个 YAML 文件
history-scraper validate teachers/sample.yaml

# 教师原创录入：校验 → 去重 → 考点提示 → 导出 JSON 与 manifest
history-scraper teacher-import teachers/sample.yaml --output data/question-bank/history/

# 批量录入一个目录下所有 YAML
history-scraper teacher-import teachers/ --output data/question-bank/history/
```

### 3. 上传至后台

产出的 `data/question-bank/history/<date>-teacher-original.json` 由管理员调用
`POST /api/admin/question/import` 上传，接口按 `external_id` 唯一索引去重，
返回 `{ inserted, skipped, conflicted }`。

## 测试

```bash
pytest -v
```

## 目录结构

```
tools/scraper/
├── README.md
├── pyproject.toml
├── src/scraper/
│   ├── __init__.py
│   ├── constants.py            # 枚举与常量（对齐 03 文档 question 表）
│   ├── validator.py            # Pydantic Schema 与校验
│   ├── dedup.py                # MinHash 题干去重
│   ├── knowledge_mapper.py     # 考点关键词映射
│   ├── exporter.py             # JSON + manifest 导出
│   ├── cli.py                  # CLI 入口
│   └── sources/
│       ├── base.py             # 抽象 Source
│       └── teacher_upload.py   # 教师原创录入适配器
├── teachers/                   # 教师提交的 YAML 原稿
├── tests/
└── data/question-bank/history/ # 输出目录（manifest 版本化，批次 JSON 按日期归档）
```

## 合规红线（docs/05 §1.2 五条铁律）

- `sourceType` 禁用 `PAST_EXAM_RAW`（真题原文），validator 层硬拦截。
- 每道题必须 `author` 与 `sourceUrl` 至少一项非空，保证来源可追溯。
- `license` 必须在白名单内，协议不明的题一律不进生产库。
