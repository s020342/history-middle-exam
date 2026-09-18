// utils/auth.js - 登录态本地缓存读写
// token / 学生信息持久化到 wx.storage；退出登录时统一清理

const TOKEN_KEY = 'history_token';
const STUDENT_KEY = 'history_student';

/**
 * 读取本地 token
 * @returns {string|null} JWT token，未登录返回 null
 */
export function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || null;
}

/**
 * 写入本地 token
 * @param {string} token JWT token
 */
export function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token);
}

/**
 * 读取本地学生信息
 * @returns {Object|null} 学生信息对象
 */
export function getStudentInfo() {
  const raw = wx.getStorageSync(STUDENT_KEY);
  return raw ? JSON.parse(raw) : null;
}

/**
 * 写入本地学生信息
 * @param {Object} student 学生信息
 */
export function setStudentInfo(student) {
  wx.setStorageSync(STUDENT_KEY, JSON.stringify(student));
}

/**
 * 清理登录态：token 与学生信息一并清除
 */
export function clearAuth() {
  wx.removeStorageSync(TOKEN_KEY);
  wx.removeStorageSync(STUDENT_KEY);
}

/**
 * 判断是否已登录
 * @returns {boolean} true 已登录；false 未登录
 */
export function isAuthenticated() {
  return !!getToken();
}
