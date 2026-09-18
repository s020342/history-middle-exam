package com.history.exam.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.history.exam.common.api.ResultCode;
import com.history.exam.common.constant.CommonConstants;
import com.history.exam.common.exception.BusinessException;
import com.history.exam.pay.dto.CreateOrderDTO;
import com.history.exam.pay.entity.SubscriptionOrder;
import com.history.exam.pay.entity.SubscriptionPlan;
import com.history.exam.pay.entity.SubscriptionRecord;
import com.history.exam.pay.mapper.SubscriptionOrderMapper;
import com.history.exam.pay.mapper.SubscriptionPlanMapper;
import com.history.exam.pay.mapper.SubscriptionRecordMapper;
import com.history.exam.pay.service.SubscriptionService;
import com.history.exam.pay.service.WxPayService;
import com.history.exam.pay.vo.CreateOrderVO;
import com.history.exam.pay.vo.PaySignVO;
import com.history.exam.pay.vo.SubscriptionPlanVO;
import com.history.exam.pay.vo.SubscriptionStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订阅服务实现
 * <p>串起订阅方案查询、订单创建、微信支付下单、回调权益开通、订阅状态查询全流程。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    /** 订单号前缀 */
    private static final String ORDER_NO_PREFIX = "HE";

    /** 订单支付有效期（分钟） */
    private static final int ORDER_EXPIRE_MINUTES = 5;

    /** 订单号插入冲突时最大重试次数 */
    private static final int ORDER_NO_RETRY_MAX = 1;

    /** 微信支付交易成功状态 */
    private static final String TRADE_STATE_SUCCESS = "SUCCESS";

    private final SubscriptionPlanMapper planMapper;
    private final SubscriptionOrderMapper orderMapper;
    private final SubscriptionRecordMapper recordMapper;
    private final WxPayService wxPayService;

    /** 直接 UPDATE/SELECT student 表，避免与其他子代理在 StudentMapper 上冲突 */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 查询在售订阅方案列表
     *
     * @return 方案 VO 列表
     */
    @Override
    public List<SubscriptionPlanVO> listPlans() {
        LambdaQueryWrapper<SubscriptionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubscriptionPlan::getStatus, CommonConstants.STATUS_NORMAL)
                .eq(SubscriptionPlan::getDeleted, CommonConstants.NOT_DELETED)
                .orderByAsc(SubscriptionPlan::getId);
        List<SubscriptionPlan> plans = planMapper.selectList(wrapper);
        return plans.stream().map(this::toPlanVO).toList();
    }

    /**
     * 创建订阅订单并拉起微信支付
     * <p>按 planCode 查方案 → 生成订单号 → 插入订单（uk_order_no 兜底重试一次）→
     * 调微信下单拿 prepayId+paySign → 回写 prepayId → 返回 CreateOrderVO。</p>
     *
     * @param dto    创建订单入参
     * @param userId 学生 ID
     * @param openid 学生 openid
     * @return 创建订单 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderVO createOrder(CreateOrderDTO dto, Long userId, String openid) {
        SubscriptionPlan plan = loadPlanByCode(dto.getPlanCode());

        SubscriptionOrder order = buildOrder(plan, userId);

        // uk_order_no 唯一索引兜底；插入失败重试一次
        insertOrderWithRetry(order);

        PaySignVO paySign = wxPayService.createOrder(order, openid);
        // 从 packageVal（格式 prepay_id=xxx）提取 prepayId 回写订单
        String prepayId = extractPrepayId(paySign.getPackageVal());

        SubscriptionOrder update = new SubscriptionOrder();
        update.setId(order.getId());
        update.setPrepayId(prepayId);
        orderMapper.updateById(update);

        CreateOrderVO vo = new CreateOrderVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setPrepayId(prepayId);
        vo.setPaySign(paySign);
        return vo;
    }

    /**
     * 处理微信支付回调
     * <p>验签解密 → 查订单（不存在直接返回避免重复通知）→ 已 PAID 跳过 →
     * 更新订单为已支付 → 写订阅权益记录 → 更新 student 订阅冗余字段。</p>
     *
     * @param headers 微信回调请求头
     * @param body    微信回调请求体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePayNotify(Map<String, String> headers, String body) {
        WxPayService.NotifyResult notify = wxPayService.parseNotify(headers, body);

        // 仅处理支付成功；其他状态（如 CLOSED）暂不处理，避免影响幂等
        if (!TRADE_STATE_SUCCESS.equals(notify.getTradeState())) {
            log.info("收到非 SUCCESS 微信回调 tradeState={} orderNo={}",
                    notify.getTradeState(), notify.getOrderNo());
            return;
        }

        SubscriptionOrder order = orderMapper.selectOne(new LambdaQueryWrapper<SubscriptionOrder>()
                .eq(SubscriptionOrder::getOrderNo, notify.getOrderNo())
                .last("LIMIT 1"));
        // 订单不存在：返回成功避免微信持续重试
        if (order == null) {
            log.warn("微信回调对应订单不存在 orderNo={}", notify.getOrderNo());
            return;
        }
        // 已支付：幂等跳过
        if (CommonConstants.ORDER_PAID == order.getStatus()) {
            log.info("订单已支付，跳过回调 orderNo={}", order.getOrderNo());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // 更新订单：已支付 + paid_at + transaction_id
        SubscriptionOrder update = new SubscriptionOrder();
        update.setId(order.getId());
        update.setStatus(CommonConstants.ORDER_PAID);
        update.setTransactionId(notify.getTransactionId());
        update.setPaidAt(now);
        orderMapper.updateById(update);

        // 查方案拿到订阅时长
        SubscriptionPlan plan = planMapper.selectById(order.getPlanId());
        if (plan == null) {
            log.error("订单关联方案不存在 planId={}", order.getPlanId());
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        LocalDateTime endAt = now.plusDays(plan.getDurationDays());
        // 写订阅权益记录
        SubscriptionRecord record = new SubscriptionRecord();
        record.setStudentId(order.getStudentId());
        record.setOrderId(order.getId());
        record.setStartAt(now);
        record.setEndAt(endAt);
        record.setStatus(CommonConstants.STATUS_NORMAL);
        recordMapper.insert(record);

        // 更新 student 表冗余订阅字段（用 JdbcTemplate 避免与其他子代理冲突）
        jdbcTemplate.update(
                "UPDATE student SET subscribed = 1, subscribed_until = ? WHERE id = ? AND deleted = 0",
                endAt, order.getStudentId());
        log.info("订阅权益开通完成 studentId={} endAt={}", order.getStudentId(), endAt);
    }

    /**
     * 查询当前学生订阅状态
     *
     * @param userId 学生 ID
     * @return 订阅状态 VO
     */
    @Override
    public SubscriptionStatusVO getSubscriptionStatus(Long userId) {
        SubscriptionStatusVO vo = new SubscriptionStatusVO();
        // 直接查 student 表避免与其他子代理冲突
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT subscribed_until FROM student WHERE id = ? AND deleted = 0",
                userId);
        if (rows.isEmpty()) {
            vo.setSubscribed(false);
            return vo;
        }
        Map<String, Object> row = rows.get(0);
        Object untilObj = row.get("subscribed_until");
        LocalDateTime expireAt = untilObj == null
                ? null
                : (untilObj instanceof java.sql.Timestamp
                        ? ((java.sql.Timestamp) untilObj).toLocalDateTime()
                        : (LocalDateTime) untilObj);
        vo.setExpireAt(expireAt);
        // subscribed = expireAt != null && expireAt > now
        boolean subscribed = expireAt != null && expireAt.isAfter(LocalDateTime.now());
        vo.setSubscribed(subscribed);
        return vo;
    }

    /**
     * 按 planCode 加载订阅方案
     *
     * @param planCode 方案编码
     * @return 订阅方案
     */
    private SubscriptionPlan loadPlanByCode(String planCode) {
        SubscriptionPlan plan = planMapper.selectOne(new LambdaQueryWrapper<SubscriptionPlan>()
                .eq(SubscriptionPlan::getCode, planCode)
                .eq(SubscriptionPlan::getStatus, CommonConstants.STATUS_NORMAL)
                .eq(SubscriptionPlan::getDeleted, CommonConstants.NOT_DELETED)
                .last("LIMIT 1"));
        if (plan == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订阅方案不存在或已下架");
        }
        return plan;
    }

    /**
     * 构建订单实体
     *
     * @param plan    方案
     * @param userId  学生 ID
     * @return 订单实体（未插入）
     */
    private SubscriptionOrder buildOrder(SubscriptionPlan plan, Long userId) {
        SubscriptionOrder order = new SubscriptionOrder();
        order.setOrderNo(generateOrderNo());
        order.setStudentId(userId);
        order.setPlanId(plan.getId());
        order.setAmountFen(plan.getPriceFen());
        order.setStatus(CommonConstants.ORDER_PENDING);
        order.setExpireAt(LocalDateTime.now().plusMinutes(ORDER_EXPIRE_MINUTES));
        return order;
    }

    /**
     * 插入订单，遇到 uk_order_no 冲突重试一次
     *
     * @param order 订单实体
     */
    private void insertOrderWithRetry(SubscriptionOrder order) {
        int attempt = 0;
        while (true) {
            try {
                orderMapper.insert(order);
                return;
            } catch (DuplicateKeyException e) {
                attempt++;
                if (attempt > ORDER_NO_RETRY_MAX) {
                    log.error("订单号插入冲突，重试后仍失败 orderNo={}", order.getOrderNo());
                    throw new BusinessException(ResultCode.DUPLICATE_SUBMIT, "订单创建失败，请稍后重试");
                }
                // 重新生成订单号后重试
                order.setOrderNo(generateOrderNo());
            }
        }
    }

    /**
     * 生成商户订单号：HE + yyyyMMddHHmmss + 4 位随机
     *
     * @return 订单号
     */
    private String generateOrderNo() {
        String time = java.time.format.DateTimeFormatter
                .ofPattern("yyyyMMddHHmmss")
                .format(LocalDateTime.now());
        // UUID 截取 4 位随机串（去掉短横线后取前 4 位）
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return ORDER_NO_PREFIX + time + random;
    }

    /**
     * 从 packageVal 中提取 prepayId
     * <p>packageVal 形如 prepay_id=wx20240917...，截取等号后内容。</p>
     *
     * @param packageVal package 字段值
     * @return prepayId
     */
    private String extractPrepayId(String packageVal) {
        if (packageVal == null || !packageVal.startsWith("prepay_id=")) {
            return null;
        }
        return packageVal.substring("prepay_id=".length());
    }

    /**
     * 实体转 VO
     *
     * @param plan 方案实体
     * @return 方案 VO
     */
    private SubscriptionPlanVO toPlanVO(SubscriptionPlan plan) {
        SubscriptionPlanVO vo = new SubscriptionPlanVO();
        vo.setCode(plan.getCode());
        vo.setName(plan.getName());
        // priceYuan = priceFen / 100.0，保留 2 位
        vo.setPriceYuan(BigDecimal.valueOf(plan.getPriceFen())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        vo.setDurationDays(plan.getDurationDays());
        return vo;
    }
}
