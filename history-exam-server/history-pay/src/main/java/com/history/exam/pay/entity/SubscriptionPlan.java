package com.history.exam.pay.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订阅方案实体
 * <p>对应数据库 subscription_plan 表，承载订阅价格、时长、免费额度等运营配置。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subscription_plan")
public class SubscriptionPlan extends BaseEntity {

    /** 方案编码（唯一），如 MONTHLY */
    private String code;

    /** 方案名称，如 月订阅 */
    private String name;

    /** 价格（单位：分） */
    private Integer priceFen;

    /** 订阅时长（单位：天） */
    private Integer durationDays;

    /** 免费体验每日题量 */
    private Integer freeDailyLimit;

    /** 状态：0 上架 1 下架 */
    private Integer status;
}
