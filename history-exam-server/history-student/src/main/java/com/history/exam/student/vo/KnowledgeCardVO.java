package com.history.exam.student.vo;

import lombok.Data;

/**
 * 知识点卡片列表项 VO
 * <p>列表场景不带 cardContent（TEXT 大字段），点击展开/详情时单独拉取。</p>
 */
@Data
public class KnowledgeCardVO {

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

    /** 卡片配图 URL */
    private String cardImage;

    /** 排序值 */
    private Integer sort;
}
