package com.history.exam.student.service;

import com.history.exam.student.vo.KnowledgeCardDetailVO;
import com.history.exam.student.vo.KnowledgeCardVO;

import java.util.List;

/**
 * 知识点卡片服务
 * <p>学生侧只读查询：高频卡片列表、卡片详情、按题目反查主考点卡片。
 * 不写入，不缓存列表（缓存留给 admin CRUD 层管理 knowledge:card:{id}）。</p>
 */
public interface KnowledgeCardService {

    /**
     * 查询高频知识点卡片列表（不带 card_content）
     *
     * @return 卡片列表
     */
    List<KnowledgeCardVO> getHighFreqCards();

    /**
     * 查询知识点卡片详情（含 card_content）
     *
     * @param id 知识点 ID
     * @return 卡片详情
     */
    KnowledgeCardDetailVO getCardById(Long id);

    /**
     * 按题目 ID 反查主考点卡片详情（小程序答错后展示）
     *
     * @param questionId 题目 ID
     * @return 主考点卡片详情；未关联返回 null
     */
    KnowledgeCardDetailVO getCardByQuestionId(Long questionId);
}
