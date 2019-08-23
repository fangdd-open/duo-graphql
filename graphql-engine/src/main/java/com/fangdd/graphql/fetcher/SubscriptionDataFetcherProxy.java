package com.fangdd.graphql.fetcher;

import com.fangdd.graphql.core.UserExecutionContext;
import com.fangdd.graphql.core.subscribe.GraphqlSubscriber;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.observables.ConnectableObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author xuwenzhen
 * @date 2019/8/23
 */
public class SubscriptionDataFetcherProxy implements DataFetcher {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionDataFetcherProxy.class);

    /**
     * 存放各个个会话列表的Map
     * key: topicName
     */
    public static final Map<String, List<ObservableEmitter<Object>>> TOPIC_EMITTERS_MAP = Maps.newConcurrentMap();

    /**
     * topic name
     */
    private String topicName;

    /**
     * This is called by the graphql engine to fetch the value.  The {@link DataFetchingEnvironment} is a composite
     * context object that tells you all you need to know about how to fetch a data value in graphql type terms.
     *
     * @param environment this is the data fetching environment which contains all the context you need to fetch a value
     * @return a value of type T. May be wrapped in a {@link DataFetcherResult}
     * @throws Exception to relieve the implementations from having to wrap checked exceptions. Any exception thrown
     *                   from a {@code DataFetcher} will eventually be handled by the registered {@link DataFetcherExceptionHandler}
     *                   and the related field will have a value of {@code null} in the result.
     */
    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        UserExecutionContext context = environment.getContext();
        Observable<Object> stockPriceUpdateObservable = Observable.create(emitter -> {
            logger.info("注册Emitter[{}]", topicName);
            TOPIC_EMITTERS_MAP.computeIfAbsent(topicName, key -> Lists.newArrayList()).add(emitter);
            GraphqlSubscriber subscriber = context.getSubscriber();
            if (subscriber != null) {
                subscriber.addEmitter(emitter);
            }
        });

        ConnectableObservable<Object> connectableObservable = stockPriceUpdateObservable.share().publish();
        connectableObservable.connect();
        return connectableObservable.toFlowable(BackpressureStrategy.BUFFER);
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public static void removeEmitter(ObservableEmitter<Object> emitter) {

    }
}
