package com.fangdd.graphql.core.util;

import com.fangdd.graphql.core.GraphqlContext;
import com.fangdd.graphql.pipeline.RegistryState;
import com.fangdd.graphql.provider.InnerProvider;
import com.fangdd.graphql.provider.dto.provider.Entity;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Maps;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * @author xuwenzhen
 * @date 2019/8/16
 */
public class GraphqlContextUtils {
    public static final Cache<String, Object> CACHE = Caffeine.newBuilder().maximumSize(10_000).build();

    private static final Map<String, GraphqlContext> GRAPHQL_CONTEXT_MAP = Maps.newConcurrentMap();

    /**
     * Spring 上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 当前可用的InnerProviders
     */
    private static List<InnerProvider> innerProviders;

    private GraphqlContextUtils() {
    }

    /**
     * 通过schema对应的GraphqlContext
     *
     * @param schemaName schema名称
     * @return 对应的GraphqlContext实例
     */
    public static GraphqlContext getGraphqlContext(String schemaName) {
        return GRAPHQL_CONTEXT_MAP.get(schemaName);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        GraphqlContextUtils.applicationContext = applicationContext;
    }

    public static Entity getEntity(String entityName) {
        for (Map.Entry<String, GraphqlContext> entry : GRAPHQL_CONTEXT_MAP.entrySet()) {
            GraphqlContext graphqlContext = entry.getValue();
            Entity entity = graphqlContext.getEntity(entityName);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    public static void setInnerProviders(List<InnerProvider> innerProviders) {
        GraphqlContextUtils.innerProviders = innerProviders;
    }

    public static List<InnerProvider> getInnerProviders() {
        return innerProviders;
    }

    public static void registry(RegistryState registryState) {
        GraphqlContext graphqlContext = new GraphqlContext(registryState);
        GRAPHQL_CONTEXT_MAP.put(registryState.getSchemaName(), graphqlContext);
    }
}
