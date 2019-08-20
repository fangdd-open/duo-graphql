package com.fangdd.graphql.provider.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 带分页信息的
 *
 * @author xuwenzhen
 */
public class Pagination<T extends Serializable, E> {
    /**
     * 结果列表
     */
    private List<T> list;

    /**
     * 总数
     */
    private long total;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
