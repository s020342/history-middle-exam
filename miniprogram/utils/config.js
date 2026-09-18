// utils/config.js - 全局配置
// 后端服务地址与业务相关常量集中管理，避免散落各页

/**
 * 应用配置
 */
const config = {
  // 后端服务地址：开发期本地，生产环境构建前替换为正式域名
  apiBaseUrl: 'http://127.0.0.1:8080',

  // 微信小程序 AppID（构建前需替换）
  appId: 'wx4378cba287f76964',

  // 默认请求超时（毫秒）
  requestTimeout: 15000,

  // 训练相关常量
  train: {
    // 每次自由训练默认题量
    defaultCount: 10,
    // 题型：1 选择 2 判断
    typeChoice: 1,
    typeJudge: 2,
  },

  // 订阅相关常量
  subscription: {
    // 月订阅方案编码
    monthlyCode: 'MONTHLY',
  },

  // 订阅消息模板 ID（订阅消息推送使用）
  templateIds: {
    // 续费提醒模板
    renewRemind: '',
    // 复习提醒模板
    reviewRemind: '',
  },
};

export default config;
