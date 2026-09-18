package com.history.exam.student.service;

import com.history.exam.student.vo.ReviewQueueVO;

/**
 * 复习队列服务
 * <p>提供学生当日复习队列查询与复习完成标记；命中 Redis 缓存。</p>
 */
public interface ReviewQueueService {

    /**
     * 查询学生今日待复习队列（命中 Redis 缓存 student:review:today:{sid}）
     *
     * @param studentId 学生 ID
     * @return 今日复习队列（含 dueCount 与 questions）
     */
    ReviewQueueVO getTodayQueue(Long studentId);

    /**
     * 标记某题当日复习完成（status=1），并失效当日缓存
     *
     * @param studentId  学生 ID
     * @param questionId 题目 ID
     * @param dueDate    应复习日
     */
    void markReviewed(Long studentId, Long questionId, java.time.LocalDate dueDate);
}
