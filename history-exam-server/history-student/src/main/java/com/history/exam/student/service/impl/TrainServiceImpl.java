package com.history.exam.student.service.impl;

import com.history.exam.common.api.ResultCode;
import com.history.exam.common.constant.CommonConstants;
import com.history.exam.common.context.CurrentUserHolder;
import com.history.exam.common.exception.BusinessException;
import com.history.exam.student.dto.StartTrainDTO;
import com.history.exam.student.dto.SubmitAnswerDTO;
import com.history.exam.student.entity.TrainRecord;
import com.history.exam.student.entity.TrainSession;
import com.history.exam.student.entity.WrongRecord;
import com.history.exam.student.mapper.QuestionQueryMapper;
import com.history.exam.student.mapper.TrainRecordMapper;
import com.history.exam.student.mapper.TrainSessionMapper;
import com.history.exam.student.mapper.WrongRecordMapper;
import com.history.exam.student.service.MasteryService;
import com.history.exam.student.service.TrainService;
import com.history.exam.student.vo.StartTrainVO;
import com.history.exam.student.vo.SubmitAnswerVO;
import com.history.exam.student.vo.TrainSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 训练会话与答题服务实现
 * <p>V1.1 同步批改：会话校验 → 比对答案 → 写答题记录 → 更新会话统计 →
 * 答错 upsert 错题 → 更新题目作答统计 → 实时刷新掌握度（简化 SM-2）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {

    private final TrainSessionMapper trainSessionMapper;
    private final TrainRecordMapper trainRecordMapper;
    private final WrongRecordMapper wrongRecordMapper;
    private final QuestionQueryMapper questionQueryMapper;
    private final MasteryService masteryService;

    /**
     * 创建训练会话
     * <p>total 取 questionIds.size()；status=进行中；started_at=now。</p>
     *
     * @param dto 创建会话入参
     * @return 会话信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StartTrainVO startSession(StartTrainDTO dto) {
        Long studentId = CurrentUserHolder.getUserId();

        TrainSession session = new TrainSession();
        session.setStudentId(studentId);
        session.setMode(dto.getMode());
        session.setTotal(dto.getQuestionIds().size());
        session.setAnswered(0);
        session.setCorrect(0);
        session.setStatus(CommonConstants.SESSION_RUNNING);
        session.setStartedAt(LocalDateTime.now());
        trainSessionMapper.insert(session);

        StartTrainVO vo = new StartTrainVO();
        vo.setSessionId(String.valueOf(session.getId()));
        vo.setMode(session.getMode());
        vo.setTotal(session.getTotal());
        return vo;
    }

    /**
     * 提交单题答案并流式批改
     *
     * @param dto 答案入参
     * @return 批改结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitAnswerVO submitAnswer(SubmitAnswerDTO dto) {
        Long studentId = CurrentUserHolder.getUserId();

        // ① 校验会话存在且归属当前学生，且状态为进行中
        TrainSession session = trainSessionMapper.selectById(dto.getSessionId());
        if (session == null || !studentId.equals(session.getStudentId())) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (session.getStatus() == null || session.getStatus() != CommonConstants.SESSION_RUNNING) {
            throw new BusinessException(ResultCode.SESSION_FINISHED);
        }

        // ② 查题目正确答案；为空视为题目不存在
        String correctAnswer = questionQueryMapper.selectAnswerById(dto.getQuestionId());
        if (correctAnswer == null) {
            throw new BusinessException(ResultCode.QUESTION_NOT_FOUND);
        }

        // 比对学生答案与正确答案（大小写不敏感）
        boolean isCorrect = correctAnswer.equalsIgnoreCase(dto.getUserAnswer());

        // ③ 写 train_record：is_correct、duration_ms、answered_at=now
        TrainRecord record = new TrainRecord();
        record.setSessionId(session.getId());
        record.setStudentId(studentId);
        record.setQuestionId(dto.getQuestionId());
        record.setUserAnswer(dto.getUserAnswer());
        record.setIsCorrect(isCorrect ? 1 : 0);
        record.setDurationMs(dto.getDurationMs());
        record.setAnsweredAt(LocalDateTime.now());
        trainRecordMapper.insert(record);

        // ④ 更新会话统计：answered++；答对 correct++
        session.setAnswered(session.getAnswered() + 1);
        if (isCorrect) {
            session.setCorrect(session.getCorrect() + 1);
        }
        trainSessionMapper.updateById(session);

        // ⑤ 答错则 upsert 错题记录：error_count+1、last_wrong_at=now、resolved=0
        if (!isCorrect) {
            upsertWrongRecord(studentId, dto.getQuestionId());
        }

        // ⑥ 同步更新题目统计：review_count++、correct_rate 滚动平均
        questionQueryMapper.updateReviewStats(dto.getQuestionId(), isCorrect ? 100 : 0);

        // ⑦ 实时刷新掌握度（简化 SM-2）：在事务内 upsert mastery，失效 Redis 缓存
        int masteryLevel = masteryService.refreshMastery(studentId, dto.getQuestionId(), isCorrect);

        SubmitAnswerVO vo = new SubmitAnswerVO();
        vo.setIsCorrect(isCorrect);
        vo.setCorrectAnswer(correctAnswer);
        vo.setMasteryLevel(masteryLevel);
        return vo;
    }

    /**
     * 结束训练会话：status=FINISHED、finished_at=now；返回汇总
     *
     * @param sessionId 会话 ID
     * @return 汇总（answered、correct）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainSummaryVO finishSession(Long sessionId) {
        Long studentId = CurrentUserHolder.getUserId();

        TrainSession session = trainSessionMapper.selectById(sessionId);
        if (session == null || !studentId.equals(session.getStudentId())) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }

        session.setStatus(CommonConstants.SESSION_FINISHED);
        session.setFinishedAt(LocalDateTime.now());
        trainSessionMapper.updateById(session);

        TrainSummaryVO vo = new TrainSummaryVO();
        vo.setSessionId(String.valueOf(session.getId()));
        vo.setAnswered(session.getAnswered());
        vo.setCorrect(session.getCorrect());
        return vo;
    }

    /**
     * 错题 upsert：已存在则 error_count+1 + last_wrong_at=now；不存在则新增
     *
     * @param studentId  学生 ID
     * @param questionId 题目 ID
     */
    private void upsertWrongRecord(Long studentId, Long questionId) {
        WrongRecord exists = wrongRecordMapper.selectByStudentAndQuestion(studentId, questionId);
        if (exists != null) {
            exists.setErrorCount(exists.getErrorCount() + 1);
            exists.setLastWrongAt(LocalDateTime.now());
            exists.setResolved(0);
            exists.setResolvedAt(null);
            wrongRecordMapper.updateById(exists);
        } else {
            WrongRecord wrong = new WrongRecord();
            wrong.setStudentId(studentId);
            wrong.setQuestionId(questionId);
            wrong.setErrorCount(1);
            wrong.setLastWrongAt(LocalDateTime.now());
            wrong.setResolved(0);
            wrongRecordMapper.insert(wrong);
        }
    }
}
