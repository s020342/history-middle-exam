package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.student.entity.ErrorCause;
import org.apache.ibatis.annotations.Mapper;

/**
 * 错因标签 Mapper
 * <p>提供 error_cause 表基础 CRUD。</p>
 */
@Mapper
public interface ErrorCauseMapper extends BaseMapper<ErrorCause> {
}
