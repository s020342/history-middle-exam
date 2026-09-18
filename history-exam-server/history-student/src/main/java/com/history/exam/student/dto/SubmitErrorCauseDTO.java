package com.history.exam.student.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交错因入参
 */
@Data
public class SubmitErrorCauseDTO {

    /** 错题记录 ID（wrong_record.id） */
    @NotNull(message = "错题记录 ID 必填")
    private Long recordId;

    /** 错因 ID（error_cause.id） */
    @NotNull(message = "错因 ID 必填")
    private Long errorCauseId;
}
