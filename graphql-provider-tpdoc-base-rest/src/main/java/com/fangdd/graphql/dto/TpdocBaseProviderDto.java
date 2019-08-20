package com.fangdd.graphql.dto;

import java.util.List;

/**
 * 基于TP-DOC的远端接口信息
 *
 * @author xuwenzhen
 * @date 2019/8/6
 */
public class TpdocBaseProviderDto {
    /**
     * appId，对应注册进TP-DOC时使用的appId
     *
     * @demo m.web.cp.fdd
     */
    private String appId;

    /**
     * 当前服务的请求地址
     */
    private String server;

    /**
     * 注册进GraphQL的模块（领域）配置
     */
    private List<TpdocBaseProviderModuleDto> modules;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public List<TpdocBaseProviderModuleDto> getModules() {
        return modules;
    }

    public void setModules(List<TpdocBaseProviderModuleDto> modules) {
        this.modules = modules;
    }
}
