package com.sls.dto;

import java.util.List;

/**
 * 分页查询结果
 */
public class PageResult<T> {

    private List<T> list;     // 当前页数据
    private long total;       // 总记录数
    private int page;         // 当前页码
    private int pageSize;     // 每页条数

    public PageResult(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }
}
