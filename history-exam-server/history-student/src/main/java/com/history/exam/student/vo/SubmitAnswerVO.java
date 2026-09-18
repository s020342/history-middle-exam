package com.history.exam.student.vo;

import lombok.Data;

/**
 * 提交单题答案出参（流式批改结果）
 */
@Data
public class SubmitAnswerVO {

    /** 是否正确 */
    private Boolean isCorrect;

    /** 正确答案（来自 question.answer） */
    private String correctAnswer;

    /** 掌握度等级：V1.0 固定 0，V1.1 起按规则计算 */
    private Integer masteryLevel;
}
