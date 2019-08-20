package com.fangdd.graphql.provider;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * 内部供应端
 *
 * @author xuwenzhen
 * @date 2019/5/22
 */
public class InnerProvider {
    public static final String QUERY = "Query";
    private static final String GRAPHQL = ".graphql";
    private static final String GRAPHQL_INNER_TYPE_PREFIX = "__";
    private static final char STR_DOT = '.';

    /**
     * 类型
     */
    private Map<String, GraphQLType> graphQLTypeMap;

    /**
     * 查询方法
     */
    private GraphQLObjectType queryType;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 关联ID
     */
    private Set<String> refIds;

    private Map<String, BaseDataFetcher> refIdDataFetcherMap;

    private RuntimeWiring.Builder newRuntimeWiring;

    private Map<String, DataFetcher> fieldDataFetcherMap;

    private GraphQLObjectType mutationType;

    /**
     * 构造方法
     *
     * @param moduleName 领域名称
     * @param refIds     关联IDs
     */
    public InnerProvider(String moduleName, Set<String> refIds) {
        this.moduleName = moduleName;
        this.refIds = refIds;
    }

    /**
     * 构建当前Inner Provider
     *
     * @return 返回当前Inner Provider
     */
    public InnerProvider build() {
        URL url = Resources.getResource(getModuleName() + GRAPHQL);
        String sdl;
        try {
            sdl = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new InnerProviderException("Graphql文件转码失败！" + getModuleName() + GRAPHQL, e);
        }

        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
        fieldDataFetcherMap.entrySet().forEach(entry -> {
            String fields = entry.getKey();
            int index = fields.indexOf(STR_DOT);
            String typeName = fields.substring(0, index);
            String fieldName = fields.substring(index + 1);
            DataFetcher dataFetcher = entry.getValue();
            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(typeName, fieldName),  dataFetcher);
        });

        newRuntimeWiring = RuntimeWiring.newRuntimeWiring()
                .codeRegistry(codeRegistryBuilder);
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry,  newRuntimeWiring.build());
        graphQLTypeMap = Maps.newHashMap();
        graphQLSchema.getTypeMap().entrySet().forEach(
                entry -> {
                    String typeName = entry.getKey();
                    if (typeName.startsWith(GRAPHQL_INNER_TYPE_PREFIX) || QUERY.equals(typeName)) {
                        return;
                    }
                    graphQLTypeMap.put(typeName, entry.getValue());
                }
        );
        queryType = graphQLSchema.getQueryType();
        mutationType = graphQLSchema.getMutationType();
        return this;
    }

    /**
     * 设置字段的DataFetcher
     *
     * @param typeName    类型名称
     * @param fieldName   字段名称
     * @param dataFetcher DataFetcher
     * @return 返回当前Inner Provider
     */
    public InnerProvider setDataFetcher(String typeName, String fieldName, DataFetcher dataFetcher) {
        if (fieldDataFetcherMap == null) {
            fieldDataFetcherMap = Maps.newHashMap();
        }
        fieldDataFetcherMap.put(typeName + STR_DOT + fieldName, dataFetcher);
        return this;
    }

    public Map<String, DataFetcher> getFieldDataFetcherMap() {
        return fieldDataFetcherMap;
    }

    public InnerProvider setRefIdDataFetcherMap(Map<String, BaseDataFetcher> refIdDataFetcherMap) {
        this.refIdDataFetcherMap = refIdDataFetcherMap;
        return this;
    }

    public Set<String> getRefIds() {
        return refIds;
    }

    public Map<String, BaseDataFetcher> getRefIdDataFetcherMap() {
        return refIdDataFetcherMap;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Map<String, GraphQLType> getGraphQLTypeMap() {
        return graphQLTypeMap;
    }

    public GraphQLObjectType getQueryType() {
        return queryType;
    }

    public GraphQLObjectType getMutationType() {
        return mutationType;
    }

    public void setMutationType(GraphQLObjectType mutationType) {
        this.mutationType = mutationType;
    }
}
