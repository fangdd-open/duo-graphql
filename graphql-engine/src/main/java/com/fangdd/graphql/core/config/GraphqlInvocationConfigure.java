package com.fangdd.graphql.core.config;

import com.fangdd.graphql.core.ExecutionMonitor;
import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.UserExecutionContext;
import com.fangdd.graphql.core.UserExecutionContextFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.spring.web.servlet.GraphQLInvocation;
import org.dataloader.DataLoaderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

/**
 * 覆盖原来的，可以动态切换graphQL实例
 *
 * @author xuwenzhen
 * @date 2019/4/9
 */
@Configuration
public class GraphqlInvocationConfigure {
    private static final Logger logger = LoggerFactory.getLogger(GraphqlInvocationConfigure.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private ExecutionMonitor executionMonitor;

    @Autowired
    private GraphqlProviderConfigure graphqlProviderConfigure;

    @Autowired
    private UserExecutionContextFactory userExecutionContextFactory;

    /**
     * 当前引擎保存的图
     */
    private static Map<String, GraphQL> graphQLMap = Maps.newConcurrentMap();

    /**
     * 使用这个图
     *
     * @param schemaName Schema名称
     * @param graphQL    图
     */
    public void setGraphQL(String schemaName, GraphQL graphQL) {
        graphQLMap.put(schemaName, graphQL);
    }

    @Bean
    @Primary
    public GraphQL getGraphQL() {
        GraphQL graphQL = graphQLMap.get(GraphqlConsts.STR_DEFAULT);
        if (graphQL == null) {
            graphQL = helloWorld();
            graphQLMap.put(GraphqlConsts.STR_DEFAULT, graphQL);
        }
        return graphQL;
    }

    @Bean
    @Primary
    public GraphQLInvocation getGraphQLInvocation() {
        return (invocationData, webRequest) -> {
            long t1 = System.currentTimeMillis();
            UserExecutionContext context = userExecutionContextFactory.get(invocationData, webRequest);
            setExecutionContextSchemaName(webRequest, context);
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(invocationData.getQuery())
                    .context(context)
                    .dataLoaderRegistry(new DataLoaderRegistry())
                    .operationName(invocationData.getOperationName())
                    .variables(invocationData.getVariables())
                    .build();
            if (executionMonitor != null) {
                executionMonitor.beforeInvocation(executionInput, webRequest);
            }
            GraphQL graphQL = graphQLMap.get(context.getSchemaName());
            CompletableFuture<ExecutionResult> executionResultCompletableFuture = graphQL.executeAsync(executionInput);
            executionResultCompletableFuture.thenRun(() -> {
                String operationName = GraphqlConsts.QUERY.toLowerCase();
                if (!StringUtils.isEmpty(invocationData.getOperationName())) {
                    operationName = invocationData.getOperationName();
                }
                String requestData = null;
                try {
                    requestData = objectMapper.writeValueAsString(invocationData);
                } catch (JsonProcessingException e) {
                    logger.error("发生错误！", e);
                }
                logger.info("graphql[{}]查询耗时 {} gql:{}", operationName, System.currentTimeMillis() - t1, requestData);
                if (executionMonitor != null) {
                    executionMonitor.afterInvocation(context, invocationData, webRequest);
                }
            });
            return executionResultCompletableFuture;
        };
    }

    private void setExecutionContextSchemaName(WebRequest webRequest, UserExecutionContext context) {
        String schemaName;
        if (ServletWebRequest.class.isInstance(webRequest)) {
            ServletWebRequest servletWebRequest = (ServletWebRequest) webRequest;
            String url = servletWebRequest.getRequest().getRequestURI();
            schemaName = graphqlProviderConfigure.getUrlSchemaName(url);
        } else {
            schemaName = GraphqlConsts.STR_DEFAULT;
        }
        context.setSchemaName(schemaName);
    }

    /**
     * 这个是默认的schema，系统第一次启动时，需要有schema，不能为空！
     *
     * @return 默认 GraphQL
     */
    private GraphQL helloWorld() {
        String schema = "type Query{hello: String}";

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        RuntimeWiring runtimeWiring = newRuntimeWiring()
                .type("Query", builder -> builder.dataFetcher("hello", new StaticDataFetcher("world")))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        GraphQL defaultGraphQL = GraphQL.newGraphQL(graphQLSchema).build();
        graphQLMap.put(GraphqlConsts.STR_DEFAULT, defaultGraphQL);
        return defaultGraphQL;
    }
}
