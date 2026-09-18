package com.history.exam.student.service;

import com.history.exam.student.dto.BuildQuestionDTO;
import com.history.exam.student.vo.QuestionVO;

import java.util.List;

/**
 * 题目服务
 * <p>负责题目查询与随机组卷相关业务。</p>
 */
public interface QuestionService {

    /**
     * 随机组卷：按题型与题量返回题目列表（含选项与正确答案）
     * <p>V1.0 忽略 knowledgeId（question_knowledge 关联表 V1.1 落库）。</p>
     *
     * @param dto 组卷参数（type 题型可选；count 题量；knowledgeId V1.0 忽略）
     * @return 题目 VO 列表
     */
    List<QuestionVO> buildQuestions(BuildQuestionDTO dto);
}
