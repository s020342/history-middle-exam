// app.js - 小程序入口
// 全局状态与启动逻辑：检查登录态、注入全局配置
import { getToken } from './utils/auth';

/**
 * 小程序应用实例
 */
App({
  globalData: {
    // 后端服务地址，开发期直连本地；生产通过 env.config 注入
    apiBaseUrl: 'http://127.0.0.1:8080',
    // 登录令牌，onLaunch 时从本地缓存恢复，request 封装优先读此处
    token: '',
    // 学生基础信息，避免频繁调用 wx.getUserInfo
    userInfo: null,
    // 是否已订阅（首页/训练页判断是否需要拉起支付）
    subscribed: false,
  },

  /**
   * 应用启动：检查本地登录态，过期则清理
   */
  onLaunch() {
    const token = getToken();
    if (token) {
      // 简单检查 token 是否仍存在；具体过期由后端返回 401 时触发清理
      this.globalData.token = token;
    }
  },

  /**
   * 应用显示：从后台切回前台时刷新订阅状态
   */
  onShow() {
    // 占位：后续 V1.1 起拉取最新订阅状态
  },

  /**
   * 应用隐藏：进入后台
   */
  onHide() {
    // 占位：暂停定时器等
  },
});
