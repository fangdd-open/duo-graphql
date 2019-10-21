package com.fangdd.graphql.core;

import com.fangdd.graphql.pipeline.RegistryState;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.spring.web.servlet.GraphQLInvocationData;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

/**
 * 执行监听器（可用于各个环节的行为监听）
 *
 * @author xuwenzhen
 * @date 2019/8/16
 */
public class ExecutionMonitor extends SimpleInstrumentation {
    /**
     * 创建Execution缓存时调用
     *
     * @param cacheKey       缓存键值
     * @param executionInput 当前的请求
     */
    public void createExecutionCache(String cacheKey, ExecutionInput executionInput) {
        // 供实现类重写
    }

    /**
     * 构建Schema前被调用
     *
     * @param schemaName   需要构建的模块名称
     * @param providerList 需要重新构建的模块
     */
    public void beforeSchemaBuild(String schemaName, List<TpDocGraphqlProviderServiceInfo> providerList) {
        // 供实现类重写
    }

    /**
     * 构建Schema，完成RegistryState构建完成时调用
     *
     * @param providerList  当前构建的Provider
     * @param registryState 构建好的RegistryState
     */
    public void onStateBuild(List<TpDocGraphqlProviderServiceInfo> providerList, RegistryState registryState) {
        // 供实现类重写
    }

    /**
     * 构建完成Schema时调用
     *
     * @param registryState 构建好的RegistryState
     * @param graphQL       当前构建好的graphQL实例
     */
    public void onSchemaBuild(RegistryState registryState, GraphQL graphQL) {
        // 供实现类重写
    }

    /**
     * 执行请求之前被调用
     *
     * @param executionInput 当前执行输入
     * @param webRequest     网页请求数据
     */
    public void beforeInvocation(ExecutionInput executionInput, WebRequest webRequest) {
        // 供实现类重写
    }

    /**
     * 执行请求之后被调用
     *
     * @param userContext    当前执行的上下文
     * @param invocationData 执行数据
     * @param webRequest     网页请求数据
     */
    public void afterInvocation(UserExecutionContext userContext, GraphQLInvocationData invocationData, WebRequest webRequest) {
        // 供实现类重写
    }
}
