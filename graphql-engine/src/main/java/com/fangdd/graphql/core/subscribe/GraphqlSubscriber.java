package com.fangdd.graphql.core.subscribe;

import com.fangdd.graphql.core.util.GraphqlContextUtils;
import com.fangdd.graphql.register.JsonService;
import com.google.common.collect.Sets;
import graphql.ExecutionResult;
import io.reactivex.ObservableEmitter;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 订阅器，每个会议是一个新的对象
 *
 * @author xuwenzhen
 * @date 2019/8/22
 */
public class GraphqlSubscriber implements Subscriber<ExecutionResult> {
    private static final Logger logger = LoggerFactory.getLogger(GraphqlSubscriber.class);
    private static JsonService jsonService;

    private final AtomicReference<Subscription> subscriptionReference = new AtomicReference<>();

    private Session session;

    private Set<ObservableEmitter<Object>> emitters;

    /**
     * 消息ID
     */
    private String id;

    @Override
    public void onSubscribe(Subscription subscriber) {
        logger.info("[{}]Subscription onSubscribe", session.getId());
        subscriptionReference.set(subscriber);
        request(1);
    }

    @Override
    public void onNext(ExecutionResult er) {
        init();
        Object data = er.getData();
        String dataJson;
        if (data instanceof String) {
            dataJson = (String) data;
        } else {
            //如果数据不是字符型
            dataJson = jsonService.toJsonString(data);
        }

        logger.info("[{}]Sending update data: {}", session.getId(), dataJson);
        sendMessage(id, GraphqlSubscriptionTypeEnum.GQL_CONNECTION_KEEP_ALIVE.getType(), dataJson);
    }

    @Override
    public void onError(Throwable t) {
        init();
        sendMessage(null, GraphqlSubscriptionTypeEnum.GQL_CONNECTION_ERROR.getType(), null);
        logger.error("[{}]Subscription threw an exception", session.getId(), t);
        cancelSubscription();
    }

    @Override
    public void onComplete() {
        init();
        sendMessage(null, GraphqlSubscriptionTypeEnum.GQL_COMPLETE.getType(), null);
        logger.info("[{}]Subscription complete", session.getId());
        cancelSubscription();
    }

    public GraphqlSubscriber(Session session) {
        this.session = session;
    }

    public void cancelSubscription() {
        logger.info("[{}]Subscription cancelSubscription", session.getId());
        Subscription subscription = subscriptionReference.get();
        if (subscription != null) {
            subscription.cancel();
        }
    }

    public Session getSession() {
        return session;
    }

    public void addEmitter(ObservableEmitter<Object> emitter) {
        if (emitters == null) {
            emitters = Sets.newHashSet();
        }
        emitters.add(emitter);
    }

    public Set<ObservableEmitter<Object>> getEmitters() {
        return emitters;
    }

    private void sendMessage(String id, String type, String dataJson) {
        SubscriptionMessage message = new SubscriptionMessage();
        message.setId(id);
        message.setType(type);
        message.setPayload(dataJson);

        session.getAsyncRemote().sendText(jsonService.toJsonString(message));
        request(1);
    }

    private static void init() {
        if (jsonService == null) {
            jsonService = GraphqlContextUtils.getApplicationContext().getBean(JsonService.class);
        }
    }

    private void request(int n) {
        logger.info("[{}]Subscription request", session.getId());
        Subscription subscription = subscriptionReference.get();
        if (subscription != null) {
            subscription.request(n);
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
