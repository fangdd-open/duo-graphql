package com.fangdd.graphql.pipeline;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.exception.GraphqlBuildException;
import com.fangdd.graphql.core.util.GraphqlTypeUtils;
import com.fangdd.graphql.dto.TpdocBaseProviderApiDto;
import com.fangdd.graphql.dto.TpdocBaseProviderDto;
import com.fangdd.graphql.fetcher.DataFetcherProxy;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.fangdd.graphql.provider.dto.provider.EntityRef;
import com.fangdd.graphql.provider.dto.provider.ProviderApiDto;
import com.fangdd.graphql.scalar.JsonScalar;
import com.fangdd.graphql.service.JsonService;
import com.fangdd.graphql.service.TpdocService;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 远端RestFul接口注入（需要基于TpDoc文档）
 * 本方法会去扫描classpaths下的*.
 *
 * @author xuwenzhen
 */
@Service
public class TpdocBaseRestBuildPipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(TpdocBaseRestBuildPipeline.class);

    @Value("${fdd.graphql.tpdoc.rest.providers:}")
    private String tpdocBassRestProviders;

    @Autowired
    private TpdocService tpdocService;

    @Autowired
    private JsonService jsonService;

    /**
     * 处理
     *
     * @param registryState 当前注册信息
     */
    @Override
    public void doPipeline(RegistryState registryState) {
        if (Strings.isNullOrEmpty(tpdocBassRestProviders)) {
            return;
        }
        Splitter
                .on(GraphqlConsts.STR_COMMA)
                .omitEmptyStrings()
                .trimResults()
                .split(tpdocBassRestProviders)
                .forEach(configName -> registryTpdocBaseApis(registryState, configName));
    }

    /**
     * 执行的顺序
     *
     * @return 数值，越小越前
     */
    @Override
    public int order() {
        return 1000;
    }

    private void registryTpdocBaseApis(RegistryState registryState, String configName) {
        String apiConfig = getResourceFileAsString(configName);
        if (Strings.isNullOrEmpty(apiConfig)) {
            logger.warn("配置文件：{}内容为空，跳过！", configName);
            return;
        }

        TpdocBaseProviderDto tpdocBaseProvider = jsonService.toObject(apiConfig, TpdocBaseProviderDto.class);
        if (tpdocBaseProvider == null || CollectionUtils.isEmpty(tpdocBaseProvider.getModules())) {
            logger.warn("配置文件：{}内容异常，跳过！", configName);
            return;
        }

        StringBuilder apiCodes = new StringBuilder();
        tpdocBaseProvider.getModules().forEach(module -> {
            List<TpdocBaseProviderApiDto> apis = module.getApis();
            if (CollectionUtils.isEmpty(apis)) {
                return;
            }
            apis.forEach(api -> apiCodes.append(GraphqlConsts.STR_COMMA).append(api.getCode()));
        });
        if (apiCodes.length() == 0) {
            logger.warn("配置文件：{} 未指定api.code，跳过！", configName);
            return;
        }
        apiCodes.deleteCharAt(0);
        //faac39aa3ea96ed18db900a24cb187946e1b63b4
        ProviderApiDto providerApiDto = tpdocService.fetchDocData(tpdocBaseProvider.getAppId(), null, apiCodes.toString());
        if (providerApiDto == null) {
            logger.warn("配置文件：{} 拉取接口文档信息失败，appId={}, apiCodes={}！", configName, tpdocBaseProvider.getAppId(), apiCodes.toString());
            return;
        }

        registryTpdocBaseApis(registryState, tpdocBaseProvider, providerApiDto);
    }

    private void registryTpdocBaseApis(
            RegistryState registryState,
            TpdocBaseProviderDto tpdocBaseProvider,
            ProviderApiDto providerApiDto
    ) {
        Map<String, Api> apiMap = Maps.newHashMap();
        providerApiDto.getApis().forEach(api -> apiMap.put(api.getCode(), api));
        registryState.addEntities(providerApiDto.getEntities());

        tpdocBaseProvider.getModules().forEach(module -> module.getApis().forEach(fieldConf -> registryApi(
                registryState, tpdocBaseProvider, apiMap, module.getName(), fieldConf
        )));
    }

    private void registryApi(
            RegistryState registryState,
            TpdocBaseProviderDto tpdocBaseProvider,
            Map<String, Api> apiMap,
            String moduleName,
            TpdocBaseProviderApiDto fieldConf
    ) {
        Api api = apiMap.get(fieldConf.getCode());
        if (api == null) {
            logger.warn("无法找到appId={}, moduleNam={}，对应的接口apiCode={}", tpdocBaseProvider.getAppId(), moduleName, fieldConf.getCode());
            return;
        }

        String queryName = fieldConf.getQueryName();
        if (Strings.isNullOrEmpty(queryName)) {
            queryName = api.getName();
        }

        GraphQLFieldDefinition.Builder graphQLFieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(queryName)
                .type(GraphqlConsts.JSON_SCALAR);
        if (!Strings.isNullOrEmpty(api.getComment())) {
            graphQLFieldDefinitionBuilder.description(api.getName() + GraphqlConsts.STR_TURN_LINE + api.getComment());
        } else {
            graphQLFieldDefinitionBuilder.description(api.getName());
        }
        List<GraphQLArgument> arguments = Lists.newArrayList();
        List<EntityRef> requestParams = api.getRequestParams();
        if (!CollectionUtils.isEmpty(requestParams)) {
            requestParams.forEach(param -> {
                GraphQLArgument.Builder graphQLArgumentBuilder = GraphQLArgument.newArgument().name(param.getName());
                GraphQLInputType type = (GraphQLInputType) GraphqlTypeUtils.getGraphqlInputType(registryState, moduleName, param.getEntityName());
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

        DataFetcherProxy dataFetcher = new DataFetcherProxy(api, getVirtualProvider(moduleName, tpdocBaseProvider));
        dataFetcher.setDataPath(fieldConf.getDataPath());

        if (fieldConf.getActionName() != null && fieldConf.getActionName().equalsIgnoreCase(GraphqlConsts.MUTATION)) {
            registryState.addMutationFieldDefinition(moduleName, graphQLFieldDefinitionBuilder.build());
            registryState.codeRegistry(GraphqlConsts.STR_M + moduleName.toUpperCase(), queryName, dataFetcher);
        } else {
            registryState.addQueryFieldDefinition(moduleName, graphQLFieldDefinitionBuilder.build());
            registryState.codeRegistry(moduleName.toUpperCase(), queryName, dataFetcher);
        }
    }

    private TpDocGraphqlProviderServiceInfo getVirtualProvider(String moduleName, TpdocBaseProviderDto tpdocBaseProvider) {
        TpDocGraphqlProviderServiceInfo provider = new TpDocGraphqlProviderServiceInfo();
        provider.setModuleName(moduleName);
        provider.setAppId(tpdocBaseProvider.getAppId());
        provider.setServer(tpdocBaseProvider.getServer());
        return provider;
    }

    private String getResourceFileAsString(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new GraphqlBuildException("读取资源文件[" + resourceName + "]失败！", e);
        }
        return null;
    }


}
