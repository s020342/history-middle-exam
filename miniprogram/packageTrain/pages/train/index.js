// packageTrain/pages/train/index.js - 自由训练答题页
// 流程：组卷 → 创建会话 → 逐题答题 → 流式批改 → 结束
import { buildQuestions, startTrainSession, submitAnswer, finishTrainSession } from '../../../services/api';
import config from '../../../utils/config';

Page({
  data: {
    mode: 1,
    loading: true,
    currentIndex: 0,
    questions: [],
    sessionId: null,
    selectedAnswer: '',
    answered: false,
    lastResult: null,
    // 批改请求中标记，防止重复点击
    submitting: false,
  },

  /**
   * 页面加载：解析参数并组卷
   */
  onLoad(query) {
    const mode = Number(query.mode) || config.train.typeChoice;
    this.setData({ mode });
    this.initQuestions();
  },

  /**
   * 组卷并创建训练会话
   */
  async initQuestions() {
    try {
      this.setData({ loading: true });
      // 步骤 1：随机组卷（默认 10 题，全部选择题）
      const questions = await buildQuestions({
        type: config.train.typeChoice,
        count: config.train.defaultCount,
      });
      // 步骤 2：创建训练会话
      const session = await startTrainSession({
        mode: this.data.mode,
        questionIds: questions.map((q) => q.id),
        total: questions.length,
      });
      this.setData({
        questions,
        sessionId: session.sessionId,
        currentIndex: 0,
        selectedAnswer: '',
        answered: false,
        lastResult: null,
      });
    } catch (err) {
      console.error('组卷失败', err);
      wx.showToast({ title: '组卷失败，请重试', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  /**
   * 选择选项：记录选中并提交批改
   * <p>点击时只设 selectedAnswer（显示选中态），批改返回后才设 answered=true，
   * 避免 correctAnswer 旧值导致正确答案先闪红再变绿。</p>
   */
  async onOptionTap(e) {
    if (this.data.answered || this.data.submitting) {
      return;
    }
    const { key } = e.detail;
    // 仅设置选中态，answered 保持 false，样式走 option-selected
    this.setData({ selectedAnswer: key, submitting: true });
    const current = this.data.questions[this.data.currentIndex];
    try {
      const result = await submitAnswer({
        sessionId: this.data.sessionId,
        questionId: current.id,
        userAnswer: key,
        durationMs: 0,
      });
      // 批改返回后再标记已作答并设置正确答案，样式一次到位
      this.setData({ answered: true, lastResult: result });
    } catch (err) {
      console.error('提交答案失败', err);
    } finally {
      this.setData({ submitting: false });
    }
  },

  /**
   * 下一题：到达最后一题则结束会话
   */
  onNextTap() {
    const next = this.data.currentIndex + 1;
    if (next >= this.data.questions.length) {
      this.finishSession();
      return;
    }
    this.setData({
      currentIndex: next,
      selectedAnswer: '',
      answered: false,
      lastResult: null,
    });
  },

  /**
   * 结束训练会话：后端汇总后展示统计
   */
  async finishSession() {
    try {
      const summary = await finishTrainSession(this.data.sessionId);
      wx.showModal({
        title: '训练完成',
        content: `共答 ${summary.answered} 题，答对 ${summary.correct} 题`,
        showCancel: false,
        confirmText: '好的',
        success: () => wx.navigateBack(),
      });
    } catch (err) {
      console.error('结束会话失败', err);
      wx.navigateBack();
    }
  },
});
