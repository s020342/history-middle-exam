package com.history.exam.student.vo;

import lombok.Data;

/**
 * 首页概况视图
 * <p>首页展示学生昵称、订阅状态与今日剩余题量。</p>
 */
@Data
public class HomeProfileVO {

    /** 昵称 */
    private String nickname;

    /** 是否订阅 */
    private Boolean subscribed;

    /** 今日剩余题量 = max(0, dailyLimit - 今日已答数) */
    private Integer remainingCount;

    /** 每日免费题量上限 */
    private Integer dailyLimit;
}
