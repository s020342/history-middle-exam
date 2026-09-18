package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.history.exam.student.entity.WrongRecord;
import com.history.exam.student.vo.WrongRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 错题记录 Mapper
 * <p>提供 wrong_record 表基础 CRUD 与按学生+题目的查询、错题列表分页查询。</p>
 */
@Mapper
public interface WrongRecordMapper extends BaseMapper<WrongRecord> {

    /**
     * 按学生与题目查询错题记录（用于 upsert 前判定）
     *
     * @param studentId  学生 ID
     * @param questionId 题目 ID
     * @return 错题记录；不存在返回 null
     */
    WrongRecord selectByStudentAndQuestion(@Param("studentId") Long studentId,
                                            @Param("questionId") Long questionId);

    /**
     * 分页查询错题列表（JOIN question 取题干/类型/正确答案，LEFT JOIN error_cause 取错因名）
     *
     * @param page      分页对象
     * @param studentId 学生 ID
     * @return 错题 VO 分页
     */
    IPage<WrongRecordVO> selectWrongRecordPage(IPage<WrongRecordVO> page,
                                                @Param("studentId") Long studentId);
}
