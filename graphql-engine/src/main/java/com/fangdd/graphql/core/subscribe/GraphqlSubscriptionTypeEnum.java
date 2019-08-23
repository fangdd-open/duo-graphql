package com.fangdd.graphql.core.subscribe;

/**
 * graphql subscription message type
 *
 * @author xuwenzhen
 * @link https://github.com/apollographql/subscriptions-transport-ws/blob/master/PROTOCOL.md
 * @date 2019/8/25
 */
public enum GraphqlSubscriptionTypeEnum {
    /**
     * Client sends this message after plain websocket connection to start the communication with the server
     */
    GQL_CONNECTION_INIT("connection_init"),

    /**
     * Client sends this message to execute GraphQL operation
     */
    GQL_START("start"),

    /**
     * Client sends this message in order to stop a running GraphQL operation execution (for example: unsubscribe)
     */
    GQL_STOP("stop"),

    /**
     * Client sends this message to terminate the connection.
     */
    GQL_CONNECTION_TERMINATE("connection_terminate"),

    // next type enums is Server -> Client
    /**
     * The server may responses with this message to the GQL_CONNECTION_INIT from client, indicates the server rejected the connection.
     */
    GQL_CONNECTION_ERROR("connection_error"),

    /**
     * The server may responses with this message to the GQL_CONNECTION_INIT from client, indicates the server accepted the connection.
     */
    GQL_CONNECTION_ACK("connection_ack"),

    /**
     * The server sends this message to transfter the GraphQL execution result from the server to the client, this message is a response for GQL_START message.
     */
    GQL_DATA("data"),

    /**
     * Server sends this message upon a failing operation, before the GraphQL execution, usually due to GraphQL validation errors (resolver errors are part of GQL_DATA message, and will be added as errors array)
     */
    GQL_ERROR("error"),

    /**
     * Server sends this message to indicate that a GraphQL operation is done, and no more data will arrive for the specific operation.
     */
    GQL_COMPLETE("complete"),

    /**
     * Server message that should be sent right after each GQL_CONNECTION_ACK processed and then periodically to keep the client connection alive.
     */
    GQL_CONNECTION_KEEP_ALIVE("ka");

    private String type;

    GraphqlSubscriptionTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
