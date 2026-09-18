package com.history.exam.pay.controller;

import com.history.exam.common.api.Result;
import com.history.exam.common.context.CurrentUser;
import com.history.exam.common.context.CurrentUserHolder;
import com.history.exam.pay.dto.CreateOrderDTO;
import com.history.exam.pay.service.SubscriptionService;
import com.history.exam.pay.vo.CreateOrderVO;
import com.history.exam.pay.vo.SubscriptionPlanVO;
import com.history.exam.pay.vo.SubscriptionStatusVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订阅 Controller
 * <p>学生端订阅相关接口：方案查询、下单、订阅状态查询。</p>
 */
@RestController
@RequestMapping("/student/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 查询在售订阅方案
     *
     * @return 方案列表
     */
    @GetMapping("/plans")
    public Result<List<SubscriptionPlanVO>> plans() {
        return Result.success(subscriptionService.listPlans());
    }

    /**
     * 创建订阅订单并拉起微信支付
     *
     * @param dto 创建订单入参
     * @return 订单与调起支付签名信息
     */
    @PostMapping("/orders")
    public Result<CreateOrderVO> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        CurrentUser user = CurrentUserHolder.get();
        return Result.success(subscriptionService.createOrder(dto, user.getUserId(), user.getOpenid()));
    }

    /**
     * 查询当前学生订阅状态
     *
     * @return 订阅状态
     */
    @GetMapping("/status")
    public Result<SubscriptionStatusVO> status() {
        Long userId = CurrentUserHolder.getUserId();
        return Result.success(subscriptionService.getSubscriptionStatus(userId));
    }
}
