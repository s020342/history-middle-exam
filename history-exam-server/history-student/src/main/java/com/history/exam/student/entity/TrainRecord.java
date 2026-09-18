package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 训练答题记录实体
 * <p>对应表 train_record：单题答题明细（学生答案、是否正确、用时、错因、答题时间）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("train_record")
public class TrainRecord extends BaseEntity {

    /** 会话 ID */
    private Long sessionId;

    /** 学生 ID */
    private Long studentId;

    /** 题目 ID */
    private Long questionId;

    /** 学生答案 */
    private String userAnswer;

    /** 是否正确：0 错 1 对 */
    private Integer isCorrect;

    /** 答题用时（毫秒） */
    private Integer durationMs;

    /** 错因 ID */
    private Long errorCauseId;

    /** 答题时间 */
    private LocalDateTime answeredAt;
}
