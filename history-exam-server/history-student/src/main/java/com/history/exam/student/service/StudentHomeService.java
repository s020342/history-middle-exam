package com.history.exam.student.service;

import com.history.exam.student.vo.HomeProfileVO;

/**
 * 学生首页服务
 * <p>提供首页概况：昵称、订阅状态、今日剩余题量。</p>
 */
public interface StudentHomeService {

    /**
     * 获取当前登录学生的首页概况
     *
     * @param studentId 登录学生 ID
     * @return 首页概况视图
     */
    HomeProfileVO getProfile(Long studentId);
}
