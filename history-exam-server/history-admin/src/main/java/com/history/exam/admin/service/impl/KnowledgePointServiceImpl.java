package com.history.exam.admin.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.history.exam.admin.dto.KnowledgePointFormDTO;
import com.history.exam.admin.dto.KnowledgePointQueryDTO;
import com.history.exam.admin.mapper.KnowledgePointMapper;
import com.history.exam.admin.service.KnowledgePointService;
import com.history.exam.admin.vo.KnowledgePointDetailVO;
import com.history.exam.admin.vo.KnowledgePointVO;
import com.history.exam.common.api.ResultCode;
import com.history.exam.common.entity.KnowledgePoint;
import com.history.exam.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 知识点管理服务实现
 * <p>CRUD + 分页查询；卡片详情缓存 knowledge:card:{id} TTL 30 分钟，
 * 更新/删除时 evict。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgePointServiceImpl implements KnowledgePointService {

    private final KnowledgePointMapper knowledgePointMapper;
    private final StringRedisTemplate redisTemplate;

    /** Redis Key 前缀：知识点卡片详情缓存 */
    private static final String KEY_CARD = "knowledge:card:";

    /** 缓存 TTL：30 分钟 */
    private static final long TTL_CARD_MINUTES = 30L;

    /**
     * 分页查询知识点列表
     *
     * @param query 查询条件
     * @return 知识点 VO 分页
     */
    @Override
    public IPage<KnowledgePointVO> pageList(KnowledgePointQueryDTO query) {
        if (query == null) {
            query = new KnowledgePointQueryDTO();
        }
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 20 : query.getSize();
        Page<KnowledgePointVO> pageObj = new Page<>(page, size);
        return knowledgePointMapper.selectPageList(pageObj, query);
    }

    /**
     * 查询知识点详情（含 card_content，命中 Redis 缓存）
     *
     * @param id 知识点 ID
     * @return 详情
     */
    @Override
    public KnowledgePointDetailVO getById(Long id) {
        String key = KEY_CARD + id;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JSONUtil.toBean(cached, KnowledgePointDetailVO.class);
        }
        KnowledgePoint kp = knowledgePointMapper.selectById(id);
        if (kp == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "知识点不存在");
        }
        KnowledgePointDetailVO vo = toDetailVO(kp);
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(vo),
                TTL_CARD_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    /**
     * 创建知识点
     *
     * @param dto 创建入参
     * @return 新记录 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(KnowledgePointFormDTO dto) {
        KnowledgePoint kp = toEntity(dto);
        knowledgePointMapper.insert(kp);
        return kp.getId();
    }

    /**
     * 更新知识点
     *
     * @param id  知识点 ID
     * @param dto 更新入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, KnowledgePointFormDTO dto) {
        KnowledgePoint exists = knowledgePointMapper.selectById(id);
        if (exists == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "知识点不存在");
        }
        KnowledgePoint kp = toEntity(dto);
        kp.setId(id);
        knowledgePointMapper.updateById(kp);
        redisTemplate.delete(KEY_CARD + id);
    }

    /**
     * 软删知识点
     *
     * @param id 知识点 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        KnowledgePoint exists = knowledgePointMapper.selectById(id);
        if (exists == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "知识点不存在");
        }
        knowledgePointMapper.deleteById(id);
        redisTemplate.delete(KEY_CARD + id);
    }

    /**
     * 入参 DTO 转实体
     *
     * @param dto 入参
     * @return 实体
     */
    private KnowledgePoint toEntity(KnowledgePointFormDTO dto) {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setCode(dto.getCode());
        kp.setName(dto.getName());
        kp.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        kp.setLevel(dto.getLevel() == null ? 1 : dto.getLevel());
        kp.setIsHighFreq(dto.getIsHighFreq() == null ? 0 : dto.getIsHighFreq());
        kp.setCardContent(dto.getCardContent());
        kp.setCardImage(dto.getCardImage());
        kp.setSort(dto.getSort() == null ? 0 : dto.getSort());
        kp.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        return kp;
    }

    /**
     * 实体转详情 VO
     *
     * @param kp 实体
     * @return 详情 VO
     */
    private KnowledgePointDetailVO toDetailVO(KnowledgePoint kp) {
        KnowledgePointDetailVO vo = new KnowledgePointDetailVO();
        vo.setId(String.valueOf(kp.getId()));
        vo.setCode(kp.getCode());
        vo.setName(kp.getName());
        vo.setParentId(kp.getParentId() == null ? null : String.valueOf(kp.getParentId()));
        vo.setLevel(kp.getLevel());
        vo.setIsHighFreq(kp.getIsHighFreq());
        vo.setCardContent(kp.getCardContent());
        vo.setCardImage(kp.getCardImage());
        vo.setSort(kp.getSort());
        vo.setStatus(kp.getStatus());
        return vo;
    }
}
