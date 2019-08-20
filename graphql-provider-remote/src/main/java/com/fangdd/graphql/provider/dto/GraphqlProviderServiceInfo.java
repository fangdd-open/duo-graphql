package com.fangdd.graphql.provider.dto;

import java.util.List;

/**
 * GraphQL供应端基本信息
 *
 * @author xuwenzhen
 * @date 2019/4/9
 */
public class GraphqlProviderServiceInfo {
    /**
     * 供应商名称，必须与Mesh风格中注册的名称一致！
     * 注册中心会以此名称的第一段作为业务名称
     *
     * @demo house.graphql.cp.fdd
     */
    private String appId;

    /**
     * 版本号，用于判断服务是否变更
     */
    private String version;

    /**
     * 供应商提供的方法列表
     */
    private List<GraphqlProviderApiDto> apis;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<GraphqlProviderApiDto> getApis() {
        return apis;
    }

    public void setApis(List<GraphqlProviderApiDto> apis) {
        this.apis = apis;
    }
}
