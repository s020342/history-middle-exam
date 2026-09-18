package com.history.exam.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 登录用户上下文
 * <p>JWT 解析后封装的当前登录用户信息，存入 ThreadLocal 供业务层取用。</p>
 */
@Data
@Builder
@AllArgsConstructor
public class CurrentUser {

    /** 用户 ID（学生 ID 或管理员 ID） */
    private Long userId;

    /** 用户类型 */
    private UserType userType;

    /** 学生 openid（仅学生端） */
    private String openid;

    /** 管理员账号（仅管理端） */
    private String username;

    /** 是否已订阅（仅学生端） */
    private Boolean subscribed;

    /** token 过期时间（毫秒时间戳） */
    private Long expireAt;
}
