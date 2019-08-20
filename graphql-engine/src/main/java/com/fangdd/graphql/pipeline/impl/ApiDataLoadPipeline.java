package com.fangdd.graphql.pipeline.impl;

import com.fangdd.graphql.core.GraphqlModuleContext;
import com.fangdd.graphql.core.exception.GraphqlBuildException;
import com.fangdd.graphql.pipeline.Pipeline;
import com.fangdd.graphql.pipeline.RegistryState;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.ProviderApiDto;
import com.fangdd.graphql.service.TpdocService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * API文档加载
 *
 * @author xuwenzhen
 * @date 2019/6/3
 */
@Service
public class ApiDataLoadPipeline implements Pipeline {
    @Autowired
    private TpdocService tpdocService;

    /**
     * 处理
     *
     * @param state 当前注册信息
     */
    @Override
    public void doPipeline(RegistryState state) {
        List<TpDocGraphqlProviderServiceInfo> providers = state.getProviderServices();
        List<CompletableFuture<ProviderApiDto>> futures = Lists.newArrayList();
        providers.forEach(provider -> {
            //拉接口文档的服务
            CompletableFuture<ProviderApiDto> providerApiFuture = CompletableFuture.supplyAsync(
                    //从TP-DOC服务中拉取文档数据
                    () -> tpdocService.fetchDocData(provider.getAppId(), provider.getVcsId(), null)
            );
            futures.add(providerApiFuture);
        });

        if (CollectionUtils.isEmpty(futures)) {
            return;
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<ProviderApiDto> future = futures.get(i);
            ProviderApiDto providerApi;
            try {
                providerApi = future.get();
            } catch (Exception e) {
                throw new GraphqlBuildException("get provider api info from tp-doc error", e);
            }
            if (providerApi == null) {
                continue;
            }
            TpDocGraphqlProviderServiceInfo provider = providers.get(i);
            GraphqlModuleContext moduleContext = new GraphqlModuleContext(providerApi, provider);
            state.putRemoteProvider(moduleContext);
        }
    }

    /**
     * 执行的顺序
     *
     * @return 数值，越小越前
     */
    @Override
    public int order() {
        return 100;
    }

}
