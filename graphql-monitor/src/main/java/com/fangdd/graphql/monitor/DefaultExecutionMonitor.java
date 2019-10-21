package com.fangdd.graphql.monitor;

import com.fangdd.graphql.core.ExecutionMonitor;
import com.fangdd.graphql.core.UserExecutionContext;
import com.fangdd.graphql.pipeline.RegistryState;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.spring.web.servlet.GraphQLInvocationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

/**
 * 默认的监听器
 *
 * @author xuwenzhen
 * @date 2019/8/16
 */
public class DefaultExecutionMonitor extends ExecutionMonitor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultExecutionMonitor.class);

    /**
     * 创建Execution缓存时调用
     *
     * @param cacheKey       缓存键值
     * @param executionInput 当前的请求
     */
    @Override
    public void createExecutionCache(String cacheKey, ExecutionInput executionInput) {
        logger.info("DefaultExecutionMonitor.createExecutionCache()");
    }

    /**
     * 构建Schema前被调用
     *
     * @param schemaName
     * @param providerList 需要重新构建的模块
     */
    @Override
    public void beforeSchemaBuild(String schemaName, List<TpDocGraphqlProviderServiceInfo> providerList) {
        logger.info("DefaultExecutionMonitor.beforeSchemaBuild()");
    }

    /**
     * 构建Schema，完成RegistryState构建完成时调用
     *
     * @param providerList  当前构建的Provider
     * @param registryState 构建好的RegistryState
     */
    @Override
    public void onStateBuild(List<TpDocGraphqlProviderServiceInfo> providerList, RegistryState registryState) {
        logger.info("DefaultExecutionMonitor.onStateBuild()");
    }

    /**
     * 构建完成Schema时调用
     *
     * @param registryState 构建好的RegistryState
     * @param graphQL       当前构建好的graphQL实例
     */
    @Override
    public void onSchemaBuild(RegistryState registryState, GraphQL graphQL) {
        logger.info("DefaultExecutionMonitor.onSchemaBuild()");
    }

    /**
     * 执行请求之前被调用
     *
     * @param executionInput 当前执行输入
     * @param webRequest     网页请求数据
     */
    @Override
    public void beforeInvocation(ExecutionInput executionInput, WebRequest webRequest) {
        logger.info("DefaultExecutionMonitor.beforeInvocation()");
    }

    /**
     * 执行请求之后被调用
     *
     * @param userContext    当前执行的上下文
     * @param invocationData 执行数据
     * @param webRequest     网页请求数据
     */
    @Override
    public void afterInvocation(UserExecutionContext userContext, GraphQLInvocationData invocationData, WebRequest webRequest) {
        logger.info("DefaultExecutionMonitor.afterInvocation()");
    }
}
