package com.history.exam.student.service.impl;

import com.history.exam.student.dto.BuildQuestionDTO;
import com.history.exam.student.entity.Question;
import com.history.exam.student.entity.QuestionOption;
import com.history.exam.student.mapper.QuestionMapper;
import com.history.exam.student.mapper.QuestionOptionMapper;
import com.history.exam.student.service.QuestionService;
import com.history.exam.student.vo.QuestionOptionVO;
import com.history.exam.student.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 题目服务实现
 * <p>V1.0 随机组卷流程：随机抽题 ID → 批量查题目 → 按题目 ID 升序逐题查选项组装 VO。
 * knowledgeId 字段 V1.0 不实现，待 V1.1 question_knowledge 表落库后启用。</p>
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;

    /**
     * 随机组卷
     * <p>步骤：1) selectRandomQuestionIds 取随机 ID；2) selectBatchIds 批量查题目；
     * 3) 按题目 ID 升序逐题 selectByQuestionId 查选项并封装到 QuestionVO.options。</p>
     *
     * @param dto 组卷参数
     * @return 题目 VO 列表；题量非法或无可用题目时返回空列表
     */
    @Override
    public List<QuestionVO> buildQuestions(BuildQuestionDTO dto) {
        // V1.0 不实现按考点组卷，knowledgeId 显式忽略
        Integer type = dto.getType();
        int count = dto.getCount() == null ? 0 : dto.getCount();
        // 题量非法直接返回空列表，避免无效查询
        if (count <= 0) {
            return Collections.emptyList();
        }
        // 1. 随机抽取题目 ID
        List<Long> questionIds = questionMapper.selectRandomQuestionIds(type, count);
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 2. 批量查询题目详情（selectBatchIds 由 MyBatis-Plus 自动追加 deleted=0）
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }
        // 3. 按题目 ID 升序排序，逐题查询选项并组装 VO
        questions.sort(Comparator.comparing(Question::getId));
        List<QuestionVO> voList = new ArrayList<>(questions.size());
        for (Question question : questions) {
            QuestionVO vo = toQuestionVO(question);
            List<QuestionOption> options = questionOptionMapper.selectByQuestionId(question.getId());
            vo.setOptions(toOptionVOList(options));
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 将题目实体转换为 VO
     *
     * @param question 题目实体
     * @return 题目 VO（不含选项，options 由调用方填充）
     */
    private QuestionVO toQuestionVO(Question question) {
        QuestionVO vo = new QuestionVO();
        vo.setId(String.valueOf(question.getId()));
        vo.setType(question.getType());
        vo.setStem(question.getStem());
        vo.setDifficulty(question.getDifficulty());
        vo.setAnswer(question.getAnswer());
        vo.setAnalysis(question.getAnalysis());
        return vo;
    }

    /**
     * 将选项实体列表转换为 VO 列表
     *
     * @param options 选项实体列表
     * @return 选项 VO 列表
     */
    private List<QuestionOptionVO> toOptionVOList(List<QuestionOption> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        List<QuestionOptionVO> list = new ArrayList<>(options.size());
        for (QuestionOption option : options) {
            QuestionOptionVO vo = new QuestionOptionVO();
            vo.setId(String.valueOf(option.getId()));
            vo.setKey(option.getOptionKey());
            vo.setContent(option.getContent());
            list.add(vo);
        }
        return list;
    }
}
