package com.fangdd.graphql.core.config;

import com.fangdd.graphql.core.GraphqlConsts;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xuwenzhen
 * @date 2019/4/2
 */
@Component
@ConfigurationProperties(prefix = "graphql.provider")
public class GraphqlProviderConfigure {
    private Map<String, String> providerService;

    private Map<String, String> innerProviderSchema;

    private Map<String, String> urlSchemaMap;

    public String getProviderService(String appId) {
        if (providerService == null) {
            return null;
        }
        return providerService.get(appId);
    }

    public void setProviderService(Map<String, String> providerService) {
        this.providerService = providerService;
    }

    public Map<String, String> getInnerProviderSchema() {
        return innerProviderSchema;
    }

    public void setInnerProviderSchema(Map<String, String> innerProviderSchema) {
        this.innerProviderSchema = innerProviderSchema;
    }

    public Map<String, String> getUrlSchemaMap() {
        return urlSchemaMap;
    }

    public void setUrlSchemaMap(Map<String, String> urlSchemaMap) {
        this.urlSchemaMap = urlSchemaMap;
    }

    public String getUrlSchemaName(String url) {
        return urlSchemaMap.computeIfAbsent(url, u -> GraphqlConsts.STR_DEFAULT);
    }
}
