package com.fangdd.graphql.provider.dto;

import java.util.List;

/**
 * @author wtx
 * @date 2019/10/10 19:49
 */
public class AggregatePagination<T> extends Pagination<T> {

    /**
     * 简单聚合结果
     */
    private List<SimpleAggregation> simpleAggregations;


    public List<SimpleAggregation> getSimpleAggregations() {
        return simpleAggregations;
    }

    public void setSimpleAggregations(List<SimpleAggregation> simpleAggregations) {
        this.simpleAggregations = simpleAggregations;
    }
}
