package com.history.exam.student.vo;

import lombok.Data;

import java.util.List;

/**
 * 今日复习队列 VO
 * <p>包含应复习题数与题目列表；题目复用 QuestionVO 结构，前端可直接走 question-item 组件答题。</p>
 */
@Data
public class ReviewQueueVO {

    /** 今日待复习题数 */
    private Integer dueCount;

    /** 今日复习题目列表 */
    private List<QuestionVO> questions;
}
