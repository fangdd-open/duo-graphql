package com.fangdd.graphql.provider;

import java.io.UnsupportedEncodingException;

/**
 * GraphQL Provider Exception
 *
 * @author xuwenzhen
 * @date 2019/6/29
 */
public class GraphqlProviderException extends RuntimeException {
    public GraphqlProviderException(String msg) {
        super(msg);
    }

    public GraphqlProviderException(String msg, Throwable e) {
        super(msg, e);
    }
}
