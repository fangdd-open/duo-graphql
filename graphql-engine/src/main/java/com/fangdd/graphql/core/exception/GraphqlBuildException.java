package com.fangdd.graphql.core.exception;

/**
 *
 * @author xuwenzhen
 * @date 2019/4/29
 */
public class GraphqlBuildException extends RuntimeException {
    public GraphqlBuildException(String msg, Throwable e) {
        super(msg, e);
    }

    public GraphqlBuildException(String msg) {
        super(msg);
    }

    public GraphqlBuildException(Throwable e) {
        super(e);
    }
}
