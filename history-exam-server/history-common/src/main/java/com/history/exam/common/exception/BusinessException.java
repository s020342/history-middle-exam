package com.history.exam.common.exception;

import com.history.exam.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * <p>业务流程中显式抛出，由全局异常处理器转为统一响应。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务码 */
    private final Integer code;

    /**
     * 使用 ResultCode 构造业务异常
     *
     * @param resultCode 业务码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用 ResultCode + 自定义提示构造业务异常
     *
     * @param resultCode 业务码枚举
     * @param message    自定义提示
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
