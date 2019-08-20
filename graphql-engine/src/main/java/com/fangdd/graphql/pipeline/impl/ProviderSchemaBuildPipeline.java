package com.fangdd.graphql.pipeline.impl;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.GraphqlModuleContext;
import com.fangdd.graphql.core.exception.GraphqlBuildException;
import com.fangdd.graphql.core.util.GraphqlTypeUtils;
import com.fangdd.graphql.fetcher.DataFetcherProxy;
import com.fangdd.graphql.pipeline.Pipeline;
import com.fangdd.graphql.pipeline.RegistryState;
import com.fangdd.graphql.provider.BaseDataFetcher;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.fangdd.graphql.provider.dto.provider.Entity;
import com.fangdd.graphql.provider.dto.provider.EntityRef;
import com.fangdd.graphql.provider.dto.provider.ProviderApiDto;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 构建ProviderSchema
 *
 * @author xuwenzhen
 * @date 2019/6/4
 */
@Service
public class ProviderSchemaBuildPipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(ProviderSchemaBuildPipeline.class);

    /**
     * 处理
     *
     * @param registryState 当前注册信息
     */
    @Override
    public void doPipeline(RegistryState registryState) {
        registryState.getModuleContextMap().entrySet().forEach(
                entry -> buildProviderSchema(registryState, entry.getValue())
        );
    }

    /**
     * 执行的顺序
     *
     * @return 数值，越小越前
     */
    @Override
    public int order() {
        return 300;
    }

    private void buildProviderSchema(RegistryState state, GraphqlModuleContext moduleContext) {
        if (moduleContext.getInnerProvider() != null) {
            //inner provider
            return;
        }

        TpDocGraphqlProviderServiceInfo provider = moduleContext.getProvider();
        ProviderApiDto apiDto = moduleContext.getProviderApi();
        if (apiDto == null || CollectionUtils.isEmpty(apiDto.getApis())) {
            logger.warn("服务{}，无可构建接口！", provider.getAppId());
            return;
        }

        logger.info("准备注册服务：{}", provider.getAppId());
        apiDto.getApis().forEach(api -> buildApiGraphQLFieldDefinition(state, moduleContext, api));
    }

    /**
     * 构建某个API对应的字段定义
     *
     * @param registryState 当前构建的状态
     * @param moduleContext Remote Provider Context
     * @param api           当前需要构建的API
     */
    private void buildApiGraphQLFieldDefinition(RegistryState registryState, GraphqlModuleContext moduleContext, Api api) {
        //接口名称（取方法名）
        String apiName = getModuleApiName(api);
        GraphQLType responseType;
        if (api.getGraphqlJson() != null && api.getGraphqlJson()) {
            //被标识为 @GraphqlJson时
            responseType = GraphqlConsts.JSON_SCALAR;
        } else {
            EntityRef response = api.getResponse();
            responseType = getGraphqlOutputType(registryState, moduleContext, response);
            if (responseType == null) {
                logger.error("接口：{} {}(...)响应体异常", response.getEntityName(), api.getCode());
                return;
            }
        }
        GraphQLFieldDefinition.Builder graphQLFieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(apiName)
                .type((GraphQLOutputType) responseType);
        if (!StringUtils.isEmpty(api.getDeprecated())) {
            graphQLFieldDefinitionBuilder.deprecate(api.getDeprecated());
        }
        if (!StringUtils.isEmpty(api.getComment())) {
            graphQLFieldDefinitionBuilder.description(api.getName() + GraphqlConsts.STR_TURN_LINE + api.getComment());
        } else {
            graphQLFieldDefinitionBuilder.description(api.getName());
        }

        List<GraphQLArgument> arguments = Lists.newArrayList();
        List<EntityRef> requestParams = api.getRequestParams();
        if (!CollectionUtils.isEmpty(requestParams)) {
            requestParams.forEach(param -> {
                GraphQLArgument.Builder graphQLArgumentBuilder = GraphQLArgument.newArgument().name(param.getName());
                GraphQLInputType type = (GraphQLInputType) GraphqlTypeUtils.getGraphqlInputType(registryState, moduleContext.getModuleName(), param.getEntityName());
                Assert.notNull(type, "typ不允许为空：" + param.getEntityName());
                boolean required = param.isRequired() || GraphqlTypeUtils.PATH_VARIABLE.equals(param.getAnnotation()) || GraphqlTypeUtils.REQUEST_BODY.equals(param.getAnnotation());
                if (required) {
                    //必填的
                    graphQLArgumentBuilder.type(GraphQLNonNull.nonNull(type));
                } else {
                    graphQLArgumentBuilder.type(type);
                }

                arguments.add(graphQLArgumentBuilder.build());
            });
            graphQLFieldDefinitionBuilder.arguments(arguments);
        }
        DataFetcherProxy dataFetcher = new DataFetcherProxy(api, moduleContext.getProvider());

        String bindingModuleName = moduleContext.getApiBindGraphqlModuleName(api.getCode());
        if (isMutation(api)) {
            //如果是Mutation操作
            registryState.addMutationFieldDefinition(bindingModuleName, graphQLFieldDefinitionBuilder.build());
            registryState.codeRegistry(GraphqlConsts.STR_M + bindingModuleName.toUpperCase(), apiName, dataFetcher);
        } else {
            registryState.addQueryFieldDefinition(bindingModuleName, graphQLFieldDefinitionBuilder.build());
            registryState.codeRegistry(bindingModuleName.toUpperCase(), apiName, dataFetcher);
        }
    }



    /**
     * get graphql type field type
     *
     * @param registryState 当前构建上下文
     * @param moduleContext 当前模块
     * @param entityRef     当前字段定义
     * @return GraphQLType
     */
    private GraphQLType getGraphqlOutputType(RegistryState registryState, GraphqlModuleContext moduleContext, EntityRef entityRef) {
        String entityName = entityRef.getEntityName();
        GraphQLType baseGraphQLType = GraphqlTypeUtils.getBaseGraphQLType(entityName);
        if (baseGraphQLType != null) {
            return baseGraphQLType;
        }
        Entity entity = registryState.getEntity(entityName);
        if (entity == null) {
            logger.warn("找不到对应的类型：{}", entityName);
            return null;
        }

        Boolean map = entity.getMap();
        if (map != null && map) {
            // Map
            logger.warn("暂不支持Map类型，忽略: {} #{}", entityRef.getName(), entityRef.getComment());
            return null;
        }

        Boolean collection = entity.getCollection();
        if (collection != null && collection) {
            // 列表
            return getGraphQLListType(registryState, moduleContext, entity);
        }

        Boolean enumerate = entity.getEnumerate();
        if (enumerate != null && enumerate) {
            // 枚举
            return addAndGetGraphQLEnumType(registryState, moduleContext, entity);
        }

        GraphQLType graphQLType = registryState.getGraphQLType(entityName);
        if (graphQLType != null) {
            return graphQLType;
        }

        //Pojo，如果没有找到对应的类型时
        if (CollectionUtils.isEmpty(entity.getFields())) {
            logger.warn("{}类型无有效属性，忽略！", entityName);
            return null;
        }

        return addAndGetOutputGraphQLType(registryState, moduleContext, entity);
    }

    private GraphQLType getGraphQLListType(RegistryState registryState, GraphqlModuleContext moduleContext, Entity entity) {
        if (CollectionUtils.isEmpty(entity.getParameteredEntityRefs())) {
            throw new GraphqlBuildException("泛型类型未指定:" + entity.getName());
        }
        EntityRef parameteredEntityRef = entity.getParameteredEntityRefs().get(0);
        GraphQLOutputType paramGraphQLOutputType = (GraphQLOutputType) getGraphqlOutputType(registryState, moduleContext, parameteredEntityRef);
        if (paramGraphQLOutputType == null) {
            throw new GraphqlBuildException("无法转换泛型类型:" + entity.getName());
        }
        return GraphQLList.list(paramGraphQLOutputType);
    }

    /**
     * 获取Pojo的GraphQLType
     */
    private GraphQLType addAndGetOutputGraphQLType(RegistryState registryState, GraphqlModuleContext moduleContext, Entity entity) {
        String moduleName = moduleContext.getProvider().getModuleName();
        String typeName = GraphqlTypeUtils.getModuleTypeName(entity, moduleName);

        if (typeName != null && registryState.buildingType(typeName)) {
            return GraphQLTypeReference.typeRef(typeName);
        }
        registryState.addBuildingType(typeName);

        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject().name(typeName);
        entity.getFields().forEach(field -> {
            String fieldName = field.getName();
            GraphQLFieldDefinition.Builder fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name(fieldName);
            if (!StringUtils.isEmpty(field.getComment())) {
                fieldDefinition.description(field.getComment());
            }

            GraphQLOutputType graphQLOutputType = buildGraphqlField(registryState, typeName, field);
            if (graphQLOutputType != null) {
                //通过@GraphqlField注解绑定
                fieldDefinition.type(graphQLOutputType);
                objectTypeBuilder.field(fieldDefinition);
                return;
            }

            GraphQLOutputType fieldType = (GraphQLOutputType) getGraphqlOutputType(registryState, moduleContext, field);
            if (fieldType == null) {
                return;
            }
            fieldDefinition.type(fieldType);
            objectTypeBuilder.field(fieldDefinition);

            //检查是否需要关联实体
            buildRefField(registryState, objectTypeBuilder, typeName, fieldName);
        });

        GraphQLType graphQLType = objectTypeBuilder.build();
        registryState.addGraphQLType(entity.getName(), graphQLType);
        return graphQLType;
    }

    private boolean isMutation(Api api) {
        List<String> methods = api.getMethods();
        return CollectionUtils.isEmpty(methods) || !HttpMethod.GET.name().equalsIgnoreCase(methods.get(0));
    }

    private void buildRefField(RegistryState registryState, GraphQLObjectType.Builder objectTypeBuilder, String typeName, String fieldName) {
        BaseDataFetcher refDataFetcher = registryState.getDataFetcher(GraphqlConsts.STR_AT + fieldName);
        if (refDataFetcher == null) {
            return;
        }

        GraphqlModuleContext moduleContext;
        GraphQLOutputType responseType;
        if (DataFetcherProxy.class.isInstance(refDataFetcher)) {
            DataFetcherProxy dataFetcherProxy = (DataFetcherProxy) refDataFetcher;
            Api api = dataFetcherProxy.getApi();
            moduleContext = registryState.getModuleContext(api.getModuleName());
            EntityRef response = api.getResponse();
            responseType = (GraphQLOutputType) getGraphqlOutputType(registryState, moduleContext, response);
            if (responseType == null) {
                logger.error("接口：{} {}(...)响应体异常", response.getEntityName(), api.getCode());
                return;
            }
        } else {
            responseType = refDataFetcher.getResponseGraphqlType();
            if (responseType == null) {
                logger.error("{}.{}.{}未指定InnerProvider DataFetcher响应体!", refDataFetcher.getModuleName(), typeName, fieldName);
                return;
            }
        }
        //自动绑定实体
        List<String> refFields = refDataFetcher.getDependencyFields();
        String refFieldName = refFields.get(0);
        String newFieldName = newRefTypeName(refFieldName);


        GraphQLFieldDefinition refField = GraphQLFieldDefinition
                .newFieldDefinition()
                .name(newFieldName)
                .description("字段" + refFieldName + "关联实体")
                .type(responseType)
                .build();
        objectTypeBuilder.field(refField);
        registryState.codeRegistry(typeName, newFieldName, refDataFetcher);
    }

    /**
     * 处理被标注为@GraphqlField的字段
     *
     * @param registryState 当前构建上下文
     * @param typeName      当前所属的GraphQLType名称
     * @param field         当前字段定义
     * @return 处理GraphqlField解析出来的类型，如果未解析成功，则返回null
     */
    private GraphQLOutputType buildGraphqlField(
            RegistryState registryState,
            String typeName,
            EntityRef field
    ) {
        //检查是否指定关联
        String graphqlFieldConf = field.getGraphqlField();
        if (StringUtils.isEmpty(graphqlFieldConf)) {
            return null;
        }
        String[] fieldConfigure = graphqlFieldConf.split(GraphqlConsts.STR_CLN);
        String graphqlProviderName = fieldConfigure[0];
        Api api = registryState.getApi(graphqlProviderName);
        if (api == null) {
            logger.warn("{}.{}关联不上{}，丢弃！", typeName, field.getName(), graphqlFieldConf);
            return null;
        }

        //使用API里的模块名称
        String moduleName = api.getModuleName();
        GraphqlModuleContext moduleContext = registryState.getModuleContext(moduleName);
        DataFetcherProxy dataFetcher = new DataFetcherProxy(api, moduleContext.getProvider());
        dataFetcher.setGraphqlProviderName(graphqlProviderName);

        List<String> dependencyFields = Lists.newArrayList();
        String providerParamConfs = fieldConfigure[1];
        Splitter
                .on(GraphqlConsts.STR_COMMA)
                .omitEmptyStrings()
                .trimResults()
                .split(providerParamConfs)
                .forEach(dependencyFields::add);
        dataFetcher.setExtraSelections(dependencyFields);

        //绑定DataFetch
        registryState.codeRegistry(typeName, field.getName(), dataFetcher);

        return (GraphQLOutputType) getGraphqlOutputType(registryState, moduleContext, api.getResponse());
    }

    /**
     * create enum type
     */
    private GraphQLEnumType addAndGetGraphQLEnumType(RegistryState registryState, GraphqlModuleContext moduleContext, Entity entity) {
        String name = GraphqlTypeUtils.getModuleTypeName(entity, moduleContext.getProvider().getModuleName());
        GraphQLType type = registryState.getGraphQLType(name);

        if (type != null) {
            return (GraphQLEnumType) type;
        }
        if (CollectionUtils.isEmpty(entity.getFields())) {
            throw new GraphqlBuildException("枚举:" + entity.getName() + ",元素为空！");
        }

        GraphQLEnumType.Builder enumTypeBuilder = GraphQLEnumType.newEnum().name(name);
        if (!StringUtils.isEmpty(entity.getComment())) {
            enumTypeBuilder.description(entity.getComment());
        }

        entity.getFields().forEach(field -> {
            GraphQLEnumValueDefinition.Builder enumValueBuild =
                    GraphQLEnumValueDefinition.newEnumValueDefinition().name(field.getName());
            if (!StringUtils.isEmpty(field.getComment())) {
                enumValueBuild.description(field.getComment());
            }

            enumTypeBuilder.value(enumValueBuild.build());
        });
        GraphQLEnumType graphQLEnumType = enumTypeBuilder.build();
        registryState.addGraphQLType(name, graphQLEnumType);
        return graphQLEnumType;
    }

    private String newRefTypeName(String name) {
        return name.replace(GraphqlConsts.STR_ID, GraphqlConsts.STR_EMPTY);
    }


    private String getModuleApiName(Api api) {
        String code = api.getCode();
        int index = code.lastIndexOf(GraphqlConsts.CHAR_DOT);
        if (index != -1) {
            code = code.substring(index + 1);
        }
        return code;
    }
}
