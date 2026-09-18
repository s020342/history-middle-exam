package com.history.exam.common.context;

/**
 * 当前登录用户 ThreadLocal 上下文
 * <p>请求拦截器解析 JWT 后写入，Service 层取用，请求结束清理。</p>
 */
public final class CurrentUserHolder {

    private CurrentUserHolder() {
    }

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    /**
     * 写入当前登录用户
     *
     * @param user 登录用户
     */
    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    /**
     * 读取当前登录用户
     *
     * @return 登录用户；未登录返回 null
     */
    public static CurrentUser get() {
        return HOLDER.get();
    }

    /**
     * 读取当前登录用户 ID
     *
     * @return 用户 ID；未登录返回 null
     */
    public static Long getUserId() {
        CurrentUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    /**
     * 读取当前登录用户类型
     *
     * @return 用户类型；未登录返回 null
     */
    public static UserType getUserType() {
        CurrentUser user = HOLDER.get();
        return user == null ? null : user.getUserType();
    }

    /**
     * 清理当前线程登录用户，避免内存泄漏
     */
    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 是否已登录
     *
     * @return true 已登录；false 未登录
     */
    public static boolean isAuthenticated() {
        return HOLDER.get() != null;
    }

    /**
     * 是否为管理员
     *
     * @return true 是管理员；false 否
     */
    public static boolean isAdmin() {
        return UserType.ADMIN.equals(getUserType());
    }

    /**
     * 是否为学生
     *
     * @return true 是学生；false 否
     */
    public static boolean isStudent() {
        return UserType.STUDENT.equals(getUserType());
    }
}
