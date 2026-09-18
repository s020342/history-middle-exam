package com.history.exam.student.service;

import com.history.exam.student.vo.MasterySummaryVO;

/**
 * 掌握度服务
 * <p>简化 SM-2 算法：在答题事务内实时刷新掌握度，并提供按知识点维度的聚合查询。
 * 不依赖 TrainService，避免循环依赖。</p>
 */
public interface MasteryService {

    /**
     * 刷新学生单题掌握度（简化 SM-2）
     * <p>逻辑：
     * <ul>
     *   <li>答错：consecutiveCorrect=0；level = max(0, level-1)；nextReviewAt = 明日</li>
     *   <li>答对：consecutiveCorrect++；若 >=3 升一档（最高 2）并重置；nextReviewAt 按 level 推算</li>
     * </ul>
     * 调用后失效 Redis 缓存 student:mastery:{sid}。</p>
     *
     * @param studentId  学生 ID
     * @param questionId 题目 ID
     * @param isCorrect  本次是否答对
     * @return 最新掌握度等级（0/1/2）
     */
    int refreshMastery(Long studentId, Long questionId, boolean isCorrect);

    /**
     * 查询学生掌握度概览（按知识点维度聚合，命中 Redis 缓存）
     *
     * @param studentId 学生 ID
     * @return 掌握度概览
     */
    MasterySummaryVO getStudentSummary(Long studentId);
}
