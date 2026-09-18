package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 错题记录实体
 * <p>对应表 wrong_record：累计学生某题的错次、错因、解决状态等。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wrong_record")
public class WrongRecord extends BaseEntity {

    /** 学生 ID */
    private Long studentId;

    /** 题目 ID */
    private Long questionId;

    /** 错因 ID */
    private Long errorCauseId;

    /** 累计错次 */
    private Integer errorCount;

    /** 最近答错时间 */
    private LocalDateTime lastWrongAt;

    /** 是否已解决：0 否 1 是 */
    private Integer resolved;

    /** 解决时间 */
    private LocalDateTime resolvedAt;
}
