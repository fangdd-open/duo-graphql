package com.fangdd.graphql.core.config;

import com.fangdd.graphql.core.UserExecutionContext;
import com.fangdd.graphql.core.UserExecutionContextFactory;
import graphql.spring.web.servlet.GraphQLInvocationData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

/**
 * @author xuwenzhen
 * @date 2019/7/17
 */
@Configuration
@ConditionalOnMissingBean(UserExecutionContextFactory.class)
public class UserExecutionContextFactoryConfigure {
    @Bean
    public UserExecutionContextFactory<UserExecutionContext> getDefaultUserExecutionContextFactory() {
        return new UserExecutionContextFactory() {
            /**
             * 创建一个工厂
             *
             * @param invocationData GraphQL调用数据
             * @param request        http请求
             * @return
             */
            @Override
            public UserExecutionContext get(GraphQLInvocationData invocationData, WebRequest request) {
                return new UserExecutionContext();
            }
        };
    }
}
