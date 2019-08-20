package com.fangdd.graphql.provider;

/**
 *
 * @author xuwenzhen
 * @date 2019/5/22
 */
public class InnerProviderException extends RuntimeException {
    public InnerProviderException(String message, Exception e) {
        super(message, e);
    }
}
