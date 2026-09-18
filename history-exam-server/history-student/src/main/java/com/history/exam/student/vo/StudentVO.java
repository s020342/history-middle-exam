package com.history.exam.student.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生视图
 * <p>对外暴露的学生信息，主键转为字符串避免前端精度丢失。</p>
 */
@Data
public class StudentVO {

    /** 学生 ID（字符串形式） */
    private String id;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatarUrl;

    /** 是否订阅：true 已订阅 / false 未订阅 */
    private Boolean subscribed;

    /** 订阅到期时间，可空 */
    private LocalDateTime subscribedUntil;
}
