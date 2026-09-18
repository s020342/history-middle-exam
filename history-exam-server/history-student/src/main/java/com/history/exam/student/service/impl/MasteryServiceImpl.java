package com.history.exam.student.service.impl;

import cn.hutool.json.JSONUtil;
import com.history.exam.student.entity.Mastery;
import com.history.exam.student.mapper.MasteryMapper;
import com.history.exam.student.mapper.QuestionKnowledgeMapper;
import com.history.exam.student.service.MasteryService;
import com.history.exam.student.vo.MasterySummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 掌握度服务实现（简化 SM-2 间隔复习算法）
 * <p>算法要点：
 * <ul>
 *   <li>level 0 未掌握 → +1d 复习；level 1 部分 → +3d；level 2 已掌握 → +7d</li>
 *   <li>答错：consecutiveCorrect 归零，level 降一档（最低 0），nextReviewAt = 明日</li>
 *   <li>答对：consecutiveCorrect++；达 3 升一档（最高 2）并重置计数器</li>
 * </ul>
 * 不依赖 TrainService，避免循环依赖。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MasteryServiceImpl implements MasteryService {

    private final MasteryMapper masteryMapper;
    private final QuestionKnowledgeMapper questionKnowledgeMapper;
    private final StringRedisTemplate redisTemplate;

    /** Redis Key 前缀：学生掌握度概览缓存 */
    private static final String KEY_MASTERY_SUMMARY = "student:mastery:";

    /** 缓存 TTL：1 小时 */
    private static final long TTL_SUMMARY_HOURS = 1L;

    /** 升级阈值：连续答对 3 次升一档 */
    private static final int UPGRADE_THRESHOLD = 3;

    /** 最大等级 */
    private static final int MAX_LEVEL = 2;

    /** 最小等级 */
    private static final int MIN_LEVEL = 0;

    /**
     * 刷新学生单题掌握度（简化 SM-2）
     *
     * @param studentId  学生 ID
     * @param questionId 题目 ID
     * @param isCorrect  本次是否答对
     * @return 最新掌握度等级（0/1/2）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshMastery(Long studentId, Long questionId, boolean isCorrect) {
        // ① 查现有掌握度记录（可能不存在）
        Mastery mastery = masteryMapper.selectByStudentAndQuestion(studentId, questionId);
        int currentLevel = (mastery == null) ? MIN_LEVEL : safeLevel(mastery.getLevel());
        int consecutiveCorrect = (mastery == null) ? 0 : safeCorrect(mastery.getConsecutiveCorrect());

        // ② 按题目反查主考点 knowledge_id（可空）
        Long knowledgeId = questionKnowledgeMapper.selectPrimaryKnowledgeIdByQuestionId(questionId);

        // ③ 计算新 level / consecutiveCorrect / nextReviewAt
        int newLevel;
        int newConsecutive;
        LocalDate nextReviewAt;

        if (isCorrect) {
            // 答对：连续答对 +1
            newConsecutive = consecutiveCorrect + 1;
            newLevel = currentLevel;
            if (newConsecutive >= UPGRADE_THRESHOLD && newLevel < MAX_LEVEL) {
                // 达阈值且未封顶：升一档并重置计数器
                newLevel = currentLevel + 1;
                newConsecutive = 0;
            }
            nextReviewAt = LocalDate.now().plusDays(intervalDays(newLevel));
        } else {
            // 答错：计数器归零，level 降一档（最低 0），明日复习
            newConsecutive = 0;
            newLevel = Math.max(MIN_LEVEL, currentLevel - 1);
            nextReviewAt = LocalDate.now().plusDays(1);
        }

        // ④ upsert 写回
        masteryMapper.upsertByStudentQuestion(studentId, questionId, knowledgeId,
                newLevel, newConsecutive, nextReviewAt);

        // ⑤ 失效学生掌握度概览缓存
        redisTemplate.delete(KEY_MASTERY_SUMMARY + studentId);

        return newLevel;
    }

    /**
     * 查询学生掌握度概览（命中 Redis 缓存）
     *
     * @param studentId 学生 ID
     * @return 掌握度概览
     */
    @Override
    public MasterySummaryVO getStudentSummary(Long studentId) {
        String key = KEY_MASTERY_SUMMARY + studentId;
        // ① 先读 Redis
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JSONUtil.toBean(cached, MasterySummaryVO.class);
        }
        // ② 未命中查 DB 聚合
        List<MasterySummaryVO.KnowledgeMasteryItem> items =
                masteryMapper.selectStudentSummary(studentId);
        MasterySummaryVO vo = buildSummary(items);
        // ③ 回写缓存
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(vo),
                TTL_SUMMARY_HOURS, TimeUnit.HOURS);
        return vo;
    }

    /**
     * 按 level 计算复习间隔天数
     *
     * @param level 掌握度等级
     * @return 间隔天数（1/3/7）
     */
    private int intervalDays(int level) {
        switch (level) {
            case 1:  return 3;
            case 2:  return 7;
            default: return 1;
        }
    }

    /**
     * 安全读取 level，防 null
     */
    private int safeLevel(Integer level) {
        return level == null ? MIN_LEVEL : level;
    }

    /**
     * 安全读取 consecutiveCorrect，防 null
     */
    private int safeCorrect(Integer correct) {
        return correct == null ? 0 : correct;
    }

    /**
     * 由知识点明细列表聚合为概览 VO
     *
     * @param items 知识点维度明细
     * @return 概览 VO
     */
    private MasterySummaryVO buildSummary(List<MasterySummaryVO.KnowledgeMasteryItem> items) {
        MasterySummaryVO vo = new MasterySummaryVO();
        vo.setItems(items);
        vo.setTotalKnowledge(items == null ? 0 : items.size());
        int mastered = 0, partial = 0, unMastered = 0;
        if (items != null) {
            for (MasterySummaryVO.KnowledgeMasteryItem it : items) {
                Integer lvl = it.getLevel();
                if (lvl != null && lvl == 2) {
                    mastered++;
                } else if (lvl != null && lvl == 1) {
                    partial++;
                } else {
                    unMastered++;
                }
            }
        }
        vo.setMasteredCount(mastered);
        vo.setPartialCount(partial);
        vo.setUnMasteredCount(unMastered);
        return vo;
    }
}
