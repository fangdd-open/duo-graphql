package com.fangdd.graphql.provider.dto;

/**
 * 标准响应体
 */
public class BaseResponse<T> {

    public static final Integer CODE_SUCCESS = 200;
    public static final Integer CODE_ERROR = 500;

    /**
     * 响应值： 200=正常 其它值表示有异常
     *
     * @demo 200
     */
    private Integer code;

    /**
     * 信息，一般在响应异常时有值
     *
     * @demo 用户名不能为空
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public static BaseResponse success() {
        BaseResponse response = new BaseResponse();
        response.setCode(CODE_SUCCESS);
        return response;
    }

    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(CODE_SUCCESS);
        response.setData(data);
        return response;
    }

    public static BaseResponse error() {
        BaseResponse response = new BaseResponse();
        response.setCode(CODE_ERROR);
        return response;
    }

    public static BaseResponse error(String msg) {
        BaseResponse response = new BaseResponse();
        response.setCode(CODE_ERROR);
        response.setMsg(msg);
        return response;
    }

    public static <T> BaseResponse<T> error(String msg, T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(CODE_SUCCESS);
        response.setMsg(msg);
        response.setData(data);
        return response;
    }

    public static <T> BaseResponse<T> error(Integer code, String msg) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
