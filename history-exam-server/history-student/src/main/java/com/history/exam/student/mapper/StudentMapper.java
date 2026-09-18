package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生 Mapper
 * <p>提供 student 表的基础 CRUD 能力，复用 MyBatis-Plus BaseMapper。</p>
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
