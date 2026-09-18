package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 训练会话实体
 * <p>对应表 train_session：记录一次训练会话的整体状态（模式、题数、已答、答对、状态等）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("train_session")
public class TrainSession extends BaseEntity {

    /** 学生 ID */
    private Long studentId;

    /** 训练模式：1 自由 2 错题 3 复习 */
    private Integer mode;

    /** 题数 */
    private Integer total;

    /** 已答 */
    private Integer answered;

    /** 答对 */
    private Integer correct;

    /** 开始时间 */
    private LocalDateTime startedAt;

    /** 结束时间 */
    private LocalDateTime finishedAt;

    /** 会话状态：0 进行 1 完成 2 中断 */
    private Integer status;
}
