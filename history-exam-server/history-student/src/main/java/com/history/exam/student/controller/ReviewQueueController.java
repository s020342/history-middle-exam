package com.history.exam.student.controller;

import com.history.exam.common.api.Result;
import com.history.exam.common.context.CurrentUserHolder;
import com.history.exam.student.service.ReviewQueueService;
import com.history.exam.student.vo.ReviewQueueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生复习队列接口
 * <p>路由前缀 /student/review，要求 STUDENT 角色（见 SecurityConfig）。</p>
 */
@RestController
@RequestMapping("/student/review")
@RequiredArgsConstructor
public class ReviewQueueController {

    private final ReviewQueueService reviewQueueService;

    /**
     * 查询当前学生今日待复习队列
     *
     * @return 今日复习队列
     */
    @GetMapping("/today")
    public Result<ReviewQueueVO> today() {
        Long studentId = CurrentUserHolder.getUserId();
        return Result.success(reviewQueueService.getTodayQueue(studentId));
    }
}
