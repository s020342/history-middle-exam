package com.history.exam.student.service;

import com.history.exam.student.dto.StudentLoginDTO;
import com.history.exam.student.vo.StudentLoginVO;

/**
 * 学生登录服务
 * <p>封装 wx.login code 换取 openid、学生记录的创建与更新、JWT token 生成。</p>
 */
public interface StudentLoginService {

    /**
     * 微信小程序登录
     * <p>流程：code2session 换 openid → 查询/创建学生 → 更新昵称头像 → 签发 JWT。</p>
     *
     * @param dto 登录入参（code、nickname、avatarUrl）
     * @return 登录响应（token + 学生信息）
     */
    StudentLoginVO login(StudentLoginDTO dto);
}
