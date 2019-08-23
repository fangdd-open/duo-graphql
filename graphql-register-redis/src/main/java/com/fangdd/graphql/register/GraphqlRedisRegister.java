package com.fangdd.graphql.register;

import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.register.config.GraphqlRegisterConfigure;
import com.fangdd.graphql.register.server.GraphqlEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GraphQL Register Redis 实现
 *
 * @author xuwenzhen
 * @date 2019/8/9
 */
@Service
public class GraphqlRedisRegister implements GraphqlRegister<TpDocGraphqlProviderServiceInfo> {
    private static final String PATH_SPLITTER = ":";
    private static final String APPS = "apps";
    private static final String SUB = "sub";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private GraphqlRegisterConfigure graphQLRegisterConfigure;

    @Autowired
    private MessagePublisher messagePublisher;

    @Autowired
    private JsonService jsonService;

    @Autowired
    private GraphqlEngineService<TpDocGraphqlProviderServiceInfo> graphqlEngineService;

    @PostConstruct
    public void init() {
        //拉取所有服务
        Map<Object, Object> providerMap = redisTemplate.opsForHash().entries(getAppRootPath());
        if (CollectionUtils.isEmpty(providerMap)) {
            return;
        }
        List<TpDocGraphqlProviderServiceInfo> providerList = providerMap.entrySet()
                .stream()
                .map(entry -> {
                    String valueStr = (String) entry.getValue();
                    return jsonService.toObject(valueStr, TpDocGraphqlProviderServiceInfo.class);
                })
                .collect(Collectors.toList());
        emitProviderList(providerList);
    }

    /**
     * 向注册中心注册供应端服务的信息
     *
     * @param provider 供应端服务的信息数据
     */
    @Override
    public void register(TpDocGraphqlProviderServiceInfo provider) {
        String key = getAppRootPath();
        String appId = provider.getAppId();
        //注册服务
        String providerDoc = provider.toString();
        redisTemplate.opsForHash().put(key, appId, providerDoc);
        messagePublisher.publish(key + PATH_SPLITTER + SUB, providerDoc);
    }

    /**
     * emit exists provider info list
     *
     * @param providerList exists provider info list
     */
    @Override
    public void emitProviderList(List<TpDocGraphqlProviderServiceInfo> providerList) {
        graphqlEngineService.emitProviderList(providerList);
    }

    private String getAppRootPath() {
        return graphQLRegisterConfigure.getRoot() + PATH_SPLITTER + APPS;
    }
}
