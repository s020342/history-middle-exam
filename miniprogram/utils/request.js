// utils/request.js - 全局请求封装
// 统一：注入 token、统一错误处理、统一响应结构 {code,message,data}

import config from './config';
import { getToken, clearAuth } from './auth';

/**
 * 发起请求
 * @param {Object} options - 请求配置
 * @param {string} options.url - 接口路径（相对路径，会拼接 apiBaseUrl）
 * @param {string} [options.method='GET'] - 请求方法
 * @param {Object} [options.data] - 请求参数（query 或 body）
 * @param {boolean} [options.needToken=true] - 是否需要注入 token
 * @param {number} [options.timeout] - 超时毫秒，默认走 config
 * @returns {Promise<Object>} 后端 data 字段内容
 */
function request(options) {
  const {
    url,
    method = 'GET',
    data = {},
    needToken = true,
    timeout,
  } = options;

  // 组装请求头
  const header = {
    'Content-Type': 'application/json',
  };
  if (needToken) {
    const token = getToken();
    if (token) {
      header['Authorization'] = `Bearer ${token}`;
    }
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${config.apiBaseUrl}${url}`,
      method,
      data,
      header,
      timeout: timeout || config.requestTimeout,
      success(res) {
        // HTTP 层失败直接抛错
        if (res.statusCode < 200 || res.statusCode >= 300) {
          showToast(`网络异常 (${res.statusCode})`);
          reject(new Error(`HTTP ${res.statusCode}`));
          return;
        }
        const body = res.data || {};
        // 业务层：code 非 0 视为失败
        if (body.code !== 0) {
          // 401：未登录或 token 过期，清理并跳登录页
          if (body.code === 401) {
            clearAuth();
            redirectToLogin();
          }
          showToast(body.message || '请求失败');
          reject(new Error(body.message || `code=${body.code}`));
          return;
        }
        resolve(body.data);
      },
      fail(err) {
        showToast('网络连接失败，请稍后重试');
        reject(err);
      },
    });
  });
}

/**
 * 简易 toast：避免每个页面重复 wx.showToast 调用
 * @param {string} message 提示文案
 */
function showToast(message) {
  wx.showToast({
    title: message,
    icon: 'none',
    duration: 2000,
  });
}

/**
 * 401 跳登录页：避免页面栈堆积
 */
function redirectToLogin() {
  // 防抖：避免并发请求触发多次跳转
  if (redirectToLogin.redirecting) {
    return;
  }
  redirectToLogin.redirecting = true;
  wx.reLaunch({
    url: '/pages/login/index',
    complete() {
      redirectToLogin.redirecting = false;
    },
  });
}

// 便捷快捷方法
export const get = (url, data, options) =>
  request({ url, method: 'GET', data, ...options });
export const post = (url, data, options) =>
  request({ url, method: 'POST', data, ...options });
export const put = (url, data, options) =>
  request({ url, method: 'PUT', data, ...options });
export const del = (url, data, options) =>
  request({ url, method: 'DELETE', data, ...options });

export default request;
