package com.history.exam.pay.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信支付签名 VO
 * <p>封装小程序 wx.requestPayment 所需的 5 个字段，由后端签名后下发前端调起支付。</p>
 */
@Data
public class PaySignVO {

    /** 时间戳（秒，字符串形式） */
    private String timeStamp;

    /** 随机字符串 */
    private String nonceStr;

    /** 订单详情扩展字符串，格式 prepay_id=xxx */
    @JsonProperty("package")
    private String packageVal;

    /** 签名类型，固定 RSA */
    private String signType;

    /** 签名 */
    private String paySign;
}
