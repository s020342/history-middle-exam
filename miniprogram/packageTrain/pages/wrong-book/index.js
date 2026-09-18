// packageTrain/pages/wrong-book/index.js - 错题本
// 分页加载错题列表，点击进入错因选择
import { getWrongBook, submitErrorCause } from '../../../services/api';

Page({
  data: {
    loading: true,
    records: [],
    page: 1,
    size: 20,
    hasMore: true,
    // 错因弹层
    showCauseSheet: false,
    currentRecordId: null,
    causes: [
      { id: 1, code: 'KNOWLEDGE_MISSING', name: '知识缺失' },
      { id: 2, code: 'CONCEPT_CONFUSED', name: '概念混淆' },
      { id: 3, code: 'QUESTION_TRAP', name: '题目陷阱' },
    ],
  },

  /**
   * 页面加载：拉取首页错题
   */
  onLoad() {
    this.loadWrongRecords(true);
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.loadWrongRecords(true).finally(() => wx.stopPullDownRefresh());
  },

  /**
   * 上拉加载更多
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadWrongRecords(false);
    }
  },

  /**
   * 加载错题列表
   * @param {boolean} reset 是否重置到第一页
   */
  async loadWrongRecords(reset) {
    try {
      this.setData({ loading: true });
      const page = reset ? 1 : this.data.page + 1;
      const data = await getWrongBook({ page, size: this.data.size });
      const list = reset ? data.records : this.data.records.concat(data.records);
      this.setData({
        records: list,
        page,
        hasMore: page < data.pages,
      });
    } catch (err) {
      console.error('加载错题失败', err);
    } finally {
      this.setData({ loading: false });
    }
  },

  /**
   * 点击"选错因"：打开底部弹层
   */
  onPickCauseTap(e) {
    this.setData({
      showCauseSheet: true,
      currentRecordId: e.currentTarget.dataset.id,
    });
  },

  /**
   * 选择错因并提交
   */
  async onCauseTap(e) {
    const causeId = Number(e.currentTarget.dataset.id);
    const recordId = this.data.currentRecordId;
    try {
      await submitErrorCause({ recordId, errorCauseId: causeId });
      wx.showToast({ title: '已记录', icon: 'success' });
      this.setData({ showCauseSheet: false });
      this.loadWrongRecords(true);
    } catch (err) {
      console.error('提交错因失败', err);
    }
  },

  /**
   * 弹层内容区点击占位
   * 配合 wxml 中 catchtap 阻止事件冒泡到遮罩层，避免点击弹层正文误触发关闭
   */
  onSheetBodyTap() {
    // 仅拦截冒泡，无业务逻辑
  },

  /**
   * 关闭错因弹层
   */
  onSheetClose() {
    this.setData({ showCauseSheet: false });
  },
});
