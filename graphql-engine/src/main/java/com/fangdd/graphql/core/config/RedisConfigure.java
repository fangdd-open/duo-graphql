package com.fangdd.graphql.core.config;

import com.fangdd.graphql.core.subscribe.RedisMessageSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

/**
 * @author xuwenzhen
 * @date 2019/8/21
 */
@Configuration
public class RedisConfigure {
    @Autowired
    private RedisPropertiesConfigure redisPropertiesConfigure;

    private RedisConnectionFactory redisConnectionFactory;

    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    @ConditionalOnBean(value = {RedisPropertiesConfigure.class})
    public RedisConnectionFactory getRedisConnectionFactory() {
        if (redisConnectionFactory == null) {
            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(
                    redisPropertiesConfigure.getHost(),
                    redisPropertiesConfigure.getPort()
            );
            String password = redisPropertiesConfigure.getPassword();
            if (!StringUtils.isEmpty(password)) {
                standaloneConfig.setPassword(password);
            }
            Integer database = redisPropertiesConfigure.getDatabase();
            if (database != null) {
                standaloneConfig.setDatabase(database);
            }
            redisConnectionFactory = new JedisConnectionFactory(standaloneConfig);
        }
        return redisConnectionFactory;
    }


    @Bean
    public RedisTemplate<String, Object> getRedisTemplate() {
        if (redisTemplate == null) {
            RedisSerializer<String> keySerializer = new StringRedisSerializer();
            redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(getRedisConnectionFactory());
            redisTemplate.setKeySerializer(keySerializer);
            redisTemplate.setHashKeySerializer(keySerializer);
            redisTemplate.setHashValueSerializer(keySerializer);
            redisTemplate.setValueSerializer(keySerializer);
            redisTemplate.afterPropertiesSet();
        }
        return redisTemplate;
    }

    @Bean
    public MessageListenerAdapter messageListener() {
        RedisMessageSubscriber redisMessageSubscriber = new RedisMessageSubscriber();
        redisMessageSubscriber.setRedisTemplate(getRedisTemplate());
        return new MessageListenerAdapter(redisMessageSubscriber);
    }

    @Bean
    public RedisMessageListenerContainer redisContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(getRedisConnectionFactory());
        return container;
    }
}
