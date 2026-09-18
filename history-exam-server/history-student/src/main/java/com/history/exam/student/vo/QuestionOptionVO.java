package com.history.exam.student.vo;

import lombok.Data;

/**
 * 题目选项 VO
 * <p>id 序列化为字符串避免 JS 精度丢失。</p>
 */
@Data
public class QuestionOptionVO {

    /** 选项 ID（字符串传输） */
    private String id;

    /** 选项 key：A/B/C/D 或 T/F */
    private String key;

    /** 选项内容 */
    private String content;
}
