package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 题目实体
 * <p>对应 question 表；继承 BaseEntity 公共字段（id/createdAt/updatedAt/deleted）。
 * 字段命名遵循驼峰，依赖 map-underscore-to-camel-case 自动映射下划线列名。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class Question extends BaseEntity {

    /** 题型：1 选择 2 判断 */
    private Integer type;

    /** 题干（可含图） */
    private String stem;

    /** 难度：1 易 2 中 3 难 */
    private Integer difficulty;

    /** 来源类型：1 真题原文(禁用) 2 真题改编 3 模拟 4 原创 5 开源搬运 */
    private Integer sourceType;

    /** 真题年份，可空 */
    private Integer sourceYear;

    /** 来源描述 */
    private String sourceDesc;

    /** 正确答案（选项 key 或 T/F） */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 状态：0 上架 1 下架 2 审核中 */
    private Integer status;

    /** 作答次数（冗余） */
    private Integer reviewCount;

    /** 正确率 0-100（冗余） */
    private BigDecimal correctRate;

    /** 抓取侧唯一 ID */
    private String externalId;

    /** 许可证：CC-BY-SA-4.0 / MIT / ORIGINAL */
    private String license;

    /** 原作者/投稿人 */
    private String author;

    /** 来源链接 */
    private String sourceUrl;
}
