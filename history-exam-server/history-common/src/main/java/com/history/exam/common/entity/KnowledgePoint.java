package com.history.exam.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识点实体
 * <p>对应表 knowledge_point：考点编码、父子层级、卡片内容（Markdown）、配图、上下架状态。
 * 放置于 history-common 模块，供 history-admin（CRUD）与 history-student（只读查询）共享。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_point")
public class KnowledgePoint extends BaseEntity {

    /** 考点编码，唯一（如 K-CHN-ANC-QIN-HAN） */
    private String code;

    /** 考点名称 */
    private String name;

    /** 父考点 ID，0 表示根节点 */
    private Long parentId;

    /** 层级：1 一级 2 二级 3 三级 */
    private Integer level;

    /** 是否高频考点：0 否 1 是 */
    private Integer isHighFreq;

    /** 知识点卡片内容（Markdown） */
    private String cardContent;

    /** 卡片配图 URL */
    private String cardImage;

    /** 排序值，越小越靠前 */
    private Integer sort;

    /** 状态：0 上架 1 下架 */
    private Integer status;
}
