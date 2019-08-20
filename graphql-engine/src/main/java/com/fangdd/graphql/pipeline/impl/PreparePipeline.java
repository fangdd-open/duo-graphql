package com.fangdd.graphql.pipeline.impl;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.config.GraphqlProviderConfigure;
import com.fangdd.graphql.core.util.GraphqlContextUtils;
import com.fangdd.graphql.pipeline.Pipeline;
import com.fangdd.graphql.pipeline.RegistryState;
import com.fangdd.graphql.provider.InnerProvider;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 准备一些基础工作
 *
 * @author xuwenzhen
 * @date 2019/6/5
 */
@Service
public class PreparePipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(PreparePipeline.class);

    @Autowired(required = false)
    private List<InnerProvider> innerProviders;

    @Autowired
    private GraphqlProviderConfigure graphqlProviderConfigure;

    /**
     * 处理
     *
     * @param registryState 当前注册信息
     */
    @Override
    public void doPipeline(RegistryState registryState) {
        if (CollectionUtils.isEmpty(innerProviders)) {
            return;
        }
        String schemaName = registryState.getSchemaName();
        Map<String, String> innerProviderSchema = graphqlProviderConfigure.getInnerProviderSchema();
        if (CollectionUtils.isEmpty(innerProviderSchema)) {
            logger.warn("未配置：graphql.provider.innerProviderSchema[{}]", schemaName);
            return;
        }

        String innerProviderNames = innerProviderSchema.get(schemaName);
        if (StringUtils.isEmpty(schemaName)) {
            return;
        }
        Set<String> providerNames = Sets.newHashSet();
        Splitter
                .on(GraphqlConsts.STR_COMMA)
                .omitEmptyStrings()
                .trimResults()
                .split(innerProviderNames)
                .forEach(providerNames::add);
        if (CollectionUtils.isEmpty(providerNames)) {
            return;
        }
        List<InnerProvider> registryInnerProviders = Lists.newArrayList();
        innerProviders.forEach(provider -> {
            String moduleName = provider.getModuleName();
            if (!providerNames.contains(moduleName)) {
                return;
            }
            registryInnerProviders.add(provider);
            if (provider.getQueryType() != null) {
                registryState.addQueryFieldDefinitions(moduleName, provider.getQueryType().getFieldDefinitions());
            }
            if (provider.getMutationType() != null) {
                registryState.addMutationFieldDefinitions(moduleName, provider.getMutationType().getFieldDefinitions());
            }
        });
        if (!CollectionUtils.isEmpty(registryInnerProviders)) {
            GraphqlContextUtils.getGraphqlContext(schemaName).registryInnerProvider(registryInnerProviders);
        }
    }

    /**
     * 执行的顺序
     *
     * @return 数值，越小越前
     */
    @Override
    public int order() {
        return 0;
    }
}
