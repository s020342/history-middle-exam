package com.history.exam.student.vo;

import lombok.Data;

import java.util.List;

/**
 * 题目 VO
 * <p>id 序列化为字符串避免 JS 精度丢失；含正确答案与解析供 V1.0 流式批改前端展示正误。
 * 不返回 status/reviewCount/correctRate/externalId/license/author/sourceUrl 等运营字段。</p>
 */
@Data
public class QuestionVO {

    /** 题目 ID（字符串传输） */
    private String id;

    /** 题型：1 选择 2 判断 */
    private Integer type;

    /** 题干 */
    private String stem;

    /** 难度：1 易 2 中 3 难 */
    private Integer difficulty;

    /** 正确答案（选项 key 或 T/F） */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 选项列表 */
    private List<QuestionOptionVO> options;
}
