package com.history.exam.student.vo;

import lombok.Data;

/**
 * 结束训练会话出参（汇总）
 */
@Data
public class TrainSummaryVO {

    /** 会话 ID（字符串避免 JS 精度丢失） */
    private String sessionId;

    /** 已答题数 */
    private Integer answered;

    /** 答对题数 */
    private Integer correct;
}
