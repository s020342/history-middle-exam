"""历史中考题库抓取与录入工具

V1.0 MVP 范围（docs/05-数据抓取方案文档.md）：
1. Pydantic Schema 校验 (validator.py)
2. 教师原创录入流程 (sources/teacher_upload.py)
3. MinHash 去重 (dedup.py)
4. 考点关键词映射 (knowledge_mapper.py)
5. JSON + manifest 导出 (exporter.py)
6. CLI 入口 (cli.py)

不包含：网络爬虫（V1.1+ 扩展开源搬运）、AI 命题、UGC 平台。
"""

__version__ = "1.0.0"
