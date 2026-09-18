package com.history.exam.pay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建订阅订单入参 DTO
 * <p>学生端调用 POST /student/subscription/orders 提交的请求体。</p>
 */
@Data
public class CreateOrderDTO {

    /** 订阅方案编码，如 MONTHLY */
    @NotBlank(message = "planCode 不能为空")
    private String planCode;
}
