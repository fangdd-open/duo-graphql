package com.fangdd.graphql.core.subscribe;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.UserExecutionContext;
import com.fangdd.graphql.core.config.GraphqlInvocationConfigure;
import com.fangdd.graphql.core.util.GraphqlContextUtils;
import com.fangdd.graphql.fetcher.SubscriptionDataFetcherProxy;
import com.fangdd.graphql.register.JsonService;
import com.google.common.collect.Maps;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.reactivex.ObservableEmitter;
import org.dataloader.DataLoaderRegistry;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * GraphQL Subscription功能入口
 *
 * @author xuwenzhen
 * @date 2019/8/8
 */
@Component
@ServerEndpoint(value = "/graphql", subprotocols = {GraphqlConsts.SUBPROTOCOLS})
public class SubscriptionGraphqlEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionGraphqlEndpoint.class);

    private JsonService jsonService;

    private GraphqlInvocationConfigure graphqlInvocationConfigure;

    /**
     * 所有连接终端
     */
    private static final Map<Session, GraphqlSubscriber> SESSION_SUBSCRIBER_MAP = Maps.newConcurrentMap();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        //每个订阅都使用一个新的GraphqlSubscriber对象
        GraphqlSubscriber graphqlSubscriber = new GraphqlSubscriber(session);
        String sessionId = session.getId();
        SESSION_SUBSCRIBER_MAP.put(session, graphqlSubscriber);
        logger.info("[{}]客户端连接", sessionId);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        GraphqlSubscriber graphqlSubscriber = SESSION_SUBSCRIBER_MAP.get(session);
        if (graphqlSubscriber != null) {
            SESSION_SUBSCRIBER_MAP.remove(session);
            graphqlSubscriber.cancelSubscription();

            Set<ObservableEmitter<Object>> emitters = graphqlSubscriber.getEmitters();
            if (!CollectionUtils.isEmpty(emitters)) {
                emitters.forEach(SubscriptionDataFetcherProxy::removeEmitter);
            }
        }

        try {
            session.close();
            logger.info("[{}]关闭会话", session.getId());
        } catch (IOException e) {
            logger.warn("[{}]关闭会话失败", session.getId(), e);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param graphqlQuery 客户端发送过来的消息
     * @param session      会话
     */
    @OnMessage
    public void onMessage(String graphqlQuery, Session session) {
        String sessionId = session.getId();
        logger.info("[{}]会话信息:{}", sessionId, graphqlQuery);

        GraphqlSubscriber graphqlSubscriber = SESSION_SUBSCRIBER_MAP.get(session);
        if (graphqlSubscriber == null) {
            logger.warn("[{}]graphql subscriber is null!", sessionId);
            return;
        }
        UserExecutionContext context = new UserExecutionContext();
        context.setSubscriber(graphqlSubscriber);

        if (jsonService == null) {
            init();
        }
        SubscriptionMessage subscriptionMessage = jsonService.toObject(graphqlQuery, SubscriptionMessage.class);
        String type = subscriptionMessage.getType();

        if (!GraphqlConsts.STR_START.equals(type)) {
            return;
        }

        graphqlSubscriber.setId(subscriptionMessage.getId());

        Object payloadData= subscriptionMessage.getPayload();
        if (payloadData == null) {
            return;
        }
        Map<String, Object> payload = (Map<String, Object>) payloadData;
        String query = (String) payload.get(GraphqlConsts.STR_QUERY_LOWER);
        if (StringUtils.isEmpty(query)) {
            return;
        }

        context.setSchemaName(GraphqlConsts.STR_DEFAULT);
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .context(context)
                .query(query)
                .variables((Map<String, Object>) payload.get(GraphqlConsts.STR_VARIABLES))
                .operationName((String) payload.get(GraphqlConsts.STR_OPERATION_NAME))
                .dataLoaderRegistry(new DataLoaderRegistry())
                .build();

        // In order to have subscriptions in graphql-java you MUST use the
        // SubscriptionExecutionStrategy strategy.
        GraphQL graphQL = graphqlInvocationConfigure.getGraphQL();
        Object dataFatch = graphQL.execute(executionInput).getData();
        if (!(dataFatch instanceof Publisher)) {
            return;
        }

        ((Publisher<ExecutionResult>) dataFatch).subscribe(graphqlSubscriber);
    }

    private void init() {
        ApplicationContext applicationContext = GraphqlContextUtils.getApplicationContext();
        jsonService = applicationContext.getBean(JsonService.class);
        graphqlInvocationConfigure = applicationContext.getBean(GraphqlInvocationConfigure.class);
    }

    /**
     * @param session 会话
     * @param e       错误信息
     */
    @OnError
    public void onError(Session session, Throwable e) {
        logger.error("[{}]连接发生错误", session.getId(), e);
    }

    /**
     * 实现服务器主动推送
     */
    public static void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     */
    private static void sendAllInfo(Session loginUserSession, String message) {
        SESSION_SUBSCRIBER_MAP.keySet().forEach(session -> {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if (loginUserSession == session) {
                    sendMessage(session, "[我]" + message);
                } else {
                    sendMessage(session, "[" + loginUserSession.getId() + "]" + message);
                }
            } catch (IOException e) {
                //发生错误，不理会，丢弃
                logger.warn("发送消息失败", e);
            }
        });
    }
}
