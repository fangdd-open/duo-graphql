package com.fangdd.graphql.service;

/**
 * 消息处理器
 *
 * @author xuwenzhen
 * @date 2019/8/22
 */
public interface MessageService {
    /**
     * 获取当前消息处理的topic或pattern
     *
     * @return topic / pattern
     */
    String getTopic();

    /**
     * 处理消息
     *
     * @param channel 频道名称
     * @param data    接收到的数据
     */
    void process(String channel, String data);
}
