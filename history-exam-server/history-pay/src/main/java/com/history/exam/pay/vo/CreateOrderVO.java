package com.history.exam.pay.vo;

import lombok.Data;

/**
 * 创建订单 VO
 * <p>对应 POST /student/subscription/orders 响应，下发商户订单号、预支付 ID 与小程序调起支付签名。</p>
 */
@Data
public class CreateOrderVO {

    /** 商户订单号 */
    private String orderNo;

    /** 微信预支付 ID */
    private String prepayId;

    /** 小程序调起支付所需签名信息 */
    private PaySignVO paySign;
}
