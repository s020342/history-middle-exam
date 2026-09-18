package com.history.exam.pay.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订阅状态 VO
 * <p>对应 GET /student/subscription/status 响应，下发当前学生订阅是否有效与到期时间。</p>
 */
@Data
public class SubscriptionStatusVO {

    /** 是否订阅中 */
    private Boolean subscribed;

    /** 订阅到期时间 */
    private LocalDateTime expireAt;
}
