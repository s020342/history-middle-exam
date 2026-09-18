package com.history.exam.admin.vo;

import lombok.Data;

/**
 * 知识点列表项 VO（不带 cardContent 大字段）
 */
@Data
public class KnowledgePointVO {

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

    /** 排序值 */
    private Integer sort;

    /** 状态：0 上架 1 下架 */
    private Integer status;
}
