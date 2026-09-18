package com.history.exam.student.vo;

import lombok.Data;

/**
 * 错题列表项出参
 * <p>id/questionId 转字符串避免 JS 精度丢失。</p>
 */
@Data
public class WrongRecordVO {

    /** 错题记录 ID（字符串） */
    private String id;

    /** 题目 ID（字符串） */
    private String questionId;

    /** 题型：1 选择 2 判断 */
    private Integer questionType;

    /** 题干 */
    private String stem;

    /** 正确答案 */
    private String correctAnswer;

    /** 累计错次 */
    private Integer errorCount;

    /** 错因 ID（字符串） */
    private String errorCauseId;

    /** 错因名称 */
    private String errorCauseName;
}
