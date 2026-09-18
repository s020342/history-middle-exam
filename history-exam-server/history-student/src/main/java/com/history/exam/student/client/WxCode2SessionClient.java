package com.history.exam.student.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.history.exam.student.config.WxMiniappProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * 微信 code2session 客户端
 * <p>通过 WebClient 调用 https://api.weixin.qq.com/sns/jscode2session，
 * 以 code 换取 openid 与 session_key。</p>
 */
@Slf4j
@Component
public class WxCode2SessionClient {

    /** 固定授权类型 */
    private static final String GRANT_TYPE = "authorization_code";

    /** 复用的 WebClient 实例 */
    private final WebClient webClient;

    /** 小程序配置 */
    private final WxMiniappProperties properties;

    /** JSON 反序列化器 */
    private final ObjectMapper objectMapper;

    /**
     * 构造客户端，初始化 WebClient
     *
     * @param properties 小程序配置
     */
    public WxCode2SessionClient(WxMiniappProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 使用 wx.login 返回的 code 换取 openid 与 session_key
     *
     * @param code 小程序登录 code
     * @return code2session 响应；失败（errcode!=0 或无 openid）返回 null
     */
    public Code2SessionResponse code2Session(String code) {
        // dev 环境 mock：appid 为空且开启 mockLogin 时，跳过微信调用，用 code 派生假 openid
        if (properties.isMockLogin() && (properties.getAppid() == null || properties.getAppid().isEmpty())) {
            log.warn("[mock-login] appid 为空，走 mock 登录路径: code={}", code);
            return buildMockResponse(code);
        }

        String rawBody;
        try {
            // 用 exchangeToMono 手动读取响应体，避免 retrieve() 对微信 200+错误JSON 误抛异常
            rawBody = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.weixin.qq.com")
                            .path("/sns/jscode2session")
                            .queryParam("appid", properties.getAppid())
                            .queryParam("secret", properties.getSecret())
                            .queryParam("js_code", code)
                            .queryParam("grant_type", GRANT_TYPE)
                            .build())
                    .exchangeToMono(response -> response.bodyToMono(String.class))
                    .block();
        } catch (Exception e) {
            log.error("调用微信 code2session 网络异常: code={}", code, e);
            return null;
        }

        if (rawBody == null || rawBody.isEmpty()) {
            log.warn("微信 code2session 返回空响应体");
            return null;
        }

        // 手动反序列化，兼容微信可能返回的非标准 Content-Type
        Code2SessionResponse response;
        try {
            response = objectMapper.readValue(rawBody, Code2SessionResponse.class);
        } catch (Exception e) {
            log.error("微信 code2session 响应体解析失败: body={}", rawBody, e);
            return null;
        }

        // 校验返回：errcode 非 0 或缺 openid 视为失败
        if (response.getErrcode() != null && response.getErrcode() != 0) {
            log.warn("微信 code2session 返回错误: errcode={}, errmsg={}",
                    response.getErrcode(), response.getErrmsg());
            return null;
        }
        if (Optional.ofNullable(response.getOpenid()).orElse("").isEmpty()) {
            log.warn("微信 code2session 返回 openid 为空: body={}", rawBody);
            return null;
        }
        return response;
    }

    /**
     * 构造 mock code2session 响应
     * <p>用 code 的 MD5 派生 32 位 openid，同一 code 映射到同一学生，
     * 便于联调时学生记录稳定不丢失。</p>
     *
     * @param code 小程序登录 code
     * @return mock 响应
     */
    private Code2SessionResponse buildMockResponse(String code) {
        Code2SessionResponse resp = new Code2SessionResponse();
        resp.setOpenid("mock-" + md5Hex(code).substring(0, 28));
        resp.setSessionKey("mock-session-key");
        resp.setErrcode(0);
        resp.setErrmsg("ok");
        return resp;
    }

    /**
     * 计算字符串的 MD5 十六进制摘要
     *
     * @param input 输入字符串
     * @return 32 位小写十六进制摘要
     */
    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5 是 JDK 内置算法，不会发生
            throw new IllegalStateException(e);
        }
    }

    /**
     * code2session 响应体
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Code2SessionResponse {

        /** 微信 openid */
        @JsonProperty("openid")
        private String openid;

        /** 会话密钥，用于解密 encryptedData */
        @JsonProperty("session_key")
        private String sessionKey;

        /** unionid，仅在满足条件时返回 */
        @JsonProperty("unionid")
        private String unionid;

        /** 错误码，0 表示成功 */
        @JsonProperty("errcode")
        private Integer errcode;

        /** 错误信息 */
        @JsonProperty("errmsg")
        private String errmsg;
    }
}
