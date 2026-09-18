package com.history.exam.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.history.exam.common.constant.CommonConstants;
import com.history.exam.common.entity.KnowledgePoint;
import com.history.exam.student.mapper.KnowledgePointQueryMapper;
import com.history.exam.student.mapper.QuestionKnowledgeMapper;
import com.history.exam.student.service.KnowledgeCardService;
import com.history.exam.student.vo.KnowledgeCardDetailVO;
import com.history.exam.student.vo.KnowledgeCardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识点卡片服务实现（学生侧只读）
 * <p>列表不带 card_content（大字段）；详情按 ID 或按题目反查主考点。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeCardServiceImpl implements KnowledgeCardService {

    private final KnowledgePointQueryMapper knowledgePointQueryMapper;
    private final QuestionKnowledgeMapper questionKnowledgeMapper;

    /**
     * 查询高频知识点卡片列表（不带 card_content）
     *
     * @return 卡片列表
     */
    @Override
    public List<KnowledgeCardVO> getHighFreqCards() {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getIsHighFreq, 1)
               .eq(KnowledgePoint::getStatus, CommonConstants.STATUS_ONLINE)
               .orderByAsc(KnowledgePoint::getSort);
        List<KnowledgePoint> list = knowledgePointQueryMapper.selectList(wrapper);
        List<KnowledgeCardVO> result = new ArrayList<>(list == null ? 0 : list.size());
        if (list != null) {
            for (KnowledgePoint kp : list) {
                result.add(toCardVO(kp));
            }
        }
        return result;
    }

    /**
     * 查询知识点卡片详情（含 card_content）
     *
     * @param id 知识点 ID
     * @return 卡片详情
     */
    @Override
    public KnowledgeCardDetailVO getCardById(Long id) {
        KnowledgePoint kp = knowledgePointQueryMapper.selectById(id);
        return kp == null ? null : toCardDetailVO(kp);
    }

    /**
     * 按题目 ID 反查主考点卡片详情
     *
     * @param questionId 题目 ID
     * @return 主考点卡片详情；未关联返回 null
     */
    @Override
    public KnowledgeCardDetailVO getCardByQuestionId(Long questionId) {
        Long knowledgeId = questionKnowledgeMapper.selectPrimaryKnowledgeIdByQuestionId(questionId);
        if (knowledgeId == null) {
            return null;
        }
        return getCardById(knowledgeId);
    }

    /**
     * 实体转列表 VO（不带 cardContent）
     *
     * @param kp 实体
     * @return 列表 VO
     */
    private KnowledgeCardVO toCardVO(KnowledgePoint kp) {
        KnowledgeCardVO vo = new KnowledgeCardVO();
        vo.setId(String.valueOf(kp.getId()));
        vo.setCode(kp.getCode());
        vo.setName(kp.getName());
        vo.setLevel(kp.getLevel());
        vo.setIsHighFreq(kp.getIsHighFreq());
        vo.setCardImage(kp.getCardImage());
        vo.setSort(kp.getSort());
        return vo;
    }

    /**
     * 实体转详情 VO（含 cardContent）
     *
     * @param kp 实体
     * @return 详情 VO
     */
    private KnowledgeCardDetailVO toCardDetailVO(KnowledgePoint kp) {
        KnowledgeCardDetailVO vo = new KnowledgeCardDetailVO();
        vo.setId(String.valueOf(kp.getId()));
        vo.setCode(kp.getCode());
        vo.setName(kp.getName());
        vo.setLevel(kp.getLevel());
        vo.setIsHighFreq(kp.getIsHighFreq());
        vo.setCardContent(kp.getCardContent());
        vo.setCardImage(kp.getCardImage());
        vo.setSort(kp.getSort());
        return vo;
    }
}
