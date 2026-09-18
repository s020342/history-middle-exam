package com.history.exam.student.service;

import com.history.exam.student.dto.StartTrainDTO;
import com.history.exam.student.dto.SubmitAnswerDTO;
import com.history.exam.student.vo.StartTrainVO;
import com.history.exam.student.vo.SubmitAnswerVO;
import com.history.exam.student.vo.TrainSummaryVO;

/**
 * 训练会话与答题服务
 */
public interface TrainService {

    /**
     * 创建训练会话
     *
     * @param dto 创建会话入参（mode、questionIds、total）
     * @return 会话信息（sessionId 字符串、mode、total）
     */
    StartTrainVO startSession(StartTrainDTO dto);

    /**
     * 提交单题答案并流式批改
     * <p>校验会话 → 查题比对 → 写答题记录 → 更新会话统计 → 答错写错题 → 更新题目统计。</p>
     *
     * @param dto 答案入参
     * @return 批改结果（isCorrect、correctAnswer、masteryLevel）
     */
    SubmitAnswerVO submitAnswer(SubmitAnswerDTO dto);

    /**
     * 结束训练会话，返回汇总
     *
     * @param sessionId 会话 ID
     * @return 汇总（sessionId、answered、correct）
     */
    TrainSummaryVO finishSession(Long sessionId);
}
