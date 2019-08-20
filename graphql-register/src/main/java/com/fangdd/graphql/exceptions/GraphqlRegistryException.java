package com.fangdd.graphql.exceptions;

/**
 * GraphQL Registry Exception
 * @author xuwenzhen
 * @date 2019/4/2
 */
public class GraphqlRegistryException extends RuntimeException {
    public GraphqlRegistryException(String message) {
        super(message);
    }

    public GraphqlRegistryException(String message, Throwable e) {
        super(message, e);
    }
}
