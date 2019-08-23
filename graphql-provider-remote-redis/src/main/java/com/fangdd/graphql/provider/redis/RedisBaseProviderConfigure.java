package com.fangdd.graphql.provider.redis;

import com.fangdd.graphql.provider.BaseProviderRegistry;
import com.fangdd.graphql.provider.GraphqlProviderException;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * @author xuwenzhen
 * @date 2019/9/8
 */
@Component
public class RedisBaseProviderConfigure extends BaseProviderRegistry {
    private static final Logger logger = LoggerFactory.getLogger(RedisBaseProviderConfigure.class);
    private static final String STR_CLN = ":";
    private static final String STR_APPS = "apps";
    private static final String APIS = "apis";
    private static final String STR_SUB = "sub";

    /**
     * Graphql Redis注册器路径
     *
     * @demo graphql:apps
     */
    @Value("${graphql.registry.redis:graphql}")
    private String redisRegistryPath;

    /**
     * api接口文档名称
     *
     * @demo api.json
     */
    @Value("${graphql.api.doc:api.json}")
    protected String apiDocName;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;

    @PostConstruct
    public void registryService() {
        validate();

        TpDocGraphqlProviderServiceInfo provider = getTpDocGraphqlProviderServiceInfo();
        if (provider == null) {
            return;
        }

        //尝试读取接口文档
        String apiDoc = readResourceString(apiDocName);
        if (StringUtils.isEmpty(apiDoc)) {
            throw new GraphqlProviderException("无法获取接口文档：" + apiDocName);
        }

        //注册接口文档
        String key = redisRegistryPath + STR_CLN + APIS + STR_CLN + provider.getAppId();
        redisTemplate.execute(new SessionCallback() {
            /**
             * Executes all the given operations inside the same session.
             *
             * @param operations Redis operations
             * @return return value
             */
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                HashOperations hashOperations = operations.opsForHash();
                hashOperations.put(key, provider.getVcsId(), apiDoc);
                hashOperations.put(redisRegistryPath + STR_CLN + STR_APPS, provider.getAppId(), provider.toString());
                return operations.exec();
            }
        });
        redisTemplate.opsForHash().put(key, provider.getVcsId(), apiDoc);
        logger.info("注册服务文档：{}", key);

        //上报
        registerProvider(provider);
    }

    /**
     * 注册服务
     *
     * @param provider 服务信息
     */
    @Override
    protected void registerProvider(TpDocGraphqlProviderServiceInfo provider) {
        redisMessagePublisher.publish(getAppsPath(), provider.toString());
    }

    private String getAppsPath() {
        return redisRegistryPath + STR_CLN + STR_APPS + STR_CLN + STR_SUB;
    }
}
