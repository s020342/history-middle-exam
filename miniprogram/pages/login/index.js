// pages/login/index.js - 登录页
// wx.login 拿 code → 后端 code2session → 拿 JWT token → 跳首页
import { loginByCode } from '../../services/api';
import { setToken, setStudentInfo, isAuthenticated } from '../../utils/auth';

Page({
  data: {
    loading: false,
  },

  /**
   * 页面加载：若已登录直接跳首页，避免重复登录
   */
  onLoad() {
    if (isAuthenticated()) {
      wx.switchTab({ url: '/pages/index/index' });
    }
  },

  /**
   * 微信一键登录：拉起 wx.login 拿 code，换取 JWT
   */
  async onLoginTap() {
    if (this.data.loading) {
      return;
    }
    this.setData({ loading: true });
    try {
      // 步骤 1：拿微信 code
      const { code } = await wx.login();
      if (!code) {
        wx.showToast({ title: '微信登录失败', icon: 'none' });
        return;
      }
      // 步骤 2：用 code 换 JWT 与学生信息
      const data = await loginByCode(code);
      setToken(data.token);
      setStudentInfo(data.student);
      wx.showToast({ title: '登录成功', icon: 'success' });
      // 步骤 3：跳转首页（tabBar 页用 switchTab）
      setTimeout(() => {
        wx.switchTab({ url: '/pages/index/index' });
      }, 300);
    } catch (err) {
      // 异常已在 request 层 toast，这里仅日志
      console.error('登录失败', err);
    } finally {
      this.setData({ loading: false });
    }
  },

  /**
   * 跳过登录浏览（仅免费体验）
   */
  onSkipTap() {
    wx.switchTab({ url: '/pages/index/index' });
  },
});
