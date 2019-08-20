package com.fangdd.graphql.provider.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.io.Serializable;

/**
 * esf_trade（成交索引）
 * @author xuwenzhen
 */
public class Trade implements Serializable {
    /**
     * 成交ID
     *
     * @demo 2137
     */
    @JsonAlias("tradeId")
    private long id;

    /**
     * 小区id
     *
     * @demo 235872
     */
    private long cellId;

    /**
     * 小区名称
     *
     * @demo 佛山里
     */
    private String cellName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCellId() {
        return cellId;
    }

    public void setCellId(long cellId) {
        this.cellId = cellId;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }
}