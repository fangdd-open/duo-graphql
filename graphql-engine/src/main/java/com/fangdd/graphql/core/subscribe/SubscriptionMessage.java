package com.fangdd.graphql.core.subscribe;

/**
 * Subscription Query Data
 * 根据Apollo Graphql Subscription规范定义的消息体
 * 官方文档：https://github.com/apollographql/subscriptions-transport-ws/blob/master/PROTOCOL.md
 *
 * @author xuwenzhen
 * @date 2019/8/22
 */
public class SubscriptionMessage {
    /**
     * query id
     */
    private String id;

    /**
     * type:
     * connection_init
     * start
     * stop
     * connection_ack
     * connection_error
     * ka: keep alive
     * connection_terminate
     * data
     * error
     * complete
     */
    private String type;

    /**
     * query data
     */
    private Object payload;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
