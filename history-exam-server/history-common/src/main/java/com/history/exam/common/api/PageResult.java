package com.history.exam.common.api;

import lombok.Data;

import java.util.List;

/**
 * 分页响应封装
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {

    /** 当前页（1 起） */
    private Long page;

    /** 每页大小 */
    private Long size;

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 当前页数据 */
    private List<T> records;

    /**
     * 构造分页结果
     *
     * @param records  当前页数据
     * @param total    总记录数
     * @param page     当前页
     * @param size     每页大小
     */
    public PageResult(List<T> records, Long total, Long page, Long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        // 总页数 = (总记录数 + 每页大小 - 1) / 每页大小；避免 size 为 0
        this.pages = size == 0 ? 0 : (total + size - 1) / size;
    }
}
