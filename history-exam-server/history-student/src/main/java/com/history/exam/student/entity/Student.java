package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 学生实体
 * <p>对应 student 表，承载小程序学生基本信息与订阅冗余字段。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {

    /** 微信 openid（唯一） */
    private String openid;

    /** 微信 unionid，可空 */
    private String unionid;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatarUrl;

    /** 年级，默认 9（初三） */
    private Integer grade;

    /** 家长绑定手机号 */
    private String phone;

    /** 状态：0 正常 1 禁用 */
    private Integer status;

    /** 是否订阅：0 否 1 是（冗余自订阅记录） */
    private Integer subscribed;

    /** 订阅到期时间，可空 */
    private LocalDateTime subscribedUntil;
}
