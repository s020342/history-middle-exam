package com.history.exam.pay.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订阅方案 VO
 * <p>对应 GET /student/subscription/plans 响应中的单个方案结构。</p>
 */
@Data
public class SubscriptionPlanVO {

    /** 方案编码 */
    private String code;

    /** 方案名称 */
    private String name;

    /** 价格（元，由 priceFen/100 计算） */
    private BigDecimal priceYuan;

    /** 订阅时长（天） */
    private Integer durationDays;
}
