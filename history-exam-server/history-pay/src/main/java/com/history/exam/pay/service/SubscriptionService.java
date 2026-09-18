package com.history.exam.pay.service;

import com.history.exam.pay.dto.CreateOrderDTO;
import com.history.exam.pay.vo.CreateOrderVO;
import com.history.exam.pay.vo.SubscriptionPlanVO;
import com.history.exam.pay.vo.SubscriptionStatusVO;

import java.util.List;
import java.util.Map;

/**
 * 订阅服务
 * <p>封装订阅方案的查询、订单创建、回调权益开通、订阅状态查询等业务逻辑。</p>
 */
public interface SubscriptionService {

    /**
     * 查询在售订阅方案列表
     *
     * @return 方案 VO 列表
     */
    List<SubscriptionPlanVO> listPlans();

    /**
     * 创建订阅订单并拉起微信支付
     *
     * @param dto    创建订单入参（planCode）
     * @param userId 当前学生 ID
     * @param openid 当前学生 openid
     * @return 创建订单 VO（含商户订单号、prepayId、调起支付签名）
     */
    CreateOrderVO createOrder(CreateOrderDTO dto, Long userId, String openid);

    /**
     * 处理微信支付回调
     * <p>验签解密后更新订单状态、写入订阅权益、刷新学生订阅冗余字段。</p>
     *
     * @param headers 微信回调请求头
     * @param body    微信回调请求体
     */
    void handlePayNotify(Map<String, String> headers, String body);

    /**
     * 查询当前学生订阅状态
     *
     * @param userId 学生 ID
     * @return 订阅状态 VO
     */
    SubscriptionStatusVO getSubscriptionStatus(Long userId);
}
