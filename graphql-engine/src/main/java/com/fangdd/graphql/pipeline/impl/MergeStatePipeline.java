package com.fangdd.graphql.pipeline.impl;

import com.fangdd.graphql.core.GraphqlContext;
import com.fangdd.graphql.core.GraphqlModuleContext;
import com.fangdd.graphql.core.util.GraphqlContextUtils;
import com.fangdd.graphql.pipeline.Pipeline;
import com.fangdd.graphql.pipeline.RegistryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 将一些公共信息合并到RegistryState
 *
 * @author xuwenzhen
 * @date 2019/6/5
 */
@Service
public class MergeStatePipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(MergeStatePipeline.class);
    /**
     * 处理
     *
     * @param registryState 当前注册信息
     */
    @Override
    public void doPipeline(RegistryState registryState) {
        GraphqlContext graphqlContext = GraphqlContextUtils.getGraphqlContext(registryState.getSchemaName());
        Map<String, GraphqlModuleContext> moduleMap = graphqlContext.getModuleContextMap();
        if (CollectionUtils.isEmpty(moduleMap)) {
            return;
        }

        //合并现存的模块(包含Inner Provider)
        moduleMap.entrySet().forEach(entry -> {
            String moduleName = entry.getKey();
            //检查是否存在
            if (registryState.moduleContextExists(moduleName)) {
                return;
            }
            GraphqlModuleContext moduleContext = entry.getValue();
            if (moduleContext.getInnerProvider() == null) {
                //Remote Provider
                registryState.putRemoteProvider(moduleContext);
            } else {
                //Inner Provider
                logger.info("准备注册Inner Provider：{}", moduleContext.getModuleName());
                registryState.putInnerProvider(moduleContext);
            }
        });
    }

    /**
     * 执行的顺序
     *
     * @return 数值，越小越前
     */
    @Override
    public int order() {
        return 200;
    }
}
