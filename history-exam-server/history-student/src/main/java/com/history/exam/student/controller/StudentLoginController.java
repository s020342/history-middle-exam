package com.history.exam.student.controller;

import com.history.exam.common.api.Result;
import com.history.exam.student.dto.StudentLoginDTO;
import com.history.exam.student.service.StudentLoginService;
import com.history.exam.student.vo.StudentLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生登录控制器
 * <p>对外暴露 POST /student/login，完成微信小程序登录。</p>
 */
@Slf4j
@RestController
@RequestMapping("/student/login")
@RequiredArgsConstructor
public class StudentLoginController {

    /** 学生登录服务 */
    private final StudentLoginService studentLoginService;

    /**
     * 微信小程序登录
     *
     * @param dto 登录入参（code、nickname、avatarUrl）
     * @return 登录响应（token + 学生信息）
     */
    @PostMapping("")
    public Result<StudentLoginVO> login(@Valid @RequestBody StudentLoginDTO dto) {
        StudentLoginVO vo = studentLoginService.login(dto);
        return Result.success(vo);
    }
}
