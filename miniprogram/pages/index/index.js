// pages/index/index.js - 首页
// 训练入口：展示订阅状态与当日剩余题量，点击进入自由训练
import { getHomeProfile } from '../../services/api';
import { isAuthenticated } from '../../utils/auth';

Page({
  data: {
    loading: true,
    subscribed: false,
    remainingCount: 0,
    dailyLimit: 10,
    nickname: '',
  },

  /**
   * 页面加载：未登录跳转登录页
   */
  onLoad() {
    if (!isAuthenticated()) {
      wx.reLaunch({ url: '/pages/login/index' });
      return;
    }
  },

  /**
   * 页面显示：刷新首页概况
   */
  onShow() {
    if (isAuthenticated()) {
      this.loadProfile();
    }
  },

  /**
   * 拉取首页概况：订阅状态、剩余题量、昵称
   */
  async loadProfile() {
    try {
      this.setData({ loading: true });
      const data = await getHomeProfile();
      this.setData({
        subscribed: data.subscribed,
        remainingCount: data.remainingCount,
        dailyLimit: data.dailyLimit,
        nickname: data.nickname || '同学',
      });
    } catch (err) {
      console.error('加载首页概况失败', err);
    } finally {
      this.setData({ loading: false });
    }
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.loadProfile().finally(() => wx.stopPullDownRefresh());
  },

  /**
   * 点击"开始自由训练"：跳转训练分包
   */
  onTrainTap() {
    wx.navigateTo({
      url: '/packageTrain/pages/train/index?mode=1',
    });
  },

  /**
   * 点击"错题本"
   */
  onWrongBookTap() {
    wx.navigateTo({
      url: '/packageTrain/pages/wrong-book/index',
    });
  },

  /**
   * 点击"订阅"
   */
  onSubscribeTap() {
    wx.navigateTo({
      url: '/packagePay/pages/subscription/index',
    });
  },
});
