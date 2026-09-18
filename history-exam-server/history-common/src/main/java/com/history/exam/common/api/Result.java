package com.history.exam.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 统一响应封装
 * <p>所有接口返回统一结构：{code, message, data}。</p>
 *
 * @param <T> data 字段类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /** 业务码：0 成功，非 0 失败 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /**
     * 构造默认成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 构造成功响应（带数据）
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 构造失败响应（仅业务码与提示）
     *
     * @param resultCode 业务码枚举
     * @param <T>        数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return build(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 构造失败响应（自定义提示）
     *
     * @param resultCode 业务码枚举
     * @param message    自定义提示
     * @param <T>        数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return build(resultCode.getCode(), message, null);
    }

    /**
     * 构造失败响应（直接传入业务码与提示）
     *
     * @param code    业务码
     * @param message 提示
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return build(code, message, null);
    }

    /**
     * 拼装响应对象
     *
     * @param code    业务码
     * @param message 提示
     * @param data    数据
     * @param <T>     数据类型
     * @return 响应对象
     */
    private static <T> Result<T> build(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
