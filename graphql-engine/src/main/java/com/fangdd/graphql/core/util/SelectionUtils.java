package com.fangdd.graphql.core.util;

import com.fangdd.graphql.core.*;
import com.fangdd.graphql.core.exception.GraphqlInvocationException;
import com.fangdd.graphql.fetcher.DataFetcherProxy;
import com.fangdd.graphql.provider.BaseDataFetcher;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.google.common.collect.Lists;
import graphql.execution.MergedField;
import graphql.language.Directive;
import graphql.language.Field;
import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author xuwenzhen
 * @date 2019/7/9
 */
public class SelectionUtils {
    private static final Logger logger = LoggerFactory.getLogger(SelectionUtils.class);

    private SelectionUtils() {
    }

    /**
     * 获取真正的selections（前缀过滤 & 去掉不用的）
     *
     * @param selections 当前Selections
     * @param prefix     前缀
     * @return 返回当前有效的selections
     */
    public static List<String> getSelections(List<String> selections, String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return selections;
        }
        List<String> fieldSelections = Lists.newArrayList();
        String pf = prefix + GraphqlConsts.CHAR_DOT;
        selections.forEach(selection -> {
            if (selection.startsWith(pf)) {
                fieldSelections.add(selection.substring(pf.length()));
            }
        });

        return fieldSelections;
    }

    /**
     * 分析当前的节点的查询，分离出selection和batchDataFetcher
     *
     * @param environment GraphQL查询上下文环境
     * @return 返回分析的结果
     */
    public static DataFetcherData analyseGql(
            DataFetchingEnvironment environment
    ) {
        UserExecutionContext context = environment.getContext();
        String executionKey = context.getExecutionKey();
        if (!StringUtils.isEmpty(executionKey)) {
            executionKey += GraphqlConsts.STR_EQ + environment.getExecutionStepInfo().getPath().toString();
            return (DataFetcherData) GraphqlContextUtils.CACHE.get(executionKey, key -> getDataFetcherData(environment));
        } else {
            return getDataFetcherData(environment);
        }
    }

    private static DataFetcherData getDataFetcherData(DataFetchingEnvironment environment) {
        DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
        if (selectionSet == null) {
            throw new GraphqlInvocationException("gql语法错误，selection不能为空！");
        }
        GraphQLOutputType entityType = environment.getFieldType();
        DataFetcherData dataFetcherData = new DataFetcherData();
        analyseGqlNode(environment, selectionSet, entityType, dataFetcherData, null, null, GraphQLList.class.isInstance(entityType), null);
        return dataFetcherData;
    }

    private static void analyseGqlNode(
            DataFetchingEnvironment environment,
            DataFetchingFieldSelectionSet selectionSet,
            GraphQLOutputType entityType,
            DataFetcherData dataFetcherData,
            String parentTypePath,
            String parentPath,
            boolean inList,
            BatchDataFetcherData bdf
    ) {
        boolean currentNodeInList = inList || GraphQLList.class.isInstance(entityType);
        selectionSet.getFields().forEach(field -> analyseGqlNodeField(environment, entityType, dataFetcherData, parentTypePath, parentPath, field, currentNodeInList, bdf));
    }

    private static void analyseGqlNodeField(
            DataFetchingEnvironment environment,
            GraphQLOutputType entityType,
            DataFetcherData dataFetcherData,
            String parentTypePath,
            String parentQueryPath,
            SelectedField field,
            boolean inList,
            BatchDataFetcherData bdf
    ) {
        String fieldName = field.getName();
        if (GraphqlConsts.STR_TYPENAME.equals(fieldName)) {
            return;
        }

        String qualifiedName = field.getQualifiedName();
        if (qualifiedName.contains(GraphqlConsts.PATH_SPLITTER)) {
            //带子节点的，全部丢弃
            return;
        }

        UserExecutionContext executionContext = environment.getContext();
        GraphqlContext graphqlContext = GraphqlContextUtils.getGraphqlContext(executionContext.getSchemaName());

        //检查指令
        analyseGqlFieldDirective(environment, dataFetcherData, field, parentTypePath, parentQueryPath);

        int index = qualifiedName.lastIndexOf(GraphqlConsts.PATH_SPLITTER);
        String queryFieldName = qualifiedName;
        if (index > -1) {
            queryFieldName = qualifiedName.substring(index);
        }
        boolean currentNodeInList = inList || GraphQLList.class.isInstance(entityType);
        String typeName = GraphqlTypeUtils.getFieldTypeName(entityType);
        List<SelectedField> subFields = field.getSelectionSet().getFields();
        BaseDataFetcher dataFetcher = graphqlContext.getDataFetcher(typeName, fieldName);
        String currentTypePath = parentTypePath == null ? fieldName : parentTypePath + GraphqlConsts.CHAR_DOT + fieldName;
        String currentQueryPath = parentQueryPath == null ? fieldName : parentQueryPath + GraphqlConsts.PATH_SPLITTER + queryFieldName;
        if (dataFetcher == null) {
            if (CollectionUtils.isEmpty(subFields)) {
                if (bdf == null) {
                    dataFetcherData.addSelection(currentTypePath);
                } else {
                    bdf.addSelection(currentTypePath);
                }
            } else {
                GraphQLOutputType fieldGraphQLOutputType = GraphqlTypeUtils.getFieldGraphQLOutputType(entityType, fieldName);
                analyseGqlNode(environment, field.getSelectionSet(), fieldGraphQLOutputType, dataFetcherData, currentTypePath, currentQueryPath, currentNodeInList, bdf);
            }
            return;
        }
        //添加DataFetcher内依赖的字段
        List<String> dataFetchRefSelections = dataFetcher.getDependencyFields();
        if (!CollectionUtils.isEmpty(dataFetchRefSelections)) {
            dataFetchRefSelections.forEach(dependencyId -> {
                String currentSelectionName = parentTypePath == null ? dependencyId : parentTypePath + GraphqlConsts.CHAR_DOT + dependencyId;
                if (bdf == null) {
                    dataFetcherData.addSelection(currentSelectionName);
                } else {
                    bdf.addSelection(currentSelectionName);
                }
            });
        }

        if (!DataFetcherProxy.class.isInstance(dataFetcher)) {
            return;
        }

        DataFetcherProxy dataFetcherProxy = (DataFetcherProxy) dataFetcher;
        BatchDataFetcherData batchDataFetcherData = null;
        if (inList) {
            //可以合并请求
            Api api = dataFetcherProxy.getApi();
            String moduleName = api.getModuleName();
            GraphqlModuleContext contextModule = graphqlContext.getContextModule(moduleName);
            if (contextModule == null) {
                throw new GraphqlInvocationException("无法找到" + moduleName + "对应的GraphqlModuleContext");
            }

            Api batchApi;
            if (api.getBatchProvider() != null && api.getBatchProvider()) {
                batchApi = api;
            } else {
                //找到批量处理的api
                String graphqlProviderName = dataFetcherProxy.getGraphqlProviderName();
                batchApi = graphqlContext.getApi(graphqlProviderName + GraphqlConsts.STR_EXCLAMATION);
                if (batchApi == null) {
                    logger.warn("{} {}未提供批量接口！请实现接口：@GraphqlProvider(name={}, batch=true)", contextModule.getProvider().getAppId(), api, graphqlProviderName);
                    return;
                }
            }

            batchDataFetcherData = new BatchDataFetcherData(contextModule, dataFetcherProxy, batchApi, parentTypePath);
            String path = environment.getExecutionStepInfo().getPath().toString();
            if (!StringUtils.isEmpty(parentTypePath)) {
                path += GraphqlConsts.PATH_SPLITTER + parentTypePath;
            }

            path += GraphqlConsts.PATH_SPLITTER + qualifiedName;
            dataFetcherData.addBatchField(path, batchDataFetcherData);
        }

        if (!CollectionUtils.isEmpty(subFields) && batchDataFetcherData != null) {
            GraphQLOutputType fieldGraphQLOutputType = GraphqlTypeUtils.getFieldGraphQLOutputType(entityType, fieldName);
            analyseGqlNode(environment, field.getSelectionSet(), fieldGraphQLOutputType, dataFetcherData, currentTypePath, currentQueryPath, false, batchDataFetcherData);
        }
    }

    private static void analyseGqlFieldDirective(DataFetchingEnvironment environment, DataFetcherData dataFetcherData, SelectedField selectedField, String parentTypePath, String parentQueryPath) {
        String path;
        if (StringUtils.isEmpty(parentQueryPath)) {
            path = selectedField.getQualifiedName();
        } else {
            path = parentQueryPath + GraphqlConsts.PATH_SPLITTER + selectedField.getQualifiedName();
        }
        MergedField mergedField = environment.getSelectionSet().get().getSubField(path);
        if (mergedField == null) {
            return;
        }
        List<Field> fields = mergedField.getFields();
        List<Directive> directiveList = Lists.newArrayList();
        fields.forEach(field -> {
            List<Directive> directives = field.getDirectives();
            if (CollectionUtils.isEmpty(directives)) {
                return;
            }
            directiveList.addAll(directives);
        });
        if (!directiveList.isEmpty()) {
            String fieldName = selectedField.getName();
            dataFetcherData.addFieldDirectives(StringUtils.isEmpty(parentTypePath) ? fieldName : parentTypePath + GraphqlConsts.CHAR_DOT + fieldName, directiveList);
        }
    }
}
