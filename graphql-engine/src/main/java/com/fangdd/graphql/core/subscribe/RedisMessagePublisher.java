package com.fangdd.graphql.core.subscribe;

import com.fangdd.graphql.register.MessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

/**
 * @author xuwenzhen
 * @date 2019/8/21
 */
@Service
public class RedisMessagePublisher implements MessagePublisher {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    private MessageListenerAdapter messageListenerAdapter;

    /**
     * 发布消息
     *
     * @param message 消息
     */
    @Override
    public void publish(String topic, String message) {
        subscribe(topic);
        redisTemplate.convertAndSend(topic, message);
    }

    @Override
    public void subscribe(String topic) {
        if (isContains(topic)) {
            return;
        }
        //添加监听
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter, new ChannelTopic(topic));
    }

    private static boolean isContains(String topic) {
        boolean contains = RedisMessagePublisher.topics.contains(topic);
        if (!contains) {
            RedisMessagePublisher.topics.add(topic);
        }
        return contains;
    }
}
