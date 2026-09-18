// services/api.js - 后端接口封装
// 与后端 docs/04-接口文档.md、docs/13-API详细字段表.md 对齐
import { get, post } from '../utils/request';

/**
 * 微信登录：wx.login 拿 code 后换取 JWT token
 * @param {string} code wx.login 返回的 code
 * @returns {Promise<{token:string, student:Object}>}
 */
export function loginByCode(code) {
  return post('/student/login', { code });
}

/**
 * 查询首页学生概况：订阅状态、当日剩余题量等
 * @returns {Promise<Object>}
 */
export function getHomeProfile() {
  return get('/student/home/profile');
}

/**
 * 题目查询：按考点/题型/随机组卷
 * @param {Object} params - { knowledgeId?, type?, count }
 * @returns {Promise<Array<Object>>}
 */
export function buildQuestions(params) {
  return get('/student/questions/build', params);
}

/**
 * 创建训练会话
 * @param {Object} payload - { mode, questionIds, total }
 * @returns {Promise<{sessionId:number}>}
 */
export function startTrainSession(payload) {
  return post('/student/train/sessions', payload);
}

/**
 * 提交单题答案并获取批改结果
 * @param {Object} payload - { sessionId, questionId, userAnswer, durationMs }
 * @returns {Promise<{isCorrect:boolean, correctAnswer:string, masteryLevel:number}>}
 */
export function submitAnswer(payload) {
  return post('/student/train/records', payload);
}

/**
 * 结束训练会话
 * @param {number} sessionId 会话 ID
 * @returns {Promise<{sessionId:number, answered:number, correct:number}>}
 */
export function finishTrainSession(sessionId) {
  return post(`/student/train/sessions/${sessionId}/finish`);
}

/**
 * 查询错题列表
 * @param {Object} params - { page, size }
 * @returns {Promise<Object>}
 */
export function getWrongBook(params) {
  return get('/student/wrong-records', params);
}

/**
 * 提交错因选择
 * @param {Object} payload - { recordId, errorCauseId }
 * @returns {Promise<void>}
 */
export function submitErrorCause(payload) {
  return post('/student/wrong-records/error-cause', payload);
}

/**
 * 查询订阅方案列表
 * @returns {Promise<Array<Object>>}
 */
export function getSubscriptionPlans() {
  return get('/student/subscription/plans');
}

/**
 * 创建订阅订单（拉起微信支付前调用）
 * @param {Object} payload - { planCode }
 * @returns {Promise<{orderNo:string, prepayId:string, paySign:Object}>}
 */
export function createSubscriptionOrder(payload) {
  return post('/student/subscription/orders', payload);
}

/**
 * 查询当前订阅状态
 * @returns {Promise<{subscribed:boolean, expireAt?:string}>}
 */
export function getSubscriptionStatus() {
  return get('/student/subscription/status');
}
