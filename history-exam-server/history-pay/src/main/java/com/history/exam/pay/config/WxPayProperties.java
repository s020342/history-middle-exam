package com.history.exam.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置
 * <p>承载 application.yml 中 history.wx.pay 节点的配置项。
 * 所有密钥均通过环境变量注入（如 WX_PAY_API_V3_KEY），禁止硬编码入库。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "history.wx.pay")
public class WxPayProperties {

    /** 商户号 */
    private String mchId;

    /** APIv3 密钥（用于回调验签解密，从环境变量 WX_PAY_API_V3_KEY 注入） */
    private String apiV3Key;

    /** 商户证书序列号 */
    private String certSerialNo;

    /** 商户私钥 PEM 文件路径（从环境变量 WX_PAY_PRIVATE_KEY_PATH 注入） */
    private String privateKeyPath;

    /** 支付回调地址（HTTPS） */
    private String notifyUrl;
}
