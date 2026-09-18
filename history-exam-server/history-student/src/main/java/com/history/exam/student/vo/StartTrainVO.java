package com.history.exam.student.vo;

import lombok.Data;

/**
 * 创建训练会话出参
 */
@Data
public class StartTrainVO {

    /** 会话 ID（字符串避免 JS 精度丢失） */
    private String sessionId;

    /** 训练模式 */
    private Integer mode;

    /** 题数 */
    private Integer total;
}
