package com.fangdd.graphql.core;

import com.fangdd.graphql.core.exception.GraphqlBuildException;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.register.server.GraphqlEngineService;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * GraphQL Provider变更订阅者
 *
 * @author xuwenzhen
 * @date 2019/8/10
 */
@Component
public class GraphqlProviderObserver implements Observer<List<TpDocGraphqlProviderServiceInfo>> {

    private static final Logger logger = LoggerFactory.getLogger(GraphqlProviderObserver.class);

    @Autowired
    private GraphqlEngineService<TpDocGraphqlProviderServiceInfo> graphqlEngineService;

    /**
     * Provides the Observer with the means of cancelling (disposing) the
     * connection (channel) with the Observable in both
     * synchronous (from within {@link #onNext(Object)}) and asynchronous manner.
     *
     * @param d the Disposable instance whose {@link Disposable#dispose()} can
     *          be called anytime to cancel the connection
     * @since 2.0
     */
    @Override
    public void onSubscribe(@NonNull Disposable d) {
        logger.info("GraphqlProviderObserver.onSubscribe()");
    }

    /**
     * Provides the Observer with a new item to observe.
     * <p>
     * The {@link Observable} may call this method 0 or more times.
     * <p>
     * The {@code Observable} will not call this method again after it calls either {@link #onComplete} or
     * {@link #onError}.
     *
     * @param tpDocGraphqlProviderServiceInfos the item emitted by the Observable
     */
    @Override
    public void onNext(@NonNull List<TpDocGraphqlProviderServiceInfo> tpDocGraphqlProviderServiceInfos) {
        logger.info("GraphqlProviderObserver.onNext()");
        Map<String, List<TpDocGraphqlProviderServiceInfo>> schemaProvidersMap = Maps.newHashMap();
        tpDocGraphqlProviderServiceInfos
                .stream()
                .filter(provider -> !StringUtils.isEmpty(provider.getAppId()))
                .forEach(provider -> {
                    String schemaNames = provider.getSchemaName();
                    if (StringUtils.isEmpty(schemaNames) || GraphqlConsts.STR_NULL.equals(schemaNames)) {
                        //如果没有设置schemaName，使用默认值
                        schemaNames = GraphqlConsts.STR_DEFAULT;
                        provider.setSchemaName(GraphqlConsts.STR_DEFAULT);
                    }
                    Splitter
                            .on(GraphqlConsts.STR_COMMA)
                            .trimResults()
                            .omitEmptyStrings()
                            .split(schemaNames)
                            .forEach(schemaName -> schemaProvidersMap.computeIfAbsent(schemaName, k -> Lists.newArrayList()).add(provider));

                });
        schemaProvidersMap.entrySet().forEach(entity -> graphqlEngineService.registry(entity.getKey(), entity.getValue()));
    }

    /**
     * Notifies the Observer that the {@link Observable} has experienced an error condition.
     * <p>
     * If the {@link Observable} calls this method, it will not thereafter call {@link #onNext} or
     * {@link #onComplete}.
     *
     * @param e the exception encountered by the Observable
     */
    @Override
    public void onError(@NonNull Throwable e) {
        throw new GraphqlBuildException(e);
    }

    /**
     * Notifies the Observer that the {@link Observable} has finished sending push-based notifications.
     * <p>
     * The {@link Observable} will not call this method if it calls {@link #onError}.
     */
    @Override
    public void onComplete() {
        logger.info("GraphqlProviderObserver.onComplete()");
    }
}
