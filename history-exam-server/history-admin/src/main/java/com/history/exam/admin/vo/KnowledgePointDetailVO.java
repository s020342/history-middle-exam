package com.history.exam.admin.vo;

import lombok.Data;

/**
 * 知识点详情 VO（含 cardContent）
 */
@Data
public class KnowledgePointDetailVO {

    /** 知识点 ID（字符串传输） */
    private String id;

    /** 考点编码 */
    private String code;

    /** 考点名称 */
    private String name;

    /** 父考点 ID（字符串传输） */
    private String parentId;

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

    /** 状态：0 上架 1 下架 */
    private Integer status;
}
