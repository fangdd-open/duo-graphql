package com.fangdd.graphql.core.exception;

/**
 * OkHttp调用错误异常
 * @author xuwenzhen
 * @date 2019/7/17
 */
public class OkHttpInvocationException extends RuntimeException {
    public OkHttpInvocationException(String msg, Exception e) {
        super(msg, e);
    }

    public OkHttpInvocationException(String msg) {
        super(msg);
    }
}
