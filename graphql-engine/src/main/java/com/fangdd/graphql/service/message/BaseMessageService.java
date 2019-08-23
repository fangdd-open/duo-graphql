package com.fangdd.graphql.service.message;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.subscribe.RedisMessageSubscriber;
import com.fangdd.graphql.register.MessagePublisher;
import com.fangdd.graphql.register.config.GraphqlRegisterConfigure;
import com.fangdd.graphql.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author xuwenzhen
 * @date 2019/8/22
 */
abstract class BaseMessageService implements MessageService {
    private static final Logger logger = LoggerFactory.getLogger(BaseMessageService.class);

    @Autowired
    protected GraphqlRegisterConfigure graphQLRegisterConfigure;

    @Autowired
    private MessagePublisher messagePublisher;

    @PostConstruct
    public void init() {
        String topic = this.getTopic();
        logger.info("[{}]启动监听：{}", this.getClass().getName(), topic);
        RedisMessageSubscriber.registryMessageService(this);
        messagePublisher.subscribe(topic);
    }

    /**
     * 获取根路径
     *
     * @return 路径名称
     */
    String getRoot() {
        return graphQLRegisterConfigure.getRoot() + GraphqlConsts.STR_CLN;
    }
}
