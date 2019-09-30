package com.fangdd.graphql.core;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import graphql.spring.web.servlet.GraphQLInvocationData;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;

import java.util.Set;

/**
 *
 * @author xuwenzhen
 * @date 2019/9/29
 */
public class DefaultUserExecutionContextFactory implements UserExecutionContextFactory<UserExecutionContext> {
    private Set<String> headerNames;

    private String graphqlQueryHeaderNames;

    public DefaultUserExecutionContextFactory(String graphqlQueryHeaderNames) {
        this.graphqlQueryHeaderNames = graphqlQueryHeaderNames;
        headerNames = Sets.newHashSet();
        if (!StringUtils.isEmpty(graphqlQueryHeaderNames)) {
            Splitter.on(GraphqlConsts.STR_COMMA)
                    .omitEmptyStrings()
                    .trimResults()
                    .split(graphqlQueryHeaderNames)
                    .forEach(headerNames::add);
        }
    }

    /**
     * 创建一个工厂
     *
     * @param invocationData GraphQL调用数据
     * @param request        http请求
     * @return
     */
    @Override
    public UserExecutionContext get(GraphQLInvocationData invocationData, WebRequest request) {
        UserExecutionContext userExecutionContext = new UserExecutionContext();
        userExecutionContext.setHeaderNames(headerNames);
        return userExecutionContext;
    }

    public String getGraphqlQueryHeaderNames() {
        return graphqlQueryHeaderNames;
    }
}
