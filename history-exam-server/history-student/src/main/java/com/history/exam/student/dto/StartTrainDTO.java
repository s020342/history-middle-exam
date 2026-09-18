package com.history.exam.student.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建训练会话入参
 */
@Data
public class StartTrainDTO {

    /** 训练模式：1 自由 2 错题 3 复习 */
    @NotNull(message = "训练模式必填")
    private Integer mode;

    /** 题目 ID 列表 */
    @NotEmpty(message = "题目列表不能为空")
    private List<Long> questionIds;

    /** 题数（与 questionIds.size() 一致，作为冗余校验） */
    private Integer total;
}
