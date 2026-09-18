package com.history.exam.student.controller;

import com.history.exam.common.api.Result;
import com.history.exam.student.service.KnowledgeCardService;
import com.history.exam.student.vo.KnowledgeCardDetailVO;
import com.history.exam.student.vo.KnowledgeCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生知识点卡片接口
 * <p>路由前缀 /student/knowledge，要求 STUDENT 角色（见 SecurityConfig）。</p>
 */
@RestController
@RequestMapping("/student/knowledge")
@RequiredArgsConstructor
public class KnowledgeCardController {

    private final KnowledgeCardService knowledgeCardService;

    /**
     * 查询高频知识点卡片列表（不带 card_content）
     *
     * @return 卡片列表
     */
    @GetMapping("/high-freq")
    public Result<List<KnowledgeCardVO>> highFreq() {
        return Result.success(knowledgeCardService.getHighFreqCards());
    }

    /**
     * 查询知识点卡片详情（含 card_content）
     *
     * @param id 知识点 ID
     * @return 卡片详情
     */
    @GetMapping("/{id}")
    public Result<KnowledgeCardDetailVO> detail(@PathVariable("id") Long id) {
        return Result.success(knowledgeCardService.getCardById(id));
    }
}
