package com.fangdd.graphql.register;

import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;

import java.util.List;

/**
 * GraphQL Register Redis 实现
 * @author xuwenzhen
 * @date 2019/8/9
 */
public class GraphqlRedisRegister implements GraphqlRegister<TpDocGraphqlProviderServiceInfo> {
    /**
     * 向注册中心注册供应端服务的信息
     *
     * @param providerServiceData 供应端服务的信息数据
     */
    @Override
    public void register(TpDocGraphqlProviderServiceInfo providerServiceData) {

    }

    /**
     * emit exists provider info list
     *
     * @param providerList exists provider info list
     */
    @Override
    public void emitProviderList(List<TpDocGraphqlProviderServiceInfo> providerList) {

    }

}
