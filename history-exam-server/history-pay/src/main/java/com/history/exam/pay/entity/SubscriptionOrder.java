package com.history.exam.pay.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订阅订单实体
 * <p>对应数据库 subscription_order 表，承载商户订单号、金额、状态与微信支付单号。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subscription_order")
public class SubscriptionOrder extends BaseEntity {

    /** 商户订单号（唯一），格式 HE+yyyyMMddHHmmss+4 位随机 */
    private String orderNo;

    /** 学生 ID */
    private Long studentId;

    /** 订阅方案 ID */
    private Long planId;

    /** 支付金额（单位：分） */
    private Integer amountFen;

    /** 订单状态：0 待支付 1 已支付 2 已关闭 3 已退款 */
    private Integer status;

    /** 微信预支付 ID */
    private String prepayId;

    /** 微信支付单号 */
    private String transactionId;

    /** 支付完成时间 */
    private LocalDateTime paidAt;

    /** 订单过期时间（待支付状态下的支付截止） */
    private LocalDateTime expireAt;
}
