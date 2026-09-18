package com.history.exam.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.history.exam.common.api.PageResult;
import com.history.exam.common.api.ResultCode;
import com.history.exam.common.context.CurrentUserHolder;
import com.history.exam.common.exception.BusinessException;
import com.history.exam.student.dto.SubmitErrorCauseDTO;
import com.history.exam.student.entity.WrongRecord;
import com.history.exam.student.mapper.WrongRecordMapper;
import com.history.exam.student.service.WrongRecordService;
import com.history.exam.student.vo.WrongRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 错题记录服务实现
 * <p>错题列表分页（JOIN question + LEFT JOIN error_cause）；
 * 错因提交防越权（按 student_id 限定）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WrongRecordServiceImpl implements WrongRecordService {

    private final WrongRecordMapper wrongRecordMapper;

    /**
     * 分页查询当前学生错题列表
     *
     * @param page 页码（1 起）
     * @param size 每页大小
     * @return 错题分页结果
     */
    @Override
    public PageResult<WrongRecordVO> listWrongRecords(int page, int size) {
        Long studentId = CurrentUserHolder.getUserId();

        // MyBatis-Plus 分页：page 从 1 起，size 由调用方约束
        Page<WrongRecordVO> pageParam = new Page<>(page, size);
        IPage<WrongRecordVO> result = wrongRecordMapper.selectWrongRecordPage(pageParam, studentId);

        List<WrongRecordVO> records = result.getRecords();
        return new PageResult<>(records, result.getTotal(), (long) page, (long) size);
    }

    /**
     * 提交错因到指定错题记录
     * <p>UPDATE wrong_record SET error_cause_id=? WHERE id=? AND student_id=?（防越权）；
     * 校验记录存在（影响行数=0 视为不存在或越权）。</p>
     *
     * @param dto 错因入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitErrorCause(SubmitErrorCauseDTO dto) {
        Long studentId = CurrentUserHolder.getUserId();

        // 防越权：UPDATE 同时限定 student_id；影响行数 0 即记录不存在或不属于当前学生
        WrongRecord update = new WrongRecord();
        update.setErrorCauseId(dto.getErrorCauseId());
        LambdaUpdateWrapper<WrongRecord> wrapper = new LambdaUpdateWrapper<WrongRecord>()
                .eq(WrongRecord::getId, dto.getRecordId())
                .eq(WrongRecord::getStudentId, studentId);
        int affected = wrongRecordMapper.update(update, wrapper);
        if (affected == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }
}
