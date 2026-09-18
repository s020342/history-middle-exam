// packageTrain/pages/knowledge-card/index.js - 知识点卡片页
// 展示高频知识点卡片列表，点击展开查看卡片正文与配图
import { getHighFreqCards, getKnowledgeDetail } from '../../../services/api';

Page({
  data: {
    loading: true,
    // 卡片列表（不带 cardContent）
    cards: [],
    // 当前展开的卡片 ID（null 全部收起）
    expandedId: null,
    // 当前展开的卡片详情（含 cardContent / cardImage）
    currentCard: null,
  },

  /**
   * 页面加载：拉取高频知识点卡片列表
   */
  onLoad() {
    this.loadCards();
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.loadCards().finally(() => wx.stopPullDownRefresh());
  },

  /**
   * 拉取高频知识点卡片列表
   */
  async loadCards() {
    try {
      this.setData({ loading: true });
      const cards = await getHighFreqCards();
      this.setData({ cards: cards || [] });
    } catch (err) {
      console.error('加载知识点卡片失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  /**
   * 点击卡片：展开/收起；展开时按需拉取详情
   */
  async onCardTap(e) {
    const id = e.currentTarget.dataset.id;
    // 同一张卡片点击：收起
    if (this.data.expandedId === id) {
      this.setData({ expandedId: null, currentCard: null });
      return;
    }
    // 先标记展开态，再按需拉详情
    this.setData({ expandedId: id, currentCard: null });
    try {
      const detail = await getKnowledgeDetail(id);
      this.setData({ currentCard: detail });
    } catch (err) {
      console.error('加载卡片详情失败', err);
      wx.showToast({ title: '加载详情失败', icon: 'none' });
    }
  },
});
