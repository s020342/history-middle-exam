package com.history.exam.student.service.impl;

import cn.hutool.json.JSONUtil;
import com.history.exam.student.entity.Question;
import com.history.exam.student.entity.QuestionOption;
import com.history.exam.student.entity.ReviewQueue;
import com.history.exam.student.mapper.QuestionMapper;
import com.history.exam.student.mapper.QuestionOptionMapper;
import com.history.exam.student.mapper.ReviewQueueMapper;
import com.history.exam.student.service.ReviewQueueService;
import com.history.exam.student.vo.QuestionOptionVO;
import com.history.exam.student.vo.QuestionVO;
import com.history.exam.student.vo.ReviewQueueVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 复习队列服务实现
 * <p>学生当日复习队列查询与复习完成标记；命中 Redis 缓存 student:review:today:{sid}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewQueueServiceImpl implements ReviewQueueService {

    private final ReviewQueueMapper reviewQueueMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final StringRedisTemplate redisTemplate;

    /** Redis Key 前缀：学生当日复习队列缓存 */
    private static final String KEY_REVIEW_TODAY = "student:review:today:";

    /**
     * 查询学生今日待复习队列
     *
     * @param studentId 学生 ID
     * @return 今日复习队列
     */
    @Override
    public ReviewQueueVO getTodayQueue(Long studentId) {
        String key = KEY_REVIEW_TODAY + studentId;
        LocalDate today = LocalDate.now();
        // ① 读 Redis
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JSONUtil.toBean(cached, ReviewQueueVO.class);
        }
        // ② 未命中查 DB：先取今日待复习队列记录
        List<ReviewQueue> queue = reviewQueueMapper.selectTodayQueue(studentId, today);
        ReviewQueueVO vo = new ReviewQueueVO();
        if (queue == null || queue.isEmpty()) {
            vo.setDueCount(0);
            vo.setQuestions(Collections.emptyList());
        } else {
            List<Long> questionIds = queue.stream()
                    .map(ReviewQueue::getQuestionId)
                    .collect(Collectors.toList());
            List<QuestionVO> questions = assembleQuestions(questionIds);
            vo.setDueCount(questions.size());
            vo.setQuestions(questions);
        }
        // ③ 回写缓存至当日 23:59:59
        long secondsUntilMidnight = secondsUntilMidnight();
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(vo),
                secondsUntilMidnight, TimeUnit.SECONDS);
        return vo;
    }

    /**
     * 标记某题当日已复习
     *
     * @param studentId  学生 ID
     * @param questionId 题目 ID
     * @param dueDate    应复习日
     */
    @Override
    public void markReviewed(Long studentId, Long questionId, LocalDate dueDate) {
        reviewQueueMapper.markReviewed(studentId, questionId, dueDate);
        redisTemplate.delete(KEY_REVIEW_TODAY + studentId);
    }

    /**
     * 按题目 ID 列表组装题目 VO（含选项）
     * <p>批量查题目 + 循环查选项；V1.1 题量 ≤10，N+1 可接受。</p>
     *
     * @param questionIds 题目 ID 列表
     * @return 题目 VO 列表
     */
    private List<QuestionVO> assembleQuestions(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }
        // 按传入顺序排序
        Map<Long, Question> idToQuestion = new HashMap<>();
        for (Question q : questions) {
            idToQuestion.put(q.getId(), q);
        }
        List<QuestionVO> result = new ArrayList<>(questionIds.size());
        for (Long id : questionIds) {
            Question q = idToQuestion.get(id);
            if (q == null) {
                continue;
            }
            QuestionVO vo = new QuestionVO();
            vo.setId(String.valueOf(q.getId()));
            vo.setType(q.getType());
            vo.setStem(q.getStem());
            vo.setDifficulty(q.getDifficulty());
            vo.setAnswer(q.getAnswer());
            vo.setAnalysis(q.getAnalysis());
            // 查选项
            List<QuestionOption> opts = questionOptionMapper.selectByQuestionId(id);
            List<QuestionOptionVO> optVOs = new ArrayList<>(opts == null ? 0 : opts.size());
            if (opts != null) {
                for (QuestionOption o : opts) {
                    QuestionOptionVO optVO = new QuestionOptionVO();
                    optVO.setId(String.valueOf(o.getId()));
                    optVO.setKey(o.getOptionKey());
                    optVO.setContent(o.getContent());
                    optVOs.add(optVO);
                }
            }
            vo.setOptions(optVOs);
            result.add(vo);
        }
        return result;
    }

    /**
     * 计算到当日 23:59:59 的剩余秒数
     *
     * @return 剩余秒数
     */
    private long secondsUntilMidnight() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return java.time.Duration.between(now, midnight).getSeconds();
    }
}
