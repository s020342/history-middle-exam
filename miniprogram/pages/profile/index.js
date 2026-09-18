// pages/profile/index.js - 我的
// 展示学生信息、订阅状态、退出登录入口
import { getSubscriptionStatus } from '../../services/api';
import { getStudentInfo, clearAuth } from '../../utils/auth';

Page({
  data: {
    student: null,
    subscribed: false,
    expireAt: '',
  },

  /**
   * 页面显示：刷新学生信息与订阅状态
   */
  onShow() {
    const student = getStudentInfo();
    this.setData({ student });
    this.loadSubscription();
  },

  /**
   * 拉取订阅状态
   */
  async loadSubscription() {
    try {
      const data = await getSubscriptionStatus();
      this.setData({
        subscribed: data.subscribed,
        expireAt: data.expireAt || '',
      });
    } catch (err) {
      console.error('查询订阅状态失败', err);
    }
  },

  /**
   * 跳转订阅页
   */
  onSubscribeTap() {
    wx.navigateTo({
      url: '/packagePay/pages/subscription/index',
    });
  },

  /**
   * 退出登录：清理本地缓存并跳登录页
   */
  onLogoutTap() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      confirmColor: '#3a7bd5',
      success: (res) => {
        if (res.confirm) {
          clearAuth();
          wx.reLaunch({ url: '/pages/login/index' });
        }
      },
    });
  },
});
