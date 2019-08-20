package com.fangdd.graphql.pipeline;

import com.fangdd.graphql.core.GraphqlContext;
import com.fangdd.graphql.core.util.GraphqlContextUtils;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author xuwenzhen
 * @date 2019/6/3
 */
@Service
public class PipelineManager {
    private static final Logger logger = LoggerFactory.getLogger(PipelineManager.class);

    /**
     * 注册服务
     *
     * @param schemaName   需要构建的Schema名称
     * @param providerList 需要注册的服务
     * @param pipelines    处理pipelines
     * @return 返回正在构建的状态，如果返回null表示停止当前构建
     */
    public RegistryState registry(String schemaName, List<TpDocGraphqlProviderServiceInfo> providerList, List<Pipeline> pipelines) {
        GraphqlContext graphqlContext = GraphqlContextUtils.getGraphqlContext(schemaName);
        if (graphqlContext != null && graphqlContext.isBusy()) {
            //如果是正在构建，则需要停下，将当前的服务一起合并了
            boolean hasChange;
            synchronized (PipelineManager.class) {
                RegistryState runningRegistryState = graphqlContext.getRegistryState();
                hasChange = merge(runningRegistryState, providerList);
                if (hasChange) {
                    //正在运行的项目，设置中断信号
                    runningRegistryState.stop();
                    //重新开始
                    graphqlContext.restart();
                }
            }
            if (hasChange) {
                return registry(schemaName, providerList, pipelines);
            }
        }

        RegistryState registryState = new RegistryState(schemaName, providerList);
        for (Pipeline pipeline : pipelines) {
            if (registryState.isStop()) {
                return null;
            }
            long t = System.currentTimeMillis();
            pipeline.doPipeline(registryState);
            logger.info("{}耗时{}ms", pipeline.getClass().getSimpleName(), System.currentTimeMillis() - t);
        }
        return registryState;
    }

    /**
     * 检查
     *
     * @param registryState 正在构建的状态
     * @param providerList  需要注册的服务
     * @return
     */
    private boolean merge(RegistryState registryState, List<TpDocGraphqlProviderServiceInfo> providerList) {
        List<TpDocGraphqlProviderServiceInfo> providerServices = registryState.getProviderServices();
        if (CollectionUtils.isEmpty(providerServices)) {
            return false;
        }
        boolean hasChange = false;
        for (TpDocGraphqlProviderServiceInfo providerServiceInfo : providerServices) {
            boolean sameProvider = false;
            for (TpDocGraphqlProviderServiceInfo psi : providerList) {
                if (psi.getAppId().equals(providerServiceInfo.getAppId())) {
                    if (!psi.getVcsId().equals(providerServiceInfo.getVcsId())) {
                        // 版本号有变化
                        logger.info("合并新版本Provider: {}", providerServiceInfo.getAppId());
                        hasChange = true;
                    }
                    sameProvider = true;
                    break;
                }
            }
            if (!sameProvider) {
                hasChange = true;
                logger.info("合并新Provider: {}", providerServiceInfo.getAppId());
                providerList.add(providerServiceInfo);
            }
        }

        return hasChange;
    }
}
