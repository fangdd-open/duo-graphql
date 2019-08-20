package com.fangdd.graphql.dto;

/**
 * 基于TP-DOC的远端接口信息
 *
 * @author xuwenzhen
 * @date 2019/8/6
 */
public class TpdocBaseProviderApiDto {
    /**
     * api.code
     */
    private String code;

    /**
     * 注册到graphql后的名称，为空时表示使用方法名称
     */
    private String queryName;

    /**
     * 接口响应字段返回的数据（jsonPath格式）
     *
     * @demo $.data
     */
    private String dataPath;

    /**
     * 注册进来的方法，不区分大小写，可选：query | mutation
     * @demo query
     */
    private String actionName;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
}
