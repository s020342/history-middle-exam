package com.history.exam.student.service.impl;

import com.history.exam.common.api.ResultCode;
import com.history.exam.common.exception.BusinessException;
import com.history.exam.student.entity.Student;
import com.history.exam.student.mapper.StudentMapper;
import com.history.exam.student.service.StudentHomeService;
import com.history.exam.student.vo.HomeProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 学生首页服务实现
 * <p>查询学生昵称与订阅状态，并基于 subscription_plan 的免费每日题量与今日 train_record 数计算剩余题量。
 * 为避免与其他子代理创建的 Mapper 冲突，subscription_plan / train_record 查询使用 JdbcTemplate 原生 SQL。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentHomeServiceImpl implements StudentHomeService {

    /** 默认每日免费题量（无上架订阅方案时兜底） */
    private static final int DEFAULT_DAILY_LIMIT = 10;

    /** 学生 Mapper */
    private final StudentMapper studentMapper;

    /** JdbcTemplate：用于跨表原生 SQL 查询 */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取首页概况
     *
     * @param studentId 登录学生 ID
     * @return 首页概况视图
     */
    @Override
    public HomeProfileVO getProfile(Long studentId) {
        // 1. 查询学生
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ResultCode.STUDENT_NOT_FOUND);
        }

        // 2. 查询每日免费题量：取任一上架方案，无则兜底 10
        Integer dailyLimit = queryDailyLimit();

        // 3. 查询今日 train_record 数
        int todayAnswered = queryTodayAnswered(studentId);

        // 4. 组装响应
        HomeProfileVO vo = new HomeProfileVO();
        vo.setNickname(student.getNickname());
        vo.setSubscribed(student.getSubscribed() != null && student.getSubscribed() == 1);
        vo.setDailyLimit(dailyLimit);
        vo.setRemainingCount(Math.max(0, dailyLimit - todayAnswered));
        return vo;
    }

    /**
     * 查询上架订阅方案的免费每日题量
     *
     * @return 每日免费题量；无上架方案返回默认值 10
     */
    private Integer queryDailyLimit() {
        Integer limit = jdbcTemplate.queryForObject(
                "SELECT free_daily_limit FROM subscription_plan WHERE status = 0 AND deleted = 0 LIMIT 1",
                Integer.class);
        return limit != null ? limit : DEFAULT_DAILY_LIMIT;
    }

    /**
     * 查询学生今日答题数
     *
     * @param studentId 学生 ID
     * @return 今日 train_record 数
     */
    private int queryTodayAnswered(Long studentId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM train_record WHERE student_id = ? AND DATE(answered_at) = CURDATE() AND deleted = 0",
                Integer.class, studentId);
        return count != null ? count : 0;
    }
}
