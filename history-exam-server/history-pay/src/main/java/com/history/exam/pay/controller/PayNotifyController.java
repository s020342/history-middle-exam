package com.history.exam.pay.controller;

import com.history.exam.common.api.Result;
import com.history.exam.pay.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 微信支付回调 Controller
 * <p>接收微信支付平台异步通知，验签解密后开通订阅权益。
 * 接口在 SecurityConfig 中放行（/pay/notify/**）。</p>
 */
@Slf4j
@RestController
@RequestMapping("/pay/notify")
@RequiredArgsConstructor
public class PayNotifyController {

    private final SubscriptionService subscriptionService;

    /**
     * 微信支付回调入口
     * <p>无论处理结果如何，统一返回 success。处理失败时微信会按策略重试，本服务保持幂等。</p>
     *
     * @param request HTTP 请求
     * @return 微信约定的成功响应
     * @throws IOException 读取请求体失败
     */
    @PostMapping
    public Result<Void> notify(HttpServletRequest request) throws IOException {
        Map<String, String> headers = collectHeaders(request);
        String body = request.getReader().lines()
                .collect(Collectors.joining("\n"));
        try {
            subscriptionService.handlePayNotify(headers, body);
        } catch (Exception e) {
            // 异常时返回 success 会让微信停止重试；改为返回失败触发微信重试
            // 这里返回 fail 让微信按既定策略重试，待本服务幂等保障最终一致
            log.error("微信支付回调处理失败", e);
            throw e;
        }
        return Result.success();
    }

    /**
     * 收集 HTTP 请求头为 Map
     *
     * @param request HTTP 请求
     * @return 头名 → 头值（多值取首个）
     */
    private Map<String, String> collectHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = request.getHeader(name);
            if (value != null) {
                headers.put(name, value);
            }
        }
        return Collections.unmodifiableMap(headers);
    }
}
