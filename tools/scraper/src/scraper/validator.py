"""题库 JSON Schema 与 Pydantic 校验

对应 docs/05 §3.1 单题结构、§5.4 协议校验规则、§8.1 自动校验规则。
校验失败抛出 pydantic.ValidationError，由 CLI 层捕获后聚合报错并非零退出。
"""

from __future__ import annotations

from typing import Annotated

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator

from .constants import (
    ALLOWED_LICENSES,
    FORBIDDEN_SOURCE_TYPES,
    KNOWLEDGE_CODES,
    QUESTION_TYPE_JSON_TO_DB,
    SINGLE_CHOICE_KEYS,
    SOURCE_TYPE_JSON_TO_DB,
    TRUE_FALSE_ANSWERS,
)


class Option(BaseModel):
    """题目选项模型，对应 docs/05 §3.1 options 数组项。"""

    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)

    key: Annotated[str, Field(min_length=1, max_length=4, description="选项 key A/B/C/D")]
    content: Annotated[str, Field(min_length=1, max_length=255, description="选项内容")]

    @field_validator("key")
    @classmethod
    def _key_must_be_upper(cls, value: str) -> str:
        """选项 key 统一转大写，避免与 answer 匹配时因大小写不一致而失败。"""
        return value.upper()


class Question(BaseModel):
    """单题 Schema 模型。

    字段名采用 camelCase，与 docs/05 §3.2 字段对照表一致，后端导入时直接映射
    （externalId→external_id, sourceType→source_type 等）。
    """

    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)

    externalId: Annotated[str, Field(min_length=1, max_length=64, description="抓取侧唯一 ID")]
    subject: Annotated[str, Field(pattern=r"^HISTORY$", description="V1.0 仅支持 HISTORY")]
    type: Annotated[str, Field(description="题型 SINGLE_CHOICE / TRUE_FALSE")]
    stem: Annotated[str, Field(min_length=1, description="题干文本")]
    stemImage: Annotated[str | None, Field(default=None, max_length=255, description="题干配图 URL，可空")]
    options: Annotated[list[Option] | None, Field(default=None, description="选择题必填，判断题为空")]
    answer: Annotated[str, Field(min_length=1, max_length=16, description="正确答案（选项 key 或 T/F）")]
    analysis: Annotated[str | None, Field(default=None, description="解析，可空")]
    knowledgeIds: Annotated[list[str], Field(min_length=1, description="考点编码列表，至少 1 个")]
    primaryKnowledgeId: Annotated[str, Field(description="主考点编码，必须出现在 knowledgeIds 中")]
    difficulty: Annotated[int, Field(ge=1, le=3, description="难度 1 易 2 中 3 难")]
    sourceType: Annotated[str, Field(description="来源类型枚举字符串")]
    sourceYear: Annotated[int | None, Field(default=None, ge=1900, le=2100, description="真题年份，可空")]
    sourceDesc: Annotated[str | None, Field(default=None, max_length=128, description="来源描述，可空")]
    sourceUrl: Annotated[str | None, Field(default=None, max_length=255, description="来源链接，可空")]
    author: Annotated[str | None, Field(default=None, max_length=64, description="原作者/投稿人")]
    license: Annotated[str, Field(description="许可证，必须在白名单中")]
    variantExternalIds: Annotated[list[str] | None, Field(default=None, description="变式题 externalId 列表")]
    variantRelationType: Annotated[str | None, Field(default=None, description="变式关联类型 VARIANT/SIMILAR")]
    tags: Annotated[list[str] | None, Field(default=None, description="标签，如 高频、秦汉史")]

    @field_validator("type")
    @classmethod
    def _type_must_be_valid(cls, value: str) -> str:
        """题型必须是 SINGLE_CHOICE / TRUE_FALSE 之一。"""
        if value not in QUESTION_TYPE_JSON_TO_DB:
            raise ValueError(f"非法题型: {value}，仅支持 SINGLE_CHOICE / TRUE_FALSE")
        return value

    @field_validator("sourceType")
    @classmethod
    def _source_type_must_be_valid(cls, value: str) -> str:
        """来源类型必须合法，且不得为 PAST_EXAM_RAW（真题原文禁用，docs/05 §1.1）。"""
        if value in FORBIDDEN_SOURCE_TYPES:
            raise ValueError("sourceType=PAST_EXAM_RAW 禁用，禁止使用真题原文")
        if value not in SOURCE_TYPE_JSON_TO_DB:
            raise ValueError(f"非法来源类型: {value}")
        return value

    @field_validator("license")
    @classmethod
    def _license_must_be_allowed(cls, value: str) -> str:
        """许可证必须在白名单中（docs/05 §5.4），存疑协议一律不进生产库。"""
        if value not in ALLOWED_LICENSES:
            raise ValueError(f"非法许可证: {value}，允许: {sorted(ALLOWED_LICENSES)}")
        return value

    @field_validator("knowledgeIds")
    @classmethod
    def _knowledge_ids_must_be_known(cls, value: list[str]) -> list[str]:
        """每个 knowledgeId 必须出现在考点编码白名单中（docs/05 §8.1 考点校验）。"""
        unknown = [k for k in value if k not in KNOWLEDGE_CODES]
        if unknown:
            raise ValueError(f"未知考点编码: {unknown}")
        return value

    @model_validator(mode="after")
    def _check_business_rules(self) -> Question:
        """跨字段业务校验：

        1. 主考点必须在 knowledgeIds 中；
        2. 选择题必须有 2~4 个选项，key 唯一且合法，答案在选项 key 中；
        3. 判断题 options 必须为空，答案必须是 T/F；
        4. 协议追溯：author 与 sourceUrl 至少一个非空（docs/05 §1.2 铁律 3）。
        """
        if self.primaryKnowledgeId not in self.knowledgeIds:
            raise ValueError("primaryKnowledgeId 必须出现在 knowledgeIds 中")

        if self.type == "SINGLE_CHOICE":
            self._validate_single_choice()
        else:  # TRUE_FALSE
            self._validate_true_false()

        if not self.sourceUrl and not self.author:
            raise ValueError("author 与 sourceUrl 至少填写一项，便于来源追溯")

        return self

    def _validate_single_choice(self) -> None:
        """选择题选项与答案校验：数量 2~4、key 唯一合法、答案必须在选项中。"""
        if not self.options or not 2 <= len(self.options) <= 4:
            raise ValueError("选择题必须有 2~4 个选项")
        keys = [opt.key for opt in self.options]
        if len(set(keys)) != len(keys):
            raise ValueError(f"选项 key 重复: {keys}")
        illegal = [k for k in keys if k not in SINGLE_CHOICE_KEYS]
        if illegal:
            raise ValueError(f"选项 key 必须在 {sorted(SINGLE_CHOICE_KEYS)} 内，非法: {illegal}")
        if self.answer not in keys:
            raise ValueError(f"答案 {self.answer} 不在选项 key {keys} 中")

    def _validate_true_false(self) -> None:
        """判断题校验：options 必须为空，答案必须是 T/F。"""
        if self.options:
            raise ValueError("判断题 options 必须为空")
        if self.answer not in TRUE_FALSE_ANSWERS:
            raise ValueError(f"判断题答案必须是 T/F，当前: {self.answer}")


class QuestionBatch(BaseModel):
    """题目批次模型，对应一次抓取/录入产出的 JSON 文件（docs/05 §3.3）。"""

    model_config = ConfigDict(extra="forbid")

    subject: Annotated[str, Field(pattern=r"^HISTORY$")]
    source: Annotated[str, Field(description="批次来源标识，如 teacher-original")]
    license: Annotated[str, Field(description="本批次主协议")]
    generatedAt: Annotated[str, Field(description="生成时间 ISO8601")]
    questions: Annotated[list[Question], Field(min_length=1, description="题目列表，至少 1 题")]


def validate_question(data: dict) -> Question:
    """校验并返回单题模型。

    :param data: 原始字典数据（来自 YAML/JSON 解析）
    :return: Pydantic Question 模型实例
    :raises pydantic.ValidationError: 任意字段校验失败
    """
    return Question.model_validate(data)


def validate_batch(data: dict) -> QuestionBatch:
    """校验并返回题目批次模型。

    :param data: 批次字典数据
    :return: Pydantic QuestionBatch 模型实例
    :raises pydantic.ValidationError: 批次或任一题目校验失败
    """
    return QuestionBatch.model_validate(data)
