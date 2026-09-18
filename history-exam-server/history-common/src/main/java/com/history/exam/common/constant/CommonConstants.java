package com.history.exam.common.constant;

/**
 * 公共常量
 * <p>跨模块共享的魔法值集中管理。</p>
 */
public final class CommonConstants {

    private CommonConstants() {
    }

    /** 未删除标记 */
    public static final int NOT_DELETED = 0;

    /** 已删除标记 */
    public static final int DELETED = 1;

    /** 启用状态 */
    public static final int STATUS_NORMAL = 0;

    /** 停用状态 */
    public static final int STATUS_DISABLED = 1;

    /** 上架状态 */
    public static final int STATUS_ONLINE = 0;

    /** 下架状态 */
    public static final int STATUS_OFFLINE = 1;

    /** 题型：选择题 */
    public static final int QUESTION_TYPE_CHOICE = 1;

    /** 题型：判断题 */
    public static final int QUESTION_TYPE_JUDGE = 2;

    /** 训练模式：自由训练 */
    public static final int TRAIN_MODE_FREE = 1;

    /** 训练模式：错题训练 */
    public static final int TRAIN_MODE_WRONG = 2;

    /** 训练模式：复习训练 */
    public static final int TRAIN_MODE_REVIEW = 3;

    /** 训练会话状态：进行中 */
    public static final int SESSION_RUNNING = 0;

    /** 训练会话状态：已完成 */
    public static final int SESSION_FINISHED = 1;

    /** 训练会话状态：已中断 */
    public static final int SESSION_INTERRUPTED = 2;

    /** 订单状态：待支付 */
    public static final int ORDER_PENDING = 0;

    /** 订单状态：已支付 */
    public static final int ORDER_PAID = 1;

    /** 订单状态：已关闭 */
    public static final int ORDER_CLOSED = 2;

    /** 订单状态：已退款 */
    public static final int ORDER_REFUNDED = 3;

    /** 请求头 Authorization 前缀 */
    public static final String AUTHORIZATION_PREFIX = "Bearer ";

    /** 请求头 Authorization */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** 当前登录用户上下文键 */
    public static final String CURRENT_USER_KEY = "currentUser";
}
