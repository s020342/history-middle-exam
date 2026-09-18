package com.history.exam.student.vo;

import lombok.Data;

/**
 * 知识点卡片详情 VO
 * <p>在列表项基础上扩展 cardContent，供小程序展开后展示卡片正文。</p>
 */
@Data
public class KnowledgeCardDetailVO {

    /** 知识点 ID（字符串传输） */
    private String id;

    /** 考点编码 */
    private String code;

    /** 考点名称 */
    private String name;

    /** 层级 */
    private Integer level;

    /** 是否高频考点 */
    private Integer isHighFreq;

    /** 知识点卡片内容（Markdown） */
    private String cardContent;

    /** 卡片配图 URL */
    private String cardImage;

    /** 排序值 */
    private Integer sort;
}
