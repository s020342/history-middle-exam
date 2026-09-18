package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目-知识点关联实体
 * <p>对应表 question_knowledge：多对多关系，is_primary=1 表示主考点。
 * 用于 mastery 写入时按题目反查主考点 knowledge_id。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_knowledge")
public class QuestionKnowledge extends BaseEntity {

    /** 题目 ID */
    private Long questionId;

    /** 知识点 ID */
    private Long knowledgeId;

    /** 是否主考点：0 否 1 是 */
    private Integer isPrimary;
}
