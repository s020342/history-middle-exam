package com.history.exam.student.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 * <p>绑定 application.yml 中 history.wx.miniapp 节点，提供 code2session 所需 appid/secret。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "history.wx.miniapp")
public class WxMiniappProperties {

    /** 小程序 appid */
    private String appid;

    /** 小程序 secret */
    private String secret;

    /**
     * 本地联调 mock 登录开关（仅限 dev 环境使用）。
     * <p>开启后，当 appid 为空时 code2session 将跳过微信调用，用 code 派生假 openid，
     * 便于无微信凭证时本地联调。生产环境必须关闭（默认 false）。</p>
     */
    private boolean mockLogin = false;
}
