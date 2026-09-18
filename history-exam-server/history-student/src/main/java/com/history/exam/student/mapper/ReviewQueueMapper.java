package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.student.entity.ReviewQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 复习队列 Mapper
 * <p>提供 review_queue 表基础 CRUD + 今日队列查询 + Job upsert 与复习完成标记。</p>
 */
@Mapper
public interface ReviewQueueMapper extends BaseMapper<ReviewQueue> {

    /**
     * 查询学生当日待复习题目列表（status=0 待复习）
     *
     * @param studentId 学生 ID
     * @param today     当日日期
     * @return 复习队列记录列表
     */
    List<ReviewQueue> selectTodayQueue(@Param("studentId") Long studentId,
                                       @Param("today") LocalDate today);

    /**
     * Job upsert：按 uk_student_question_date 唯一键存在则跳过，否则插入
     *
     * @param studentId   学生 ID
     * @param questionId  题目 ID
     * @param dueDate     应复习日
     * @param status      状态
     * @param generatedAt 生成时间
     * @return 受影响行数（1 新增，0 已存在跳过）
     */
    int upsertByStudentQuestionDate(@Param("studentId") Long studentId,
                                   @Param("questionId") Long questionId,
                                   @Param("dueDate") LocalDate dueDate,
                                   @Param("status") Integer status,
                                   @Param("generatedAt") LocalDateTime generatedAt);

    /**
     * 标记某题当日已复习（status=1）
     *
     * @param studentId  学生 ID
     * @param questionId 题目 ID
     * @param dueDate    应复习日
     * @return 受影响行数
     */
    int markReviewed(@Param("studentId") Long studentId,
                     @Param("questionId") Long questionId,
                     @Param("dueDate") LocalDate dueDate);
}
