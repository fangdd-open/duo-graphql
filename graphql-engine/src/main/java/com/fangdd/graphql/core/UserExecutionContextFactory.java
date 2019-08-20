package com.fangdd.graphql.core;

import graphql.spring.web.servlet.GraphQLInvocationData;
import org.springframework.web.context.request.WebRequest;

/**
 * 执行期用户内容工厂
 *
 * @author xuwenzhen
 * @date 2019/7/17
 */
public interface UserExecutionContextFactory<T extends UserExecutionContext> {
    /**
     * 创建一个工厂
     *
     * @param invocationData GraphQL调用数据
     * @param request        http请求
     * @return
     */
    T get(GraphQLInvocationData invocationData, WebRequest request);
}
