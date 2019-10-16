package com.duoec.graphql.core.exceptions;

/**
 *
 * @author xuwenzhen
 * @date 2019/10/14
 */
public class BaseServiceException extends RuntimeException {
    /**
     * 错误代码
     */
    private int code = 500;

    public BaseServiceException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
