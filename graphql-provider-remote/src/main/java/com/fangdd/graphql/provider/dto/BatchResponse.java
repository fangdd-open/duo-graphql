package com.fangdd.graphql.provider.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 排量接口响应体，为避免Graphql不支持Map
 *
 * @author xuwenzhen
 * @date 2019/6/25
 */
public class BatchResponse<T> {
    /**
     * 数据，响应值必须按批量的顺序加入本列表，不然会错乱！
     * 即使没有值，也应该用null占位！
     */
    private List<T> data = new ArrayList<>();

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public void add(T item) {
        data.add(item);
    }

    public T first() {
        return data.get(0);
    }
}
