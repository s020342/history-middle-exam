package com.history.exam.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.history.exam.admin.dto.KnowledgePointQueryDTO;
import com.history.exam.admin.vo.KnowledgePointVO;
import com.history.exam.common.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 知识点 Mapper（管理端）
 * <p>提供 knowledge_point 表基础 CRUD + 按名称/高频/状态筛选的分页查询。</p>
 */
@Mapper
public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {

    /**
     * 分页查询知识点列表
     * <p>支持名称模糊、高频筛选、状态筛选；不带 card_content（大字段单独查询）。</p>
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 知识点 VO 分页
     */
    IPage<KnowledgePointVO> selectPageList(IPage<KnowledgePointVO> page,
                                          @Param("query") KnowledgePointQueryDTO query);
}
