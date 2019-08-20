package com.fangdd.graphql.controller;

import com.fangdd.graphql.core.config.GraphqlInvocationConfigure;
import com.fangdd.graphql.service.JsonService;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.spring.web.servlet.ExecutionResultHandler;
import graphql.spring.web.servlet.GraphQLInvocation;
import graphql.spring.web.servlet.components.GraphQLRequestBody;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;

/**
 * GraphQL Subscription功能入口
 *
 * @author xuwenzhen
 * @date 2019/8/8
 */
@Component
@ServerEndpoint("/graphql")
public class SubscriptionGraphqlController {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionGraphqlController.class);

    @Autowired
    private JsonService jsonService;

    @Autowired
    private GraphQLInvocation graphQLInvocation;

    @Autowired
    private ExecutionResultHandler executionResultHandler;

    @Autowired
    private GraphqlInvocationConfigure graphqlInvocationConfigure;

    private Session session;

    private final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();

    /**
     * 所有连接终端
     */
    private static Set<SubscriptionGraphqlController> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        try {
            sendInfo(session.getId() + "登录");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.session = session;
        webSocketSet.add(this);
        logger.info("客户端连接");
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        Subscription subscription = subscriptionRef.get();
        if (subscription != null) {
            subscription.cancel();
        }

        webSocketSet.remove(this);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param graphqlQuery 客户端发送过来的消息
     * @param session      会话
     */
    @OnMessage
    public void onMessage(String graphqlQuery, Session session) {
        logger.info("Websocket said {}", graphqlQuery);
        GraphQLRequestBody parameters = jsonService.toObject(graphqlQuery, GraphQLRequestBody.class);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(parameters.getQuery())
                .variables(parameters.getVariables())
                .operationName(parameters.getOperationName())
                .build();

        Instrumentation instrumentation = new ChainedInstrumentation(
                singletonList(new TracingInstrumentation())
        );

        //
        // In order to have subscriptions in graphql-java you MUST use the
        // SubscriptionExecutionStrategy strategy.
        //
        GraphQL graphQL = graphqlInvocationConfigure.getGraphQL();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        Publisher<ExecutionResult> stockPriceStream = executionResult.getData();

        stockPriceStream.subscribe(new Subscriber<ExecutionResult>() {
            @Override
            public void onSubscribe(Subscription s) {
                subscriptionRef.set(s);
                request(1);
            }

            @Override
            public void onNext(ExecutionResult er) {
                logger.debug("Sending stick price update");
                Object stockPriceUpdate = er.getData();
                session.getAsyncRemote().sendText(jsonService.toJsonString(stockPriceUpdate));
                request(1);
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Subscription threw an exception", t);
                try {
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onComplete() {
                logger.info("Subscription complete");
                try {
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @param session 会话
     * @param error   错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.info("发生错误");
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message) throws IOException {
        for (SubscriptionGraphqlController item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                item.sendMessage(item.session, message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    private void request(int n) {
        Subscription subscription = subscriptionRef.get();
        if (subscription != null) {
            subscription.request(n);
        }
    }
}
