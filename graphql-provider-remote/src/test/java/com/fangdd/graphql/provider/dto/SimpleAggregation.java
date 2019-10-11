package com.fangdd.graphql.provider.dto;

/**
 * @author wtx
 * @date 2019/10/10 19:52
 */
public class SimpleAggregation {

    /**
     * 聚合的KEY
     */
    private String name;

    /**
     * 聚合的值
     */
    private Double value;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
