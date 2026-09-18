// packagePay/pages/subscription/index.js - 订阅页
// 拉取方案 → 创建订单 → 拉起微信支付 → 回首页
import { getSubscriptionPlans, createSubscriptionOrder } from '../../../services/api';
import config from '../../../utils/config';

Page({
  data: {
    loading: true,
    plans: [],
    selectedPlanCode: config.subscription.monthlyCode,
  },

  /**
   * 页面加载：拉取订阅方案
   */
  onLoad() {
    this.loadPlans();
  },

  /**
   * 拉取订阅方案
   */
  async loadPlans() {
    try {
      this.setData({ loading: true });
      const plans = await getSubscriptionPlans();
      this.setData({ plans });
    } catch (err) {
      console.error('加载订阅方案失败', err);
    } finally {
      this.setData({ loading: false });
    }
  },

  /**
   * 选择方案
   */
  onPlanTap(e) {
    this.setData({ selectedPlanCode: e.currentTarget.dataset.code });
  },

  /**
   * 立即订阅：创建订单并拉起支付
   */
  async onSubscribeTap() {
    try {
      wx.showLoading({ title: '正在创建订单' });
      const order = await createSubscriptionOrder({
        planCode: this.data.selectedPlanCode,
      });
      wx.hideLoading();
      // 调用微信支付
      await wx.requestPayment({
        timeStamp: order.paySign.timeStamp,
        nonceStr: order.paySign.nonceStr,
        package: order.paySign.package,
        signType: order.paySign.signType,
        paySign: order.paySign.paySign,
      });
      wx.showToast({ title: '订阅成功', icon: 'success' });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/index/index' });
      }, 500);
    } catch (err) {
      wx.hideLoading();
      // 用户取消支付不报错
      if (err && err.errMsg && err.errMsg.includes('cancel')) {
        return;
      }
      console.error('订阅失败', err);
      wx.showToast({ title: '订阅失败', icon: 'none' });
    }
  },

  /**
   * 跳转协议页
   */
  onAgreementTap() {
    // 占位：V1.1 起接入用户协议与隐私政策页面
  },
});
