package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.student.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目 Mapper
 * <p>基于 MyBatis-Plus BaseMapper 扩展，提供随机组卷等自定义查询。</p>
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 随机抽取指定题型与数量的题目 ID
     * <p>仅取未删除且上架的题目；type 为空时混合抽取。</p>
     *
     * @param type  题型：1 选择 2 判断，null 表示混合
     * @param count 题量
     * @return 题目 ID 列表
     */
    List<Long> selectRandomQuestionIds(@Param("type") Integer type, @Param("count") int count);
}
