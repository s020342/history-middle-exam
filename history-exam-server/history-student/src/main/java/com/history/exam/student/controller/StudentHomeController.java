package com.history.exam.student.controller;

import com.history.exam.common.api.Result;
import com.history.exam.common.context.CurrentUserHolder;
import com.history.exam.student.service.StudentHomeService;
import com.history.exam.student.vo.HomeProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生首页控制器
 * <p>对外暴露 GET /student/home/profile，返回首页概况。</p>
 */
@Slf4j
@RestController
@RequestMapping("/student/home")
@RequiredArgsConstructor
public class StudentHomeController {

    /** 学生首页服务 */
    private final StudentHomeService studentHomeService;

    /**
     * 获取首页概况
     *
     * @return 首页概况视图
     */
    @GetMapping("/profile")
    public Result<HomeProfileVO> getProfile() {
        Long studentId = CurrentUserHolder.getUserId();
        HomeProfileVO vo = studentHomeService.getProfile(studentId);
        return Result.success(vo);
    }
}
