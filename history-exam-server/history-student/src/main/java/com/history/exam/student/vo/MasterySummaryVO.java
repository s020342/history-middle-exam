package com.history.exam.student.vo;

import lombok.Data;

import java.util.List;

/**
 * 学生掌握度概览 VO
 * <p>按知识点维度聚合，前端用 mastery-bar 组件展示三档进度。</p>
 */
@Data
public class MasterySummaryVO {

    /** 知识点总数 */
    private Integer totalKnowledge;

    /** 已掌握（level=2）知识点数 */
    private Integer masteredCount;

    /** 部分掌握（level=1）知识点数 */
    private Integer partialCount;

    /** 未掌握（level=0）知识点数 */
    private Integer unMasteredCount;

    /** 按知识点分组的明细列表 */
    private List<KnowledgeMasteryItem> items;

    /**
     * 单个知识点的掌握度明细
     */
    @Data
    public static class KnowledgeMasteryItem {

        /** 知识点 ID（字符串传输） */
        private String knowledgeId;

        /** 知识点名称 */
        private String knowledgeName;

        /** 该知识点下题目总数 */
        private Integer totalCount;

        /** 已掌握题目数（level=2） */
        private Integer masteredCount;

        /** 未掌握题目数（level=0 或 1） */
        private Integer unMasteredCount;

        /** 该知识点的整体掌握等级：0 未掌握 1 部分 2 已掌握 */
        private Integer level;
    }
}
