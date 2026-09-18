package com.history.exam.admin.dto;

import lombok.Data;

/**
 * 知识点分页查询入参
 */
@Data
public class KnowledgePointQueryDTO {

    /** 当前页（默认 1） */
    private Integer page = 1;

    /** 每页大小（默认 20） */
    private Integer size = 20;

    /** 考点名称（模糊匹配，可空） */
    private String name;

    /** 是否高频：0 否 1 是，null 不筛选 */
    private Integer isHighFreq;

    /** 状态：0 上架 1 下架，null 不筛选 */
    private Integer status;
}
