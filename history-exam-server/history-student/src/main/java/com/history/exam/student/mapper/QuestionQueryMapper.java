package com.history.exam.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 题目只读查询 Mapper
 * <p>训练/错题子代理内部使用：仅查 question.answer 与更新 review_count/correct_rate，
 * 不创建 Question 实体（其他子代理负责），避免循环依赖与文件冲突。</p>
 */
@Mapper
public interface QuestionQueryMapper {

    /**
     * 按题目 ID 查询正确答案
     *
     * @param id 题目 ID
     * @return 正确答案字符串（选项 key 或 T/F）；不存在返回 null
     */
    String selectAnswerById(@Param("id") Long id);

    /**
     * 同步更新题目作答统计：review_count+1，correct_rate 滚动平均
     * <p>correct_rate = (原正确率 × 原次数 + 本次得分) / (原次数 + 1)；
     * 本次得分 100 对 / 0 错。</p>
     *
     * @param id        题目 ID
     * @param score     本次得分：100 答对 / 0 答错
     * @return 影响行数
     */
    int updateReviewStats(@Param("id") Long id, @Param("score") int score);
}
