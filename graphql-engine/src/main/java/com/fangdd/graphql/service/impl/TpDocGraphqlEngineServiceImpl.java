package com.fangdd.graphql.service.impl;

import com.fangdd.graphql.core.ExecutionMonitor;
import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.GraphqlModuleContext;
import com.fangdd.graphql.core.GraphqlProviderObserver;
import com.fangdd.graphql.core.config.GraphqlInvocationConfigure;
import com.fangdd.graphql.core.util.GraphqlContextUtils;
import com.fangdd.graphql.pipeline.Pipeline;
import com.fangdd.graphql.pipeline.PipelineManager;
import com.fangdd.graphql.pipeline.RegistryState;
import com.fangdd.graphql.provider.InnerProvider;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.register.server.GraphqlEngineService;
import com.fangdd.graphql.service.DirectiveService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import graphql.GraphQL;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.schema.*;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于TP-DOC的Graphql Provider注册实现
 *
 * @author xuwenzhen
 * @date 2019/4/9
 */
@Service
public class TpDocGraphqlEngineServiceImpl implements GraphqlEngineService<TpDocGraphqlProviderServiceInfo> {
    private static final Logger logger = LoggerFactory.getLogger(TpDocGraphqlEngineServiceImpl.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GraphqlInvocationConfigure graphqlInvocationConfigure;

    @Autowired(required = false)
    private List<InnerProvider> innerProviders;

    @Autowired
    private PipelineManager pipelineManager;

    @Autowired
    private DirectiveService directiveService;

    @Autowired(required = false)
    private ExecutionMonitor executionMonitor;

    @Value("${graphql.query.max-depth:14}")
    private int graphqlQueryMaxDepth;

    @Autowired
    private List<Pipeline> pipelines;

    @Autowired
    private GraphqlProviderObserver graphqlProviderObserver;

    @Autowired
    private PreparsedDocumentProvider preparsedDocumentProvider;

    private ObservableEmitter<List<TpDocGraphqlProviderServiceInfo>> providerEmitter;

    /**
     * Spring框架加载时完成一些初始化动作
     */
    @PostConstruct
    public void initGraphqlContext() {
        GraphqlContextUtils.setApplicationContext(applicationContext);
        //重新排序，小的在前
        Collections.sort(pipelines, ((Pipeline o1, Pipeline o2) -> o1.order() - o2.order()));

        if (!CollectionUtils.isEmpty(innerProviders)) {
            GraphqlContextUtils.setInnerProviders(innerProviders);
        }
        Observable<List<TpDocGraphqlProviderServiceInfo>> providerObservable = Observable.create((ObservableEmitter<List<TpDocGraphqlProviderServiceInfo>> emitter) -> {
            logger.info("init Observable and create a emitter...");
            providerEmitter = emitter;
        });
        providerObservable.subscribe(graphqlProviderObserver);
    }

    @Override
    public void emitProviderList(List<TpDocGraphqlProviderServiceInfo> providerList) {
        providerEmitter.onNext(providerList);
    }

    @Override
    public void registry(String schemaName, List<TpDocGraphqlProviderServiceInfo> providerServiceDataList) {
        //注册
        RegistryState state = null;
        if (executionMonitor != null) {
            executionMonitor.beforeSchemaBuild(schemaName, providerServiceDataList);
        }
        try {
            state = pipelineManager.registry(schemaName, providerServiceDataList, pipelines);
            if (state == null) {
                return;
            }
        } catch (Exception e) {
            logger.error("生成Schema失败！", e);
        }
        if (executionMonitor != null) {
            executionMonitor.onStateBuild(providerServiceDataList, state);
        }

        //部署
        long t = System.currentTimeMillis();
        try {
            deploy(state);
        } catch (Exception e) {
            logger.error("构建GraphQL Schema失败！", e);
        } finally {
            logger.info("构建Schema耗时{}ms", System.currentTimeMillis() - t);
            GraphqlContextUtils.getGraphqlContext(schemaName).finish();
        }
    }

    /**
     * 发布，会聚合各provider的服务在一起
     *
     * @param registryState 当前更新状态
     */
    private void deploy(RegistryState registryState) {
        if (registryState == null) {
            return;
        }

        //当前schema状态，分别是：query, mutation, subscription
        SchemaBuildState schemaStatus = new SchemaBuildState();
        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject().name(GraphqlConsts.QUERY);
        GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject().name(GraphqlConsts.MUTATION);
        GraphQLObjectType.Builder subscriptionBuilder = GraphQLObjectType.newObject().name(GraphqlConsts.SUBSCRIPTION);

        //build query
        schemaStatus.hasQuery = buildQuery(registryState, queryBuilder);

        //build mutation
        schemaStatus.hasMutation = buildMutation(registryState, mutationBuilder);

        //build subscription
        schemaStatus.hasSubscription = false;

        //build inner provider
        buildInnerProvider(registryState);

        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema()
                .codeRegistry(registryState.getCodeRegistry().build());
        if (schemaStatus.hasQuery) {
            schemaBuilder.query(queryBuilder.build());
        }
        if (schemaStatus.hasMutation) {
            schemaBuilder.mutation(mutationBuilder.build());
        }
        if (schemaStatus.hasSubscription) {
            schemaBuilder.subscription(subscriptionBuilder.build());
        }

        Set<GraphQLDirective> directiveSet = directiveService.getDirectiveSet();
        if (!CollectionUtils.isEmpty(directiveSet)) {
            schemaBuilder.additionalDirectives(directiveSet);
        }
        GraphQLSchema graphQLSchema = schemaBuilder
                .additionalTypes(registryState.getGraphQLTypes())
                .build();

        //缓存Document，减少ql请求解析
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema)
                .instrumentation(getInstrumentations())
                .subscriptionExecutionStrategy(new SubscriptionExecutionStrategy())
                .preparsedDocumentProvider(preparsedDocumentProvider)
                .build();
        graphqlInvocationConfigure.setGraphQL(registryState.getSchemaName(), graphQL);

        if (executionMonitor != null) {
            executionMonitor.onSchemaBuild(registryState, graphQL);
        }
    }

    private void buildInnerProvider(RegistryState registryState) {
        registryState.getModuleContextMap().entrySet()
                .forEach((Map.Entry<String, GraphqlModuleContext> entry) -> {
                    GraphqlModuleContext moduleContext = entry.getValue();
                    if (moduleContext.getInnerProvider() != null && !CollectionUtils.isEmpty(moduleContext.getCodeRegistryMap())) {
                        moduleContext.getCodeRegistryMap().entrySet().forEach(codeRegistryMapEntry -> {
                            String[] fields = codeRegistryMapEntry.getKey();
                            FieldCoordinates coordinates = FieldCoordinates.coordinates(fields[0], fields[1]);
                            registryState.getCodeRegistry().dataFetcher(coordinates, codeRegistryMapEntry.getValue());
                        });
                    }
                });
    }

    private boolean buildSubscription(RegistryState registryState, GraphQLObjectType.Builder subscriptionBuilder) {
        SchemaBuildState state = new SchemaBuildState();
        List<GraphQLFieldDefinition> fieldDefinitions = Lists.newArrayList();
        GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
                .name("testSubscription")
                .type(GraphQLTypeReference.typeRef("xf_House"))
                .description("测试订阅功能")
                .build();
        fieldDefinitions.add(fieldDefinition);
        GraphQLObjectType moduleSubscriptionOutType = GraphQLObjectType
                .newObject()
                .name("S_XF")
                .fields(fieldDefinitions)
                .build();
        GraphQLFieldDefinition.Builder groupSubscriptionBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name("xf")
                .type(moduleSubscriptionOutType);
        subscriptionBuilder.field(groupSubscriptionBuilder);
        state.hasSubscription = true;
        registryState.addGraphQLType("S_XF.xf", moduleSubscriptionOutType);
        DataFetcher subscriptionDataFetcher = new DataFetcher() {
            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {
                return null;
            }
        };
        registryState.getCodeRegistry().dataFetcher(FieldCoordinates.coordinates("S_XF", "testSubscription"), subscriptionDataFetcher);
        return state.hasSubscription;
    }

    private boolean buildMutation(RegistryState registryState, GraphQLObjectType.Builder mutationBuilder) {
        SchemaBuildState state = new SchemaBuildState();
        Map<String, List<GraphQLFieldDefinition>> moduleMutionFieldDefinitionMap = registryState.getModuleMutationFieldDefinitionMap();
        if (!CollectionUtils.isEmpty(moduleMutionFieldDefinitionMap)) {
            moduleMutionFieldDefinitionMap.entrySet().forEach(entry -> {
                String moduleName = entry.getKey();
                List<GraphQLFieldDefinition> fieldDefinitions = entry.getValue();
                if (CollectionUtils.isEmpty(fieldDefinitions)) {
                    return;
                }
                String mutationTypeName = GraphqlConsts.STR_M + moduleName.toUpperCase();
                GraphQLObjectType moduleMutationOutType = GraphQLObjectType
                        .newObject()
                        .name(mutationTypeName)
                        .fields(fieldDefinitions)
                        .build();
                GraphQLFieldDefinition.Builder moduleMutationBuilder = GraphQLFieldDefinition.newFieldDefinition()
                        .name(moduleName)
                        .type(moduleMutationOutType);

                FieldCoordinates coordinates = FieldCoordinates.coordinates(GraphqlConsts.MUTATION, moduleName.toLowerCase());
                DataFetcher dataFetcher = environment -> Maps.newHashMap();
                registryState.getCodeRegistry().dataFetcher(coordinates, dataFetcher);

                mutationBuilder.field(moduleMutationBuilder);
                state.hasMutation = true;
                registryState.addGraphQLType(mutationTypeName + GraphqlConsts.STR_DOT + moduleName, moduleMutationOutType);
            });
        }
        return state.hasMutation;
    }

    private boolean buildQuery(RegistryState registryState, GraphQLObjectType.Builder queryBuilder) {
        SchemaBuildState state = new SchemaBuildState();
        Map<String, List<GraphQLFieldDefinition>> moduleQueryFieldDefinitionMap = registryState.getModuleQueryFieldDefinitionMap();
        if (!CollectionUtils.isEmpty(moduleQueryFieldDefinitionMap)) {
            moduleQueryFieldDefinitionMap.entrySet().forEach(entry -> {
                String moduleName = entry.getKey();
                List<GraphQLFieldDefinition> fieldDefinitions = entry.getValue();
                if (CollectionUtils.isEmpty(fieldDefinitions)) {
                    return;
                }
                GraphQLObjectType moduleQueryOutType = GraphQLObjectType
                        .newObject()
                        .name(moduleName.toUpperCase())
                        .fields(fieldDefinitions)
                        .build();
                GraphQLFieldDefinition.Builder moduleQueryBuilder = GraphQLFieldDefinition.newFieldDefinition()
                        .name(moduleName)
                        .type(moduleQueryOutType);
                FieldCoordinates coordinates = FieldCoordinates.coordinates(GraphqlConsts.QUERY, moduleName.toLowerCase());
                DataFetcher dataFetcher = environment -> Maps.newHashMap();
                registryState.getCodeRegistry().dataFetcher(coordinates, dataFetcher);

                queryBuilder.field(moduleQueryBuilder);
                state.hasQuery = true;
                registryState.addGraphQLType(moduleName.toUpperCase() + GraphqlConsts.STR_DOT + moduleName, moduleQueryOutType);
            });
        }
        return state.hasQuery;
    }

    private Instrumentation getInstrumentations() {
        MaxQueryDepthInstrumentation maxQueryDepthInstrumentation = new MaxQueryDepthInstrumentation(graphqlQueryMaxDepth);
        List<Instrumentation> instrumentations = Lists.newArrayList(maxQueryDepthInstrumentation);
        if (executionMonitor != null) {
            instrumentations.add(executionMonitor);
        }
        return new ChainedInstrumentation(instrumentations);
    }

    /**
     * 构建中的Schema状态
     */
    private class SchemaBuildState {
        boolean hasQuery = false;
        boolean hasMutation = false;
        boolean hasSubscription = false;
    }
}
