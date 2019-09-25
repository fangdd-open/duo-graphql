package com.fangdd.graphql.pipeline;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.GraphqlModuleContext;
import com.fangdd.graphql.core.exception.GraphqlBuildException;
import com.fangdd.graphql.core.util.GraphqlContextUtils;
import com.fangdd.graphql.fetcher.DataFetcherProxy;
import com.fangdd.graphql.provider.BaseDataFetcher;
import com.fangdd.graphql.provider.InnerProvider;
import com.fangdd.graphql.provider.dto.ProviderModelInfo;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.fangdd.graphql.provider.dto.provider.Entity;
import com.fangdd.graphql.provider.dto.provider.ProviderApiDto;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 注册过程状态
 *
 * @author xuwenzhen
 * @date 2019/6/3
 */
public class RegistryState {
    private static final Logger logger = LoggerFactory.getLogger(RegistryState.class);

    /**
     * 需要构建的Schema名称
     */
    private String schemaName;

    /**
     * 本次要构建的GraphQL Providers（不一定包含全部）
     */
    private List<TpDocGraphqlProviderServiceInfo> graphqlProviderList;

    /**
     * 正在构建中的GraphQLType Set
     */
    private Set<String> creatingGraphQLTypes = Sets.newHashSet();

    /**
     * 中止信号
     */
    private boolean stop = false;

    /**
     * 各模块环境Map
     * moduleName => GraphqlModuleContext
     */
    private Map<String, GraphqlModuleContext> moduleContextMap = Maps.newLinkedHashMap();

    /**
     * 各实体映射表（不包含InnerProvider的）
     * entityName => Entity
     */
    private Map<String, Entity> entityMap = Maps.newHashMap();

    /**
     * API Map
     * api.code / graphqlProviderName => Api
     */
    private Map<String, Api> apiMap = Maps.newHashMap();

    /**
     * 接口-DataFetcher映射表
     * api code => BaseDataFetch
     * refId => BaseDataFetch
     * FieldCoordinates => BaseDataFetcher
     */
    private Map<String, BaseDataFetcher> dataFetcherMap = Maps.newHashMap();

    /**
     * 当前注册上来的GraphQL类型
     * java class name => GraphQLType
     */
    private Map<String, GraphQLType> graphQLTypeMap = Maps.newHashMap();

    /**
     * GraphQL CodeRegistry Builder
     */
    private GraphQLCodeRegistry.Builder graphQLCodeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

    /**
     * 各模块的查询字段定义
     */
    private Map<String, List<GraphQLFieldDefinition>> moduleQueryFieldDefinitionMap = Maps.newHashMap();

    /**
     * 各模块的写字段定义
     */
    private Map<String, List<GraphQLFieldDefinition>> moduleMutationFieldDefinitionMap = Maps.newHashMap();

    /**
     * 通过需要注册的GraphQL Provider信息，创建 RegistryState
     *
     * @param schemaName          Schema名称
     * @param graphqlProviderList 需要注册的GraphQL Provider信息
     */
    public RegistryState(String schemaName, List<TpDocGraphqlProviderServiceInfo> graphqlProviderList) {
        this.schemaName = schemaName;
        this.graphqlProviderList = graphqlProviderList;
        GraphqlContextUtils.registry(this);
    }

    public Map<String, GraphqlModuleContext> getModuleContextMap() {
        return moduleContextMap;
    }

    public Map<String, Entity> getEntityMap() {
        return entityMap;
    }

    /**
     * stop the building process
     */
    public void stop() {
        this.stop = true;
    }

    /**
     * check the state of building process
     *
     * @return true or false
     */
    public boolean isStop() {
        return stop;
    }

    public List<TpDocGraphqlProviderServiceInfo> getProviderServices() {
        return graphqlProviderList;
    }

    public Map<String, ProviderApiDto> getProviderApiMap() {
        Map<String, ProviderApiDto> providerApiMap = new HashMap<>(moduleContextMap.size());
        moduleContextMap.entrySet().forEach(entry -> providerApiMap.put(entry.getKey(), entry.getValue().getProviderApi()));
        return providerApiMap;
    }

    public Map<String, BaseDataFetcher> getDataFetcherMap() {
        return dataFetcherMap;
    }

    /**
     * 判断某个领域是否已经存在
     *
     * @param moduleName 领域名称
     * @return 是否已经存在
     */
    public boolean moduleContextExists(String moduleName) {
        return moduleContextMap.containsKey(moduleName);
    }

    /**
     * add Remote Provider context
     *
     * @param moduleContext Remote Provider Context
     */
    public void putRemoteProvider(GraphqlModuleContext moduleContext) {
        TpDocGraphqlProviderServiceInfo provider = moduleContext.getProvider();
        ProviderApiDto providerApi = moduleContext.getProviderApi();
        String moduleName = provider.getModuleName();
        moduleContextMap.put(moduleName, moduleContext);

        //抽取
        addEntities(providerApi.getEntities());
        providerApi.getApis().forEach(api -> setApi(moduleName, api));

        //处理refId
        List<ProviderModelInfo> models = provider.getModels();
        if (CollectionUtils.isEmpty(models)) {
            return;
        }
        models.forEach(model -> setModelRefDataFetcher(provider, model));
    }

    /**
     * add all entity to map
     *
     * @param entities entity list
     */
    public void addEntities(List<Entity> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        entities.forEach(entity -> entityMap.put(entity.getName(), entity));
    }

    private void setModelRefDataFetcher(TpDocGraphqlProviderServiceInfo provider, ProviderModelInfo model) {
        Set<String> refIds = model.getRefIds();
        if (CollectionUtils.isEmpty(refIds)) {
            return;
        }
        String idProvider = model.getIdProvider();
        if (StringUtils.isEmpty(idProvider)) {
            return;
        }

        Api idProviderApi = apiMap.get(idProvider);
        if (idProviderApi == null) {
            return;
        }

        String idsProvider = model.getIdsProvider();
        Api idsProviderApi = null;
        if (!StringUtils.isEmpty(idsProvider)) {
            idsProviderApi = apiMap.get(idsProvider);
        }

        for (String refId : refIds) {
            String providerName = GraphqlConsts.STR_AT + refId;
            BaseDataFetcher existsDataFetcher = dataFetcherMap.get(providerName);
            if (existsDataFetcher != null) {
                //重复了！
                throw new GraphqlBuildException("重复GraphqlProvider,name=" + providerName + "在：" + existsDataFetcher.getModuleName() + GraphqlConsts.STR_COMMA + provider.getModuleName());
            }
            DataFetcherProxy idDataFetcher = new DataFetcherProxy(idProviderApi, provider);
            idDataFetcher.setDependencyFields(Lists.newArrayList(refId));
            idDataFetcher.addExtraSelection(GraphqlConsts.STR_ID.toLowerCase(), refId);
            idDataFetcher.setGraphqlProviderName(providerName);
            dataFetcherMap.put(providerName, idDataFetcher);
            apiMap.put(providerName, idProviderApi);

            if (idsProviderApi == null) {
                return;
            }
            idsProviderApi.setBatchProvider(true);
            DataFetcherProxy idsDataFetcher = new DataFetcherProxy(idsProviderApi, provider);
            idsDataFetcher.setDependencyFields(Lists.newArrayList(refId + GraphqlConsts.STR_S));
            idsDataFetcher.addExtraSelection(GraphqlConsts.STR_IDS, refId + GraphqlConsts.STR_S);
            //批量请求
            providerName += GraphqlConsts.STR_S;
            idsDataFetcher.setGraphqlProviderName(providerName);
            existsDataFetcher = dataFetcherMap.get(providerName);
            if (existsDataFetcher != null) {
                //重复了！
                throw new GraphqlBuildException("重复GraphqlProvider,name=" + providerName + "!在：" + existsDataFetcher.getModuleName() + GraphqlConsts.STR_COMMA + provider.getModuleName());
            }
            dataFetcherMap.put(providerName, idsDataFetcher);
            apiMap.put(providerName, idsProviderApi);
        }
    }

    private void setApi(String moduleName, Api api) {
        api.setModuleName(moduleName);
        String apiCode = api.getCode();
        Api existsApi = apiMap.get(apiCode);
        if (existsApi != null) {
            throw new GraphqlBuildException("重复方法签名:" + existsApi + GraphqlConsts.STR_COMMA + api);
        }

        apiMap.put(apiCode, api);

        String providerName = api.getProviderName();
        if (Strings.isNullOrEmpty(providerName)) {
            return;
        }
        providerName = GraphqlConsts.STR_AT + providerName;
        Boolean batchProvider = api.getBatchProvider();
        if (batchProvider != null && batchProvider) {
            //批量接口
            providerName += GraphqlConsts.STR_EXCLAMATION;
        }
        existsApi = apiMap.get(providerName);
        if (existsApi != null) {
            if (existsApi == api) {
                //同一个接口
                return;
            }
            throw new GraphqlBuildException("重复方法签名:providerName=" + providerName + GraphqlConsts.STR_COMMA + existsApi + GraphqlConsts.STR_COMMA + api);
        }

        apiMap.put(providerName, api);
    }

    /**
     * add Inner Provider context
     *
     * @param moduleContext Inner Provider context
     */
    public void putInnerProvider(GraphqlModuleContext moduleContext) {
        InnerProvider innerProvider = moduleContext.getInnerProvider();
        if (innerProvider == null) {
            return;
        }
        Map<String, GraphQLType> innerGraphQLTypeMap = innerProvider.getGraphQLTypeMap();
        if (CollectionUtils.isEmpty(innerGraphQLTypeMap)) {
            logger.warn("领域[{}]没有可用GraphQLType", moduleContext.getModuleName());
            return;
        }
        this.graphQLTypeMap.putAll(innerGraphQLTypeMap);

        Map<String, BaseDataFetcher> refIdDataFetcherMap = innerProvider.getRefIdDataFetcherMap();
        if (CollectionUtils.isEmpty(refIdDataFetcherMap)) {
            return;
        }
        dataFetcherMap.putAll(refIdDataFetcherMap);
        moduleContextMap.put(innerProvider.getModuleName(), moduleContext);
    }

    /**
     * 添加GraphQLTypes
     *
     * @param graphQLTypeMap GraphQLTypes
     */
    public void putAllGraphQLTypes(Map<String, GraphQLType> graphQLTypeMap) {
        if (graphQLTypeMap == null) {
            return;
        }
        this.graphQLTypeMap.putAll(graphQLTypeMap);
    }

    /**
     * 通过类名，获取GraphQL对应的类型
     *
     * @param entityName 类名
     * @return GraphQLType
     */
    public GraphQLType getGraphQLType(String entityName) {
        return graphQLTypeMap.get(entityName);
    }

    /**
     * 添加GraphQL类型
     *
     * @param entityName  实体名称
     * @param graphQLType GraphQLType
     */
    public void addGraphQLType(String entityName, GraphQLType graphQLType) {
        graphQLTypeMap.put(entityName, graphQLType);
    }

    /**
     * 通过实体名称，获取实体定义
     *
     * @param entityName 实体名称
     * @return 实体定义，获取不到时返回null
     */
    public Entity getEntity(String entityName) {
        return entityMap.get(entityName);
    }

    /**
     * 通过代码获取ModuleDataFetcher
     *
     * @param code 编码，可以是api.code / providerName / refId
     * @return ModuleDataFetcher实体，查询不到返回null
     */
    public BaseDataFetcher getDataFetcher(String code) {
        return dataFetcherMap.get(code);
    }

    /**
     * 通过领域名称获取领域上下文信息
     *
     * @param moduleName 领域名称
     * @return 领域上下文信息
     */
    public GraphqlModuleContext getModuleContext(String moduleName) {
        return moduleContextMap.get(moduleName);
    }

    /**
     * 注册一个字段的DataFetcher
     *
     * @param typeName    类型名称
     * @param fieldName   字段名称
     * @param dataFetcher DataFetcher
     */
    public void codeRegistry(String typeName, String fieldName, BaseDataFetcher dataFetcher) {
        GraphqlContextUtils.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(dataFetcher);
        FieldCoordinates coordinates = FieldCoordinates.coordinates(typeName, fieldName);
        graphQLCodeRegistryBuilder.dataFetcher(
                coordinates,
                dataFetcher
        );
        dataFetcherMap.put(typeName + GraphqlConsts.CHAR_DOT + fieldName, dataFetcher);
    }

    /**
     * 向某个领域添加一个查询字段定义
     *
     * @param moduleName      领域名称
     * @param fieldDefinition 查询字段定义
     */
    public void addQueryFieldDefinition(String moduleName, GraphQLFieldDefinition fieldDefinition) {
        moduleQueryFieldDefinitionMap.computeIfAbsent(moduleName, mn -> Lists.newArrayList()).add(fieldDefinition);
    }

    /**
     * 向某个领域添加多个查询字段定义
     *
     * @param moduleName       领域名称
     * @param fieldDefinitions 查询字段定义
     */
    public void addQueryFieldDefinitions(String moduleName, List<GraphQLFieldDefinition> fieldDefinitions) {
        moduleQueryFieldDefinitionMap.computeIfAbsent(moduleName, mn -> Lists.newArrayList()).addAll(fieldDefinitions);
    }

    /**
     * 向某个领域添加一个写字段定义
     *
     * @param moduleName      领域名称
     * @param fieldDefinition 写字段定义
     */
    public void addMutationFieldDefinition(String moduleName, GraphQLFieldDefinition fieldDefinition) {
        moduleMutationFieldDefinitionMap.computeIfAbsent(moduleName, mn -> Lists.newArrayList()).add(fieldDefinition);
    }

    /**
     * 向某个领域添加多个写字段定义
     *
     * @param moduleName       领域名称
     * @param fieldDefinitions 多个写字段定义
     */
    public void addMutationFieldDefinitions(String moduleName, List<GraphQLFieldDefinition> fieldDefinitions) {
        moduleMutationFieldDefinitionMap.computeIfAbsent(moduleName, mn -> Lists.newArrayList()).addAll(fieldDefinitions);
    }

    /**
     * 通过API code获取api
     *
     * @param code api.code / graphqlProviderName
     * @return 返回Api实例，如果找不到则返回null
     */
    public Api getApi(String code) {
        return apiMap.get(code);
    }

    public Set<GraphQLType> getGraphQLTypes() {
        return graphQLTypeMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    /**
     * building type is exists
     *
     * @param typeName type name
     * @return exists if type name is building or builded
     */
    public boolean buildingType(String typeName) {
        return creatingGraphQLTypes.contains(typeName);
    }

    /**
     * add building or builded type name
     *
     * @param typeName type name
     */
    public void addBuildingType(String typeName) {
        creatingGraphQLTypes.add(typeName);
    }

    public GraphQLCodeRegistry.Builder getCodeRegistry() {
        return graphQLCodeRegistryBuilder;
    }

    public Map<String, Api> getApiMap() {
        return apiMap;
    }

    public Map<String, List<GraphQLFieldDefinition>> getModuleQueryFieldDefinitionMap() {
        return moduleQueryFieldDefinitionMap;
    }

    public Map<String, List<GraphQLFieldDefinition>> getModuleMutationFieldDefinitionMap() {
        return moduleMutationFieldDefinitionMap;
    }

    public String getSchemaName() {
        return schemaName;
    }
}
