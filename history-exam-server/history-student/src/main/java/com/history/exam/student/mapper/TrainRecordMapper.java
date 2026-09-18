package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.student.entity.TrainRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 训练答题记录 Mapper
 * <p>提供 train_record 表基础 CRUD 与今日已答题量统计。</p>
 */
@Mapper
public interface TrainRecordMapper extends BaseMapper<TrainRecord> {

    /**
     * 统计学生今日已答题数（按 answered_at 当日）
     *
     * @param studentId 学生 ID
     * @return 今日已答题数
     */
    int countTodayAnswered(@Param("studentId") Long studentId);
}
