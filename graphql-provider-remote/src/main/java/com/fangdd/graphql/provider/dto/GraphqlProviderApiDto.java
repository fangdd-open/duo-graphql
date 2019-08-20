package com.fangdd.graphql.provider.dto;

import java.util.List;

/**
 * GraphQL供应端基本信息
 *
 * @author xuwenzhen
 * @date 2019/4/9
 */
public class GraphqlProviderApiDto {
    /**
     * 调用方法
     *
     * @demo GET
     */
    private String method;

    /**
     * 接口
     *
     * @demo /api/house
     */
    private String url;

    /**
     * 注册的名称，可以直接使用实现方法的名称
     *
     * @demo byIds
     */
    private String name;

    /**
     * 接口标题
     *
     * @demo 通过IDs获取楼盘列表
     */
    private String title;

    /**
     * 接口描述
     *
     * @demo 本接口可以查询跨城市楼盘
     */
    private String desc;

    /**
     * 响应体
     */
    private EntityDto response;

    /**
     * 请求参数
     */
    private List<EntityDto> request;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public EntityDto getResponse() {
        return response;
    }

    public void setResponse(EntityDto response) {
        this.response = response;
    }

    public List<EntityDto> getRequest() {
        return request;
    }

    public void setRequest(List<EntityDto> request) {
        this.request = request;
    }
}
