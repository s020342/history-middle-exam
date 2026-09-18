package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 掌握度实体
 * <p>对应表 mastery：按 (student_id, question_id) 唯一记录学生对单题的掌握等级、
 * 连续答对次数与下次复习日。简化 SM-2 算法的关键状态。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mastery")
public class Mastery extends BaseEntity {

    /** 学生 ID */
    private Long studentId;

    /** 题目 ID */
    private Long questionId;

    /** 知识点 ID（按题目的主考点冗余，可空） */
    private Long knowledgeId;

    /** 掌握度等级：0 未掌握 1 部分 2 已掌握 */
    private Integer level;

    /** 连续答对次数（达 3 次升级） */
    private Integer consecutiveCorrect;

    /** 下次复习日（按 level 推算：0→+1d / 1→+3d / 2→+7d） */
    private LocalDate nextReviewAt;
}
