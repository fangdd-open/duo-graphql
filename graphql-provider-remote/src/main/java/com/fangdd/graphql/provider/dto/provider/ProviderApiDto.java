package com.fangdd.graphql.provider.dto.provider;


import java.util.List;

/**
 * 为Graphql准备的Provider Api数据
 *
 * @author xuwenzhen
 * @date 2019/4/22
 */
public class ProviderApiDto {
    /**
     * 接口信息
     */
    private List<Api> apis;

    /**
     * 实体
     */
    private List<Entity> entities;

    public List<Api> getApis() {
        return apis;
    }

    public void setApis(List<Api> apis) {
        this.apis = apis;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }
}
