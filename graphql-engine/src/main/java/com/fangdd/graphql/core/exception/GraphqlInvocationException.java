package com.fangdd.graphql.core.exception;

/**
 * 调用错误异常
 * @author xuwenzhen
 * @date 2019/7/17
 */
public class GraphqlInvocationException extends OkHttpInvocationException {
    public GraphqlInvocationException(String msg, Exception e) {
        super(msg, e);
    }

    public GraphqlInvocationException(String msg) {
        super(msg);
    }
}
