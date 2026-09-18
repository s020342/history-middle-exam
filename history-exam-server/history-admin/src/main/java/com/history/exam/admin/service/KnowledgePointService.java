package com.history.exam.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.history.exam.admin.dto.KnowledgePointFormDTO;
import com.history.exam.admin.dto.KnowledgePointQueryDTO;
import com.history.exam.admin.vo.KnowledgePointDetailVO;
import com.history.exam.admin.vo.KnowledgePointVO;

/**
 * 知识点管理服务
 * <p>提供知识点的 CRUD 与分页查询；卡片详情命中 Redis 缓存 knowledge:card:{id}。</p>
 */
public interface KnowledgePointService {

    /**
     * 分页查询知识点列表
     *
     * @param query 查询条件
     * @return 知识点 VO 分页
     */
    IPage<KnowledgePointVO> pageList(KnowledgePointQueryDTO query);

    /**
     * 查询知识点详情（含 card_content）
     *
     * @param id 知识点 ID
     * @return 详情
     */
    KnowledgePointDetailVO getById(Long id);

    /**
     * 创建知识点
     *
     * @param dto 创建入参
     * @return 新记录 ID
     */
    Long create(KnowledgePointFormDTO dto);

    /**
     * 更新知识点
     *
     * @param id  知识点 ID
     * @param dto 更新入参
     */
    void update(Long id, KnowledgePointFormDTO dto);

    /**
     * 软删知识点
     *
     * @param id 知识点 ID
     */
    void delete(Long id);
}
