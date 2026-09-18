package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.common.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识点只读查询 Mapper（学生侧）
 * <p>仅 BaseMapper 提供的 selectById / selectList 足以覆盖学生侧只读场景，
 * 与 history-admin 的 KnowledgePointMapper 区分命名以避免 @MapperScan 同名冲突。</p>
 */
@Mapper
public interface KnowledgePointQueryMapper extends BaseMapper<KnowledgePoint> {
}
