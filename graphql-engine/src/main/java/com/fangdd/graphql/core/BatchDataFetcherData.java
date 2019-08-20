package com.fangdd.graphql.core;

import com.fangdd.graphql.fetcher.DataFetcherProxy;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 * 需要进行合并请求的数据
 *
 * @author xuwenzhen
 * @date 2019/7/29
 */
public class BatchDataFetcherData implements Serializable {
    /**
     * 当前模块的上下文
     */
    private GraphqlModuleContext contextModule;

    /**
     * 合并请求的接口
     */
    private Api api;

    /**
     * 当前字段绑定的DataFetcher
     */
    private DataFetcherProxy dataFetcher;

    /**
     * 需要查询的字段
     */
    private List<String> selections;

    /**
     * 字段路径
     */
    private String fieldPath;

    public BatchDataFetcherData(GraphqlModuleContext contextModule, DataFetcherProxy dataFetcher, Api api, String fieldPath) {
        this.contextModule = contextModule;
        this.dataFetcher = dataFetcher;
        this.api = api;
        this.fieldPath = fieldPath;
    }

    public DataFetcherProxy getDataFetcher() {
        return dataFetcher;
    }

    public Api getApi() {
        return api;
    }

    public GraphqlModuleContext getContextModule() {
        return contextModule;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public List<String> getSelections() {
        return selections;
    }

    public void setSelections(List<String> selections) {
        this.selections = selections;
    }

    public void addSelection(String selection) {
        if (selections == null) {
            selections = Lists.newArrayList();
        }
        selections.add(selection);
    }
}
