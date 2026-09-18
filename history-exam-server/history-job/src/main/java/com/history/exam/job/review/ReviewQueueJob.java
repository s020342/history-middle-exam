package com.history.exam.job.review;

import com.history.exam.student.entity.Mastery;
import com.history.exam.student.mapper.MasteryMapper;
import com.history.exam.student.mapper.ReviewQueueMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 每日复习队列生成 Job
 * <p>每日 03:00 扫描 mastery.next_review_at <= today AND level &lt; 2 的记录，
 * 按唯一键 uk_student_question_date upsert 到 review_queue，靠唯一键防重复生成；
 * 完成后逐个 evict 涉及学生的 Redis 缓存 student:review:today:{sid}。</p>
 * <p>并发控制：单实例直接 @Scheduled；多实例需 Redisson 分布式锁，
 * V1.1 标注 TODO 留 V1.4 多实例改造。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewQueueJob {

    private final MasteryMapper masteryMapper;
    private final ReviewQueueMapper reviewQueueMapper;
    private final StringRedisTemplate redisTemplate;

    /** 单批扫描上限 */
    private static final int BATCH_SIZE = 500;

    /** Redis Key 前缀：学生当日复习队列缓存 */
    private static final String KEY_REVIEW_TODAY_PREFIX = "student:review:today:";

    /**
     * 每日 03:00 触发：生成当日复习队列
     * <p>cron = "0 0 3 * * ?"（秒 分 时 日 月 周）。</p>
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void generateDailyQueue() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        int scanned = 0;
        int generated = 0;
        // 收集本次 Job 涉及的学生 ID，用于精确 evict 缓存（避免 KEYS/SCAN 阻塞）
        Set<Long> affectedStudentIds = new HashSet<>();
        log.info("ReviewQueueJob start, today={}", today);

        // 分批扫描到期 mastery 记录，每条 upsert 到 review_queue
        List<Mastery> dueList = masteryMapper.selectDueList(today, BATCH_SIZE);
        while (dueList != null && !dueList.isEmpty()) {
            for (Mastery m : dueList) {
                scanned++;
                affectedStudentIds.add(m.getStudentId());
                int affected = reviewQueueMapper.upsertByStudentQuestionDate(
                        m.getStudentId(), m.getQuestionId(), today, 0, now);
                generated += affected;
            }
            // 继续扫下一批；满批时可能重复读已处理记录，靠唯一键 upsert 跳过
            if (dueList.size() < BATCH_SIZE) {
                break;
            }
            dueList = masteryMapper.selectDueList(today, BATCH_SIZE);
        }

        // 精确 evict 涉及学生的当日复习缓存
        for (Long sid : affectedStudentIds) {
            redisTemplate.delete(KEY_REVIEW_TODAY_PREFIX + sid);
        }

        log.info("ReviewQueueJob done, scanned={}, generated={}, evicted={}",
                scanned, generated, affectedStudentIds.size());
    }
}

