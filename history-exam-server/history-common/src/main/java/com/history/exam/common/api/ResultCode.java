package com.history.exam.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务响应码枚举
 * <p>0 成功；4xx 客户端错误；5xx 服务端错误；1xxx 业务错误。</p>
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(0, "success"),

    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    // 服务端错误
    INTERNAL_ERROR(500, "服务内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误：1xxx
    WX_LOGIN_FAIL(1001, "微信登录失败"),
    WX_PAY_FAIL(1002, "微信支付下单失败"),
    ORDER_NOT_FOUND(1003, "订单不存在"),
    ORDER_CLOSED(1004, "订单已关闭"),
    SUBSCRIPTION_EXPIRED(1005, "订阅已过期"),
    FREE_LIMIT_EXCEEDED(1006, "今日免费题量已用完"),
    QUESTION_NOT_FOUND(1007, "题目不存在"),
    SESSION_NOT_FOUND(1008, "训练会话不存在"),
    SESSION_FINISHED(1009, "训练会话已结束"),
    STUDENT_NOT_FOUND(1010, "学生信息不存在"),
    DUPLICATE_SUBMIT(1011, "请勿重复提交"),
    SMS_CODE_INVALID(1012, "短信验证码错误"),
    SMS_CODE_EXPIRED(1013, "短信验证码已过期"),
    PHONE_ALREADY_BOUND(1014, "该手机号已被绑定");

    /** 业务码 */
    private final Integer code;

    /** 提示信息 */
    private final String message;
}
