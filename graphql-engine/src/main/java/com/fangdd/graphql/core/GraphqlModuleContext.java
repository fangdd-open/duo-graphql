package com.fangdd.graphql.core;

import com.fangdd.graphql.provider.InnerProvider;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.ProviderApiDto;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import graphql.schema.DataFetcher;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 某个领域的上下文环境
 *
 * @author xuwenzhen
 * @date 2019/6/3
 */
public class GraphqlModuleContext {
    /**
     * GraphQL Provider服务信息
     */
    private TpDocGraphqlProviderServiceInfo provider;

    /**
     * GraphQL Provider接口文档
     */
    private ProviderApiDto providerApi;

    /**
     * Inner Provider实例
     */
    private InnerProvider innerProvider;

    /**
     * 领域名称：XF | ESF
     */
    private String moduleName;

    /**
     * 别的Module的Controller
     */
    private Map<String, String> outsizeModuleController;

    private Map<String[], DataFetcher> codeRegistryMap;

    public GraphqlModuleContext(ProviderApiDto providerApi, TpDocGraphqlProviderServiceInfo provider) {
        this.provider = provider;
        this.providerApi = providerApi;
        this.moduleName = provider.getModuleName().toUpperCase();
        filteOutsizeModule();
    }

    public GraphqlModuleContext(InnerProvider innerProvider) {
        this.innerProvider = innerProvider;
        this.moduleName = innerProvider.getModuleName().toUpperCase();
        Map<String, DataFetcher> fieldDataFetcherMap = innerProvider.getFieldDataFetcherMap();
        if (fieldDataFetcherMap != null) {
            codeRegistryMap = Maps.newHashMap();
            fieldDataFetcherMap.entrySet().forEach(entry -> {
                String fields = entry.getKey();
                int index = fields.indexOf(GraphqlConsts.CHAR_DOT);
                String fieldName = fields.substring(index + 1);
                DataFetcher dataFetcher = entry.getValue();
                codeRegistryMap.put(new String[]{moduleName, fieldName}, dataFetcher);
            });
        }
    }

    public TpDocGraphqlProviderServiceInfo getProvider() {
        return provider;
    }

    public ProviderApiDto getProviderApi() {
        return providerApi;
    }

    public InnerProvider getInnerProvider() {
        return innerProvider;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Map<String[], DataFetcher> getCodeRegistryMap() {
        return codeRegistryMap;
    }

    private void filteOutsizeModule() {
        if (CollectionUtils.isEmpty(provider.getModuleMap())) {
            return;
        }
        outsizeModuleController = Maps.newHashMap();
        provider.getModuleMap().entrySet().forEach(entry -> {
            String module = entry.getKey();
            String controllers = entry.getValue();
            Splitter.on(GraphqlConsts.STR_COMMA)
                    .trimResults()
                    .omitEmptyStrings()
                    .split(controllers)
                    .forEach(controller -> outsizeModuleController.put(controller, module));
        });
    }

    /**
     * 获取API绑定的模块名称，如果是当前模块，则返回null
     *
     * @param apiCode 接口代号
     * @return 如果是当前模块，则返回null
     */
    public String getApiBindGraphqlModuleName(String apiCode) {
        if (outsizeModuleController != null) {
            int index = apiCode.lastIndexOf(GraphqlConsts.STR_DOT);
            String controllerName = apiCode.substring(0, index);
            String apiModuleName = outsizeModuleController.get(controllerName);
            if (apiModuleName != null) {
                return apiModuleName;
            }
        }
        return provider.getModuleName();
    }
}
