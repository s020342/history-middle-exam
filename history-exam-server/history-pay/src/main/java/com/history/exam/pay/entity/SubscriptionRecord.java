package com.history.exam.pay.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订阅权益记录实体
 * <p>对应数据库 subscription_record 表，承载学生一次有效订阅的起止时间。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subscription_record")
public class SubscriptionRecord extends BaseEntity {

    /** 学生 ID */
    private Long studentId;

    /** 订单 ID */
    private Long orderId;

    /** 订阅开始时间 */
    private LocalDateTime startAt;

    /** 订阅结束时间 */
    private LocalDateTime endAt;

    /** 状态：0 有效 1 失效 */
    private Integer status;
}
