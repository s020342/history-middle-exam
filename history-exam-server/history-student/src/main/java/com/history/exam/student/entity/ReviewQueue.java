package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 复习队列实体
 * <p>对应表 review_queue：按 (student_id, question_id, due_date) 唯一，
 * 由每日 03:00 Job 扫描 mastery.next_review_at 生成，学生复习答题后置为已复习。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review_queue")
public class ReviewQueue extends BaseEntity {

    /** 学生 ID */
    private Long studentId;

    /** 题目 ID */
    private Long questionId;

    /** 应复习日 */
    private LocalDate dueDate;

    /** 状态：0 待复习 1 已复习 2 已过期 */
    private Integer status;

    /** 生成时间 */
    private LocalDateTime generatedAt;
}
