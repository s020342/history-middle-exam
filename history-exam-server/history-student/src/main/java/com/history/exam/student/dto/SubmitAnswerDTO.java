package com.history.exam.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交单题答案入参（流式批改）
 */
@Data
public class SubmitAnswerDTO {

    /** 会话 ID */
    @NotNull(message = "会话 ID 必填")
    private Long sessionId;

    /** 题目 ID */
    @NotNull(message = "题目 ID 必填")
    private Long questionId;

    /** 学生答案 */
    @NotBlank(message = "学生答案必填")
    private String userAnswer;

    /** 答题用时（毫秒） */
    @NotNull(message = "答题用时必填")
    private Integer durationMs;
}
