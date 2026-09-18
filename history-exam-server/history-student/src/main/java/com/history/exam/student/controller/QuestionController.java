package com.history.exam.student.controller;

import com.history.exam.common.api.Result;
import com.history.exam.student.dto.BuildQuestionDTO;
import com.history.exam.student.service.QuestionService;
import com.history.exam.student.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 题目接口
 * <p>提供小程序题目查询与随机组卷能力。</p>
 */
@RestController
@RequestMapping("/student/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 随机组卷
     * <p>GET /student/questions/build?type=1&count=10
     * 按题型与题量返回题目列表（含选项与正确答案，供 V1.0 流式批改前端展示正误）。</p>
     *
     * @param dto 组卷参数（type 题型可选；count 题量；knowledgeId V1.0 忽略）
     * @return 题目列表
     */
    @GetMapping("/build")
    public Result<List<QuestionVO>> build(BuildQuestionDTO dto) {
        return Result.success(questionService.buildQuestions(dto));
    }
}
