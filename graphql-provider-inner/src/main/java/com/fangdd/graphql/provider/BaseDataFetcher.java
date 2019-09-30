package com.fangdd.graphql.provider;

import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 领域服务DataFetcher
 *
 * @author xuwenzhen
 */
public abstract class BaseDataFetcher implements DataFetcher {
    private List<String> dependencyFields;

    /**
     * 获取领域名称
     *
     * @return 领域名称
     */
    public abstract String getModuleName();

    /**
     * 获取本DataFetcher返回的GraphQL类型
     *
     * @return GraphQL类型
     */
    public abstract GraphQLOutputType getResponseGraphqlType();

    /**
     * 接口依赖的字段
     *
     * @return 字段数组，有可能为空！
     */
    public List<String> getDependencyFields() {
        return dependencyFields;
    }

    /**
     * 设置接口依赖的字段
     *
     * @param dependencyFields 接口依赖的字段
     */
    public void setDependencyFields(List<String> dependencyFields) {
        this.dependencyFields = dependencyFields;
    }

    /**
     * 添加一个依赖的字段
     *
     * @param field 字段名称
     */
    protected void addDependencyField(String field) {
        if (this.dependencyFields == null) {
            this.dependencyFields = Lists.newArrayList();
        }
        if (!this.dependencyFields.contains(field)) {
            this.dependencyFields.add(field);
        }
    }

    /**
     * 获取数据
     *
     * @param environment 执行上下文环境
     * @return 返回数据
     */
    protected Object getData(DataFetchingEnvironment environment) {
        return null;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        return getAsyncData(() -> getData(environment));
    }

    /**
     * 抽取本方法，主要是为了在APM中，探针重写，请不要删除本方法
     *
     * @param supplier supplier
     * @return
     */
    private CompletableFuture<Object> getAsyncData(Supplier<Object> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }
}
