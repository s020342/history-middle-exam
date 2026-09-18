package com.history.exam.student.controller;

import com.history.exam.common.api.PageResult;
import com.history.exam.common.api.Result;
import com.history.exam.student.dto.SubmitErrorCauseDTO;
import com.history.exam.student.service.WrongRecordService;
import com.history.exam.student.vo.WrongRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 错题记录接口
 * <p>路由前缀 /student/wrong-records，要求 STUDENT 角色（见 SecurityConfig）。</p>
 */
@RestController
@RequestMapping("/student/wrong-records")
@RequiredArgsConstructor
public class WrongRecordController {

    private final WrongRecordService wrongRecordService;

    /**
     * 分页查询当前学生错题列表
     *
     * @param page 页码（默认 1）
     * @param size 每页大小（默认 20）
     * @return 错题分页结果
     */
    @GetMapping
    public Result<PageResult<WrongRecordVO>> listWrongRecords(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(wrongRecordService.listWrongRecords(page, size));
    }

    /**
     * 提交错因到指定错题记录
     *
     * @param dto 错因入参
     * @return 成功响应
     */
    @PostMapping("/error-cause")
    public Result<Void> submitErrorCause(@Valid @RequestBody SubmitErrorCauseDTO dto) {
        wrongRecordService.submitErrorCause(dto);
        return Result.success();
    }
}
