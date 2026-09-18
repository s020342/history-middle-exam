package com.history.exam.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.history.exam.student.entity.Mastery;
import com.history.exam.student.vo.MasterySummaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 掌握度 Mapper
 * <p>提供 mastery 表基础 CRUD（BaseMapper）+ upsert + 到期扫描 + 学生维度聚合。</p>
 */
@Mapper
public interface MasteryMapper extends BaseMapper<Mastery> {

    /**
     * 按学生与题目查询掌握度记录（upsert 前预查）
     *
     * @param studentId  学生 ID
     * @param questionId 题目 ID
     * @return 掌握度记录；不存在返回 null
     */
    Mastery selectByStudentAndQuestion(@Param("studentId") Long studentId,
                                       @Param("questionId") Long questionId);

    /**
     * upsert：按 uk_student_question 唯一键存在则更新，否则插入
     * <p>传 Service 层算好的 level / consecutiveCorrect / nextReviewAt，避免 SQL 内嵌业务逻辑。</p>
     *
     * @param studentId          学生 ID
     * @param questionId         题目 ID
     * @param knowledgeId       知识点 ID（可空）
     * @param level              掌握度等级
     * @param consecutiveCorrect 连续答对次数
     * @param nextReviewAt       下次复习日
     * @return 受影响行数
     */
    int upsertByStudentQuestion(@Param("studentId") Long studentId,
                               @Param("questionId") Long questionId,
                               @Param("knowledgeId") Long knowledgeId,
                               @Param("level") Integer level,
                               @Param("consecutiveCorrect") Integer consecutiveCorrect,
                               @Param("nextReviewAt") LocalDate nextReviewAt);

    /**
     * 扫描到期复习记录：next_review_at <= today AND level < 2（已掌握不再生成）
     *
     * @param today 当日日期
     * @param limit 单批扫描上限
     * @return 到期掌握度记录列表
     */
    List<Mastery> selectDueList(@Param("today") LocalDate today,
                                @Param("limit") int limit);

    /**
     * 按学生查询掌握度概览：按 knowledge_id 聚合 level 加权与未掌握数
     *
     * @param studentId 学生 ID
     * @return 知识点维度聚合列表
     */
    List<MasterySummaryVO.KnowledgeMasteryItem> selectStudentSummary(@Param("studentId") Long studentId);
}
