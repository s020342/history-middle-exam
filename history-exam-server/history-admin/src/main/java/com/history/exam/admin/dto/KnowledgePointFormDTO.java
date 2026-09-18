package com.history.exam.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识点创建/更新入参
 */
@Data
public class KnowledgePointFormDTO {

    /** 考点编码（唯一） */
    @NotBlank(message = "考点编码必填")
    private String code;

    /** 考点名称 */
    @NotBlank(message = "考点名称必填")
    private String name;

    /** 父考点 ID，0 表示根 */
    private Long parentId;

    /** 层级 */
    private Integer level;

    /** 是否高频考点 */
    private Integer isHighFreq;

    /** 卡片内容（Markdown） */
    private String cardContent;

    /** 卡片配图 URL */
    private String cardImage;

    /** 排序值 */
    private Integer sort;

    /** 状态：0 上架 1 下架 */
    private Integer status;
}
