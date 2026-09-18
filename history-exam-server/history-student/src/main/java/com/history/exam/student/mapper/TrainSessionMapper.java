package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.student.entity.TrainSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练会话 Mapper
 * <p>提供 train_session 表的基础 CRUD。</p>
 */
@Mapper
public interface TrainSessionMapper extends BaseMapper<TrainSession> {
}
