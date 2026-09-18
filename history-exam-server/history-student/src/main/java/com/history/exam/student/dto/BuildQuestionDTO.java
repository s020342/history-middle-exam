package com.history.exam.student.dto;

import lombok.Data;

/**
 * 组卷请求 DTO
 * <p>GET /student/questions/build 入参；knowledgeId V1.0 保留字段但 Service 内部忽略，
 * 待 V1.1 question_knowledge 关联表落库后启用。</p>
 */
@Data
public class BuildQuestionDTO {

    /** 考点 ID（V1.0 忽略，V1.1 落库后启用） */
    private Long knowledgeId;

    /** 题型：1 选择 2 判断，可空表示混合 */
    private Integer type;

    /** 题量 */
    private Integer count;
}
