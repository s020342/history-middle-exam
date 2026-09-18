package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.student.entity.QuestionKnowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 题目-知识点关联 Mapper
 * <p>提供 question_knowledge 表基础 CRUD + 按题目查主考点 knowledge_id。</p>
 */
@Mapper
public interface QuestionKnowledgeMapper extends BaseMapper<QuestionKnowledge> {

    /**
     * 按题目查询主考点（is_primary=1）的 knowledge_id
     *
     * @param questionId 题目 ID
     * @return 主知识点 ID；未关联返回 null
     */
    Long selectPrimaryKnowledgeIdByQuestionId(@Param("questionId") Long questionId);
}
