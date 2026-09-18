package com.history.exam.student.vo;

import lombok.Data;

/**
 * 学生登录响应
 * <p>登录成功后返回 JWT token 与学生基本信息。</p>
 */
@Data
public class StudentLoginVO {

    /** JWT token，后续请求带 Authorization: Bearer <token> */
    private String token;

    /** 学生信息 */
    private StudentVO student;
}
