package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.student.entity.QuestionOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目选项 Mapper
 * <p>提供按题目 ID 查询选项的自定义方法。</p>
 */
@Mapper
public interface QuestionOptionMapper extends BaseMapper<QuestionOption> {

    /**
     * 按题目 ID 查询选项列表（按 sort 升序）
     *
     * @param questionId 题目 ID
     * @return 选项列表
     */
    List<QuestionOption> selectByQuestionId(@Param("questionId") Long questionId);
}
