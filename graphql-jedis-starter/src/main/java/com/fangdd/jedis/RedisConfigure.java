package com.fangdd.jedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * @author xuwenzhen
 * @date 2019/8/21
 */
@Configuration
public class RedisConfigure {
    private static final String STR_SENTINEL = "sentinel";
    private static final String STR_STANDALONE = "standalone";
    private static final String STR_CLUSTER = "cluster";
    private static final String STR_CLN = ":";
    private static final String STR_SEMICOLON = ";";
    private static final String DEFALUT_REDIS_CLIENT_NAME = "myRedisClient";
    private static final String SPRING_REDIS_TYPE = "spring.redis.type";

    @Autowired
    private RedisPropertiesConfigure redisPropertiesConfigure;

    private RedisConnectionFactory redisConnectionFactory;

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 哨兵模式
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = SPRING_REDIS_TYPE, havingValue = STR_SENTINEL)
    public RedisConnectionFactory getRedisSentinelConnectionFactory() {
        if (redisConnectionFactory != null) {
            return redisConnectionFactory;
        }
        redisConnectionFactory = getSentinelFactory();
        return redisConnectionFactory;
    }

    /**
     * 单机模式
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = SPRING_REDIS_TYPE, havingValue = STR_STANDALONE)
    public RedisConnectionFactory getRedisStandaloneConnectionFactory() {
        if (redisConnectionFactory != null) {
            return redisConnectionFactory;
        }
        redisConnectionFactory = getStandaloneFactory();
        return redisConnectionFactory;
    }

    /**
     * 集群模式
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = SPRING_REDIS_TYPE, havingValue = STR_CLUSTER)
    public RedisConnectionFactory getRedisClusterConnectionFactory() {
        if (redisConnectionFactory != null) {
            return redisConnectionFactory;
        }
        redisConnectionFactory = getClusterFactory();
        return redisConnectionFactory;
    }

    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> getRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        if (redisTemplate == null) {
            RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
            redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            redisTemplate.setKeySerializer(stringRedisSerializer);
            redisTemplate.setHashKeySerializer(stringRedisSerializer);
            redisTemplate.setHashValueSerializer(stringRedisSerializer);
            redisTemplate.setValueSerializer(stringRedisSerializer);
            redisTemplate.afterPropertiesSet();
        }
        return redisTemplate;
    }

    private JedisClientConfiguration getRedisConnectionFactoryProperties() {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();

        String redisClientName = redisPropertiesConfigure.getRedisClientName();
        if (StringUtils.isEmpty(redisClientName)) {
            redisClientName = DEFALUT_REDIS_CLIENT_NAME;
        }
        builder.clientName(redisClientName);

        Long connectTimeout = redisPropertiesConfigure.getConnectTimeout();
        if (connectTimeout != null) {
            builder.connectTimeout(Duration.ofMillis(connectTimeout));
        }

        Long readTimeout = redisPropertiesConfigure.getReadTimeout();
        if (connectTimeout != null) {
            builder.readTimeout(Duration.ofMillis(readTimeout));
        }

        return builder.build();
    }


    private static RedisServer getRedisServer(String host) {
        RedisServer redisServer = new RedisServer();
        int index = host.indexOf(STR_CLN);
        if (index > -1) {
            redisServer.port = Integer.parseInt(host.substring(index + 1));
            redisServer.host = host.substring(0, index);
        } else {
            redisServer.host = host;
        }
        return redisServer;
    }

    private RedisConnectionFactory getClusterFactory() {
        String hosts = redisPropertiesConfigure.getHost();
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();
        for (String host : hosts.split(STR_SEMICOLON)) {
            RedisServer redisServer = getRedisServer(host);
            clusterConfig.clusterNode(redisServer.host, redisServer.port);
        }

        if (!StringUtils.isEmpty(redisPropertiesConfigure.getPassword())) {
            clusterConfig.setPassword(redisPropertiesConfigure.getPassword());
        }
        JedisClientConfiguration clientConfiguration = getRedisConnectionFactoryProperties();
        return new JedisConnectionFactory(clusterConfig, clientConfiguration);
    }

    private RedisConnectionFactory getSentinelFactory() {
        String hosts = redisPropertiesConfigure.getHost();
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master(redisPropertiesConfigure.getMasterName());
        for (String host : hosts.split(STR_SEMICOLON)) {
            RedisServer redisServer = getRedisServer(host);
            sentinelConfig.sentinel(redisServer.host, redisServer.port);
        }

        if (!StringUtils.isEmpty(redisPropertiesConfigure.getPassword())) {
            sentinelConfig.setPassword(redisPropertiesConfigure.getPassword());
        }
        if (redisPropertiesConfigure.getDatabase() != null) {
            sentinelConfig.setDatabase(redisPropertiesConfigure.getDatabase());
        }
        JedisClientConfiguration clientConfiguration = getRedisConnectionFactoryProperties();
        return new JedisConnectionFactory(sentinelConfig, clientConfiguration);
    }

    private RedisConnectionFactory getStandaloneFactory() {
        RedisServer redisServer = getRedisServer(redisPropertiesConfigure.getHost());
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redisServer.host, redisServer.port);
        String password = redisPropertiesConfigure.getPassword();
        if (!StringUtils.isEmpty(password)) {
            standaloneConfig.setPassword(password);
        }
        Integer database = redisPropertiesConfigure.getDatabase();
        if (database != null) {
            standaloneConfig.setDatabase(database);
        }
        JedisClientConfiguration clientConfiguration = getRedisConnectionFactoryProperties();
        return new JedisConnectionFactory(standaloneConfig, clientConfiguration);
    }

    private static class RedisServer {
        String host;
        int port = 6379;
    }
}
