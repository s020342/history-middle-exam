package com.history.exam.pay.service;

import com.history.exam.pay.entity.SubscriptionOrder;
import com.history.exam.pay.vo.PaySignVO;

import java.util.Map;

/**
 * 微信支付服务
 * <p>封装微信支付 SDK v3 的统一下单与回调验签解密能力，供订阅服务调用。</p>
 */
public interface WxPayService {

    /**
     * 统一下单并生成小程序调起支付签名
     * <p>调用 JsapiServiceExtension.prepayWithRequestPayment 拿到 prepayId 与 paySign 等字段。
     * 商户号、回调地址、私钥等敏感配置均由 WxPayProperties 从环境变量注入。</p>
     *
     * @param order  订阅订单实体（提供订单号、金额、过期时间）
     * @param openid 学生 openid（JSAPI 支付必填）
     * @return 小程序调起支付所需签名信息（包含 prepayId，可从 packageVal 提取）
     */
    PaySignVO createOrder(SubscriptionOrder order, String openid);

    /**
     * 解析微信支付回调通知
     * <p>使用 NotificationParser 验签并解密 resource.ciphertext，得到商户订单号、微信支付单号、交易状态。</p>
     *
     * @param headers 微信回调请求头（Wechatpay-Timestamp / Nonce / Serial / Signature / Signature-Type）
     * @param body    微信回调请求体（原始 JSON 字符串）
     * @return 回调解析结果
     */
    NotifyResult parseNotify(Map<String, String> headers, String body);

    /**
     * 微信回调解析结果
     * <p>承载验签解密后从 Transaction 中提取的关键字段。</p>
     */
    class NotifyResult {

        /** 商户订单号 */
        private String orderNo;

        /** 微信支付单号 */
        private String transactionId;

        /** 交易状态：SUCCESS / REFUND / NOTPAY / CLOSED / REVOKED / USERPAYING / PAYERROR */
        private String tradeState;

        /** 支付完成时间（RFC3339 字符串） */
        private String successTime;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getTradeState() {
            return tradeState;
        }

        public void setTradeState(String tradeState) {
            this.tradeState = tradeState;
        }

        public String getSuccessTime() {
            return successTime;
        }

        public void setSuccessTime(String successTime) {
            this.successTime = successTime;
        }
    }
}
