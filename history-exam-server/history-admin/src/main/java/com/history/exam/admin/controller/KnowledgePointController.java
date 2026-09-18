package com.history.exam.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.history.exam.admin.dto.KnowledgePointFormDTO;
import com.history.exam.admin.dto.KnowledgePointQueryDTO;
import com.history.exam.admin.service.KnowledgePointService;
import com.history.exam.admin.vo.KnowledgePointDetailVO;
import com.history.exam.admin.vo.KnowledgePointVO;
import com.history.exam.common.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识点管理接口
 * <p>路由前缀 /admin/knowledge-point，要求 ADMIN 角色（见 SecurityConfig）。</p>
 */
@RestController
@RequestMapping("/admin/knowledge-point")
@RequiredArgsConstructor
public class KnowledgePointController {

    private final KnowledgePointService knowledgePointService;

    /**
     * 分页查询知识点列表
     *
     * @param query 查询条件
     * @return 知识点 VO 分页
     */
    @GetMapping
    public Result<IPage<KnowledgePointVO>> pageList(KnowledgePointQueryDTO query) {
        return Result.success(knowledgePointService.pageList(query));
    }

    /**
     * 查询知识点详情
     *
     * @param id 知识点 ID
     * @return 详情
     */
    @GetMapping("/{id}")
    public Result<KnowledgePointDetailVO> getById(@PathVariable("id") Long id) {
        return Result.success(knowledgePointService.getById(id));
    }

    /**
     * 创建知识点
     *
     * @param dto 创建入参
     * @return 新记录 ID
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody KnowledgePointFormDTO dto) {
        return Result.success(knowledgePointService.create(dto));
    }

    /**
     * 更新知识点
     *
     * @param id  知识点 ID
     * @param dto 更新入参
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @Valid @RequestBody KnowledgePointFormDTO dto) {
        knowledgePointService.update(id, dto);
        return Result.success();
    }

    /**
     * 软删知识点
     *
     * @param id 知识点 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        knowledgePointService.delete(id);
        return Result.success();
    }
}
