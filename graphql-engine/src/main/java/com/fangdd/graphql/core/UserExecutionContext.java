package com.fangdd.graphql.core;

import com.fangdd.graphql.core.subscribe.GraphqlSubscriber;
import com.fangdd.graphql.fetcher.batcher.BatchLoader;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 查询运行时上下文
 *
 * @author xuwenzhen
 * @date 2019/6/24
 */
public class UserExecutionContext {
    /**
     * 当前schema名称
     */
    private String schemaName;

    /**
     * 当前属性路径->BatchLoader
     */
    private Map<String, BatchLoader> batchDataLoaders = Maps.newConcurrentMap();

    /**
     * 请求头
     */
    private Map<String, String> headers = Maps.newHashMap();

    /**
     * 当前查询的缓存KEY
     */
    private String executionKey;
    private GraphqlSubscriber subscriber;

    /**
     * 将某个dataFetcher添加到批量处理里面，并执行请求
     *
     * @param batchLoader 批量处理器
     */
    public void bathFetch(BatchLoader batchLoader) {
        batchLoader.setHeaders(headers);
        batchDataLoaders.computeIfAbsent(batchLoader.getPath(), key -> batchLoader).fetchData();
    }

    public BatchLoader getBatchLoader(String key) {
        return batchDataLoaders.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }

    public String getExecutionKey() {
        return executionKey;
    }

    public void setExecutionKey(String executionKey) {
        this.executionKey = executionKey;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setSubscriber(GraphqlSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    public GraphqlSubscriber getSubscriber() {
        return subscriber;
    }
}
