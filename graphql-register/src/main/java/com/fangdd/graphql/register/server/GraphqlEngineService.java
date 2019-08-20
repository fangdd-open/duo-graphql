package com.fangdd.graphql.register.server;

import java.util.List;

/**
 * graphql provider registry service
 *
 * @param <T> grpahql provider info
 * @author xuwenzhen
 * @date 2019/4/9
 */
public interface GraphqlEngineService<T> {
    /**
     * registry graphql provider to registry center, eg. zookeeper or redis
     *
     * @param key
     * @param providerServiceDataList graphql provider info list
     */
    void registry(String key, List<T> providerServiceDataList);

    /**
     * emit the changed graphQL provider info list. so that, every graphql engine can get this change
     *
     * @param providerList changed graphQL provider info list
     */
    void emitProviderList(List<T> providerList);
}