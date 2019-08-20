package com.fangdd.graphql.dto;

import java.util.List;

/**
 * 基于TP-DOC的远端接口信息
 *
 * @author xuwenzhen
 * @date 2019/8/6
 */
public class TpdocBaseProviderModuleDto {
    /**
     * 注册进GraphQL的模块（领域）名称
     */
    private String name;

    /**
     * 需要注册进来的接口信息
     */
    private List<TpdocBaseProviderApiDto> apis;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TpdocBaseProviderApiDto> getApis() {
        return apis;
    }

    public void setApis(List<TpdocBaseProviderApiDto> apis) {
        this.apis = apis;
    }
}
