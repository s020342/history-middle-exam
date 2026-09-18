package com.history.exam.pay.service.impl;

import com.history.exam.common.api.ResultCode;
import com.history.exam.common.exception.BusinessException;
import com.history.exam.pay.config.WxPayProperties;
import com.history.exam.pay.entity.SubscriptionOrder;
import com.history.exam.pay.service.WxPayService;
import com.history.exam.pay.vo.PaySignVO;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.model.Transaction;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 微信支付服务实现
 * <p>使用 wechatpay-java SDK v3：RSAAutoCertificateConfig 自动加载并刷新平台证书，
 * JsapiServiceExtension 完成统一下单并签名，NotificationParser 完成回调验签解密。</p>
 */
@Slf4j
@Service
public class WxPayServiceImpl implements WxPayService {

    /** 微信支付配置（mchId、apiV3Key、certSerialNo、privateKeyPath、notifyUrl，均由环境变量注入） */
    private final WxPayProperties properties;

    /** 小程序 appid（来自 history.wx.miniapp.appid，回调下单所需） */
    @Value("${history.wx.miniapp.appid:}")
    private String appId;

    /** Spring 资源加载器，用于读取商户私钥 PEM 文件 */
    private final ResourceLoader resourceLoader;

    /** 微信支付 SDK 配置（含商户凭证与平台证书） */
    private Config config;

    /** JSAPI 下单扩展服务 */
    private JsapiServiceExtension jsapiServiceExtension;

    /** 回调通知解析器 */
    private NotificationParser notificationParser;

    /**
     * 构造函数注入依赖
     *
     * @param properties     微信支付配置
     * @param resourceLoader Spring 资源加载器
     */
    public WxPayServiceImpl(WxPayProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 初始化微信支付 SDK 配置
     * <p>从环境变量注入的密钥读取私钥 PEM 内容并构建 RSAAutoCertificateConfig。
     * dev 环境未配置密钥时仅记录日志，待真正调用下单时再校验。</p>
     */
    @PostConstruct
    public void init() {
        // 安全考虑：密钥通过 WxPayProperties 从环境变量注入，不写入代码库
        if (isBlank(properties.getMchId())
                || isBlank(properties.getApiV3Key())
                || isBlank(properties.getCertSerialNo())
                || isBlank(properties.getPrivateKeyPath())) {
            log.warn("微信支付配置不完整，相关功能不可用。请检查 WX_PAY_MCH_ID / WX_PAY_API_V3_KEY / "
                    + "WX_PAY_CERT_SERIAL / WX_PAY_PRIVATE_KEY_PATH 环境变量。");
            return;
        }
        try {
            String privateKey = readPrivateKey(properties.getPrivateKeyPath());
            // RSAAutoCertificateConfig 会自动从微信下载并定期刷新平台证书
            this.config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(properties.getMchId())
                    .privateKey(privateKey)
                    .merchantSerialNumber(properties.getCertSerialNo())
                    .apiV3Key(properties.getApiV3Key())
                    .build();
            this.jsapiServiceExtension = new JsapiServiceExtension.Builder()
                    .config(config)
                    .build();
            this.notificationParser = new NotificationParser((NotificationConfig) config);
            log.info("微信支付 SDK 初始化完成，商户号={}", properties.getMchId());
        } catch (Exception e) {
            // 配置异常时不阻断应用启动，待真正调用时显式失败
            log.error("微信支付 SDK 初始化失败", e);
        }
    }

    /**
     * 统一下单并生成小程序调起支付签名
     *
     * @param order  订阅订单实体
     * @param openid 学生 openid
     * @return 小程序调起支付签名信息
     */
    @Override
    public PaySignVO createOrder(SubscriptionOrder order, String openid) {
        ensureInitialized();
        PrepayRequest request = new PrepayRequest();
        request.setAppid(appId);
        request.setMchid(properties.getMchId());
        request.setDescription("历史中考训练小程序订阅");
        request.setOutTradeNo(order.getOrderNo());
        request.setNotifyUrl(properties.getNotifyUrl());
        // 微信要求 RFC3339 格式，例如 2026-09-17T12:34:56+08:00
        request.setTimeExpire(formatRfc3339(order.getExpireAt()));

        Amount amount = new Amount();
        amount.setTotal(order.getAmountFen());
        amount.setCurrency("CNY");
        request.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);

        try {
            // SDK 内部完成下单 + 计算调起支付签名
            PrepayWithRequestPaymentResponse resp =
                    jsapiServiceExtension.prepayWithRequestPayment(request);
            PaySignVO vo = new PaySignVO();
            vo.setTimeStamp(resp.getTimeStamp());
            vo.setNonceStr(resp.getNonceStr());
            vo.setPackageVal(resp.getPackageVal());
            vo.setSignType(resp.getSignType());
            vo.setPaySign(resp.getPaySign());
            return vo;
        } catch (Exception e) {
            log.error("微信支付下单失败 orderNo={}", order.getOrderNo(), e);
            throw new BusinessException(ResultCode.WX_PAY_FAIL);
        }
    }

    /**
     * 解析微信支付回调通知
     *
     * @param headers 微信回调请求头
     * @param body    微信回调请求体
     * @return 回调解析结果
     */
    @Override
    public NotifyResult parseNotify(Map<String, String> headers, String body) {
        ensureInitialized();
        // 微信回调请求头字段名固定，大小写敏感
        String timestamp = headers.get("Wechatpay-Timestamp");
        String nonce = headers.get("Wechatpay-Nonce");
        String signature = headers.get("Wechatpay-Signature");
        String serial = headers.get("Wechatpay-Serial");
        String signType = headers.get("Wechatpay-Signature-Type");

        RequestParam param = new RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonce)
                .timestamp(timestamp)
                .signature(signature)
                .signType(signType)
                .body(body)
                .build();
        try {
            // 验签 + AES-GCM 解密
            Transaction transaction = notificationParser.parse(param, Transaction.class);
            NotifyResult result = new NotifyResult();
            result.setOrderNo(transaction.getOutTradeNo());
            result.setTransactionId(transaction.getTransactionId());
            result.setTradeState(transaction.getTradeState() == null
                    ? null : transaction.getTradeState().name());
            result.setSuccessTime(transaction.getSuccessTime());
            return result;
        } catch (Exception e) {
            log.error("微信支付回调验签/解密失败", e);
            throw new BusinessException(ResultCode.WX_PAY_FAIL, "回调验签失败");
        }
    }

    /**
     * 校验 SDK 是否已成功初始化
     */
    private void ensureInitialized() {
        if (config == null || jsapiServiceExtension == null || notificationParser == null) {
            throw new BusinessException(ResultCode.WX_PAY_FAIL, "微信支付未正确配置");
        }
    }

    /**
     * 读取商户私钥 PEM 文件为字符串
     * <p>支持 classpath: 前缀与文件系统绝对路径。</p>
     *
     * @param path 私钥路径
     * @return PEM 内容字符串
     */
    private String readPrivateKey(String path) {
        try {
            Resource resource = resourceLoader.getResource(path);
            try (InputStream in = resource.getInputStream()) {
                return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取微信支付商户私钥失败: " + path, e);
        }
    }

    /**
     * 将 LocalDateTime 格式化为 RFC3339 字符串
     *
     * @param time 本地时间
     * @return RFC3339 格式字符串
     */
    private String formatRfc3339(LocalDateTime time) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                time.atZone(ZoneId.of("Asia/Shanghai")));
    }

    /**
     * 判断字符串是否为空
     *
     * @param s 字符串
     * @return true 空；false 非空
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
