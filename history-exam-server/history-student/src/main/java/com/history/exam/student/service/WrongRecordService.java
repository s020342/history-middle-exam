package com.history.exam.student.service;

import com.history.exam.common.api.PageResult;
import com.history.exam.student.dto.SubmitErrorCauseDTO;
import com.history.exam.student.vo.WrongRecordVO;

/**
 * 错题记录服务
 */
public interface WrongRecordService {

    /**
     * 分页查询当前学生错题列表
     *
     * @param page 页码（1 起）
     * @param size 每页大小
     * @return 错题分页结果
     */
    PageResult<WrongRecordVO> listWrongRecords(int page, int size);

    /**
     * 提交错因到指定错题记录（防越权）
     *
     * @param dto 错因入参（recordId、errorCauseId）
     */
    void submitErrorCause(SubmitErrorCauseDTO dto);
}
