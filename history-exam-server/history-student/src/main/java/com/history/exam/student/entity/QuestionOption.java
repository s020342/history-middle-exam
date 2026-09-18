package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目选项实体
 * <p>对应 question_option 表；继承 BaseEntity 公共字段。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_option")
public class QuestionOption extends BaseEntity {

    /** 题目 ID */
    private Long questionId;

    /** 选项 key：A/B/C/D 或 T/F */
    private String optionKey;

    /** 选项内容 */
    private String content;

    /** 排序 */
    private Integer sort;
}
