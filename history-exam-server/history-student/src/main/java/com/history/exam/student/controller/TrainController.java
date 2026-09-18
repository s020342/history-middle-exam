package com.history.exam.student.controller;

import com.history.exam.common.api.Result;
import com.history.exam.student.dto.StartTrainDTO;
import com.history.exam.student.dto.SubmitAnswerDTO;
import com.history.exam.student.service.TrainService;
import com.history.exam.student.vo.StartTrainVO;
import com.history.exam.student.vo.SubmitAnswerVO;
import com.history.exam.student.vo.TrainSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 训练会话与答题接口
 * <p>路由前缀 /student/train，要求 STUDENT 角色（见 SecurityConfig）。</p>
 */
@RestController
@RequestMapping("/student/train")
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    /**
     * 创建训练会话
     *
     * @param dto 创建会话入参
     * @return 会话信息
     */
    @PostMapping("/sessions")
    public Result<StartTrainVO> startSession(@Valid @RequestBody StartTrainDTO dto) {
        return Result.success(trainService.startSession(dto));
    }

    /**
     * 提交单题答案并流式批改
     *
     * @param dto 答案入参
     * @return 批改结果
     */
    @PostMapping("/records")
    public Result<SubmitAnswerVO> submitAnswer(@Valid @RequestBody SubmitAnswerDTO dto) {
        return Result.success(trainService.submitAnswer(dto));
    }

    /**
     * 结束训练会话
     *
     * @param id 会话 ID（路径参数）
     * @return 汇总信息
     */
    @PostMapping("/sessions/{id}/finish")
    public Result<TrainSummaryVO> finishSession(@PathVariable("id") Long id) {
        return Result.success(trainService.finishSession(id));
    }
}
