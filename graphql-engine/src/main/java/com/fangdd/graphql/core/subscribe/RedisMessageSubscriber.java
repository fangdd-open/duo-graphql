package com.fangdd.graphql.core.subscribe;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.service.MessageService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * @author xuwenzhen
 * @date 2019/8/21
 */
public class RedisMessageSubscriber implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(RedisMessageSubscriber.class);
    private static final Map<String, MessageService> MESSAGE_SERVICE_MAP = Maps.newConcurrentMap();
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Callback for processing received objects through Redis.
     *
     * @param message message must not be {@literal null}.
     * @param pattern pattern matching the channel (if specified) - can be {@literal null}.
     */
    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        String data = (String) redisTemplate.getValueSerializer().deserialize(message.getBody());
        RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();
        String channel = stringSerializer.deserialize(message.getChannel());
        logger.info("收到消息：[{}]{}", channel, data);
        MessageService messageService = MESSAGE_SERVICE_MAP.get(channel);
        if (messageService == null) {
            int index = channel.lastIndexOf(GraphqlConsts.STR_CLN);
            if (index > -1) {
                String patternTopic = channel.substring(0, index + 1) + GraphqlConsts.STR_STAR;
                messageService = MESSAGE_SERVICE_MAP.get(patternTopic);
            }
        }
        if (messageService == null) {
            logger.warn("未找到消息处理器：{}", channel);
        } else {
            messageService.process(channel, data);
        }
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 注册消息处理器
     *
     * @param messageService 消息处理器
     */
    public static void registryMessageService(MessageService messageService) {
        MESSAGE_SERVICE_MAP.put(messageService.getTopic(), messageService);
    }
}
