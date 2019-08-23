package com.fangdd.graphql.provider.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author xuwenzhen
 * @date 2019/8/21
 */
@Service
public class RedisMessagePublisher {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 发布消息
     *
     * @param message 消息
     */
    public void publish(String topic, String message) {
        redisTemplate.convertAndSend(topic, message);
    }
}
