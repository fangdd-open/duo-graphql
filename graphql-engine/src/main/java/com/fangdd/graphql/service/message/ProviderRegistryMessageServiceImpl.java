package com.fangdd.graphql.service.message;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.register.JsonService;
import com.fangdd.graphql.register.server.GraphqlEngineService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provider 注册消息处理器
 *
 * @author xuwenzhen
 * @date 2019/8/22
 */
@Service
public class ProviderRegistryMessageServiceImpl extends BaseMessageService {
    @Autowired
    private JsonService jsonService;

    @Autowired
    private GraphqlEngineService<TpDocGraphqlProviderServiceInfo> graphqlEngineService;

    /**
     * 获取当前消息处理的topic或pattern
     *
     * @return topic / pattern
     */
    @Override
    public String getTopic() {
        return getRoot() + GraphqlConsts.STR_APPS  + GraphqlConsts.STR_CLN + GraphqlConsts.STR_SUB;
    }

    @Override
    public void process(String channel, String data) {
        TpDocGraphqlProviderServiceInfo provider = jsonService.toObject(data, TpDocGraphqlProviderServiceInfo.class);
        graphqlEngineService.emitProviderList(Lists.newArrayList(provider));
    }
}
