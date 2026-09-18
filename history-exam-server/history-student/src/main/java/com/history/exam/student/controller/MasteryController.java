package com.history.exam.student.controller;

import com.history.exam.common.api.Result;
import com.history.exam.common.context.CurrentUserHolder;
import com.history.exam.student.service.MasteryService;
import com.history.exam.student.vo.MasterySummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生掌握度接口
 * <p>路由前缀 /student/mastery，要求 STUDENT 角色（见 SecurityConfig）。</p>
 */
@RestController
@RequestMapping("/student/mastery")
@RequiredArgsConstructor
public class MasteryController {

    private final MasteryService masteryService;

    /**
     * 查询当前学生掌握度概览（按知识点维度聚合）
     *
     * @return 掌握度概览
     */
    @GetMapping("/summary")
    public Result<MasterySummaryVO> summary() {
        Long studentId = CurrentUserHolder.getUserId();
        return Result.success(masteryService.getStudentSummary(studentId));
    }
}
