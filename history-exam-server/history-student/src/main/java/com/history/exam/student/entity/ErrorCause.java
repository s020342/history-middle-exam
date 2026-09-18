package com.history.exam.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.history.exam.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 错因标签实体
 * <p>对应表 error_cause：错因字典，供 wrong_record.error_cause_id 引用。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("error_cause")
public class ErrorCause extends BaseEntity {

    /** 错因编码 */
    private String code;

    /** 错因名称 */
    private String name;

    /** 排序 */
    private Integer sort;
}
