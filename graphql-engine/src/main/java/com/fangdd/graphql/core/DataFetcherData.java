package com.fangdd.graphql.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import graphql.language.Directive;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author xuwenzhen
 * @date 2019/7/29
 */
public class DataFetcherData implements Serializable {
    /**
     * 当前查询需要查询的字段
     */
    private List<String> selections = Lists.newArrayList();

    /**
     * 需要合并请求的字段
     */
    private Map<String, BatchDataFetcherData> batchDataFetcherDataMap;

    /**
     * 字段的指令表
     */
    private Map<String, List<Directive>> fieldDirectives;

    public List<String> getSelections() {
        return selections;
    }

    public void setSelections(List<String> selections) {
        this.selections = selections;
    }

    public Map<String, BatchDataFetcherData> getBatchDataFetcherDataMap() {
        return batchDataFetcherDataMap;
    }

    public void setBatchDataFetcherDataMap(Map<String, BatchDataFetcherData> batchDataFetcherDataMap) {
        this.batchDataFetcherDataMap = batchDataFetcherDataMap;
    }

    /**
     * 添加一个需要取回来的字段
     *
     * @param selection 需要取回的字段名称
     */
    public void addSelection(String selection) {
        if (!selections.contains(selection)) {
            selections.add(selection);
        }
    }

    /**
     * 添加一个需要合并处理的字段数据
     *
     * @param qualifiedName        当前字段的路径
     * @param batchDataFetcherData 批量处理需要用到的一些信息
     */
    public void addBatchField(String qualifiedName, BatchDataFetcherData batchDataFetcherData) {
        if (batchDataFetcherDataMap == null) {
            batchDataFetcherDataMap = Maps.newConcurrentMap();
        }
        batchDataFetcherDataMap.put(qualifiedName, batchDataFetcherData);
    }

    /**
     * 根据当前字段路径，获取合并处理数据
     *
     * @param qualifiedName 字段路径
     * @return 合并处理数据
     */
    public BatchDataFetcherData getBatchDataFetcherData(String qualifiedName) {
        if (batchDataFetcherDataMap == null) {
            return null;
        }
        return batchDataFetcherDataMap.get(qualifiedName);
    }

    public Map<String, List<Directive>> getFieldDirectives() {
        return fieldDirectives;
    }

    public void setFieldDirectives(Map<String, List<Directive>> fieldDirectives) {
        this.fieldDirectives = fieldDirectives;
    }

    public void addFieldDirectives(String path, List<Directive> directiveList) {
        if (fieldDirectives == null) {
            fieldDirectives = Maps.newHashMap();
        }
        fieldDirectives.put(path, directiveList);
    }
}
