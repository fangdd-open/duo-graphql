package com.fangdd.graphql.core.exception;

/**
 * GraphQL Engine Exception
 * @author xuwenzhen
 * @date 2019/4/29
 */
public class GraphqlEngineException extends RuntimeException {
    public GraphqlEngineException(String msg, Exception e) {
        super(msg, e);
    }

    public GraphqlEngineException(String msg) {
        super(msg);
    }
}
