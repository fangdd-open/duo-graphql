package com.fangdd.graphql.provider.dto;

import java.util.Set;

/**
 * 提供的基础视图
 *
 * @author xuwenzhen
 * @date 2019/5/12
 */
public class ProvidModelInfo {
    /**
     * 基础视图名称
     *
     * @demo House
     */
    private String modelName;

    /**
     * 基本视图对应的id
     *
     * @demo ["houseId", "lpId", "xfId"]
     */
    private Set<String> refIds;

    /**
     * 通过ID查询接口
     *
     * @demo com.fangdd.graphql.house.controller.HouseApiController.getById
     */
    private String idProvider;

    /**
     * 通过IDs查询接口
     *
     * @demo com.fangdd.graphql.house.controller.HouseApiController.getByIds
     */
    private String idsProvider;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Set<String> getRefIds() {
        return refIds;
    }

    public void setRefIds(Set<String> refIds) {
        this.refIds = refIds;
    }

    public String getIdProvider() {
        return idProvider;
    }

    public void setIdProvider(String idProvider) {
        this.idProvider = idProvider;
    }

    public String getIdsProvider() {
        return idsProvider;
    }

    public void setIdsProvider(String idsProvider) {
        this.idsProvider = idsProvider;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\"modelName\":\"")
                .append(modelName)
                .append("\",\"refIds\":[");
        if (refIds != null) {
            refIds.forEach(id -> sb.append("\"").append(id).append("\"").append(","));
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]")
                .append(",\"idProvider\":")
                .append(idProvider == null ? "null" : "\"" + idProvider + "\"")
                .append(",\"idsProvider\":")
                .append(idsProvider == null ? "null" : "\"" + idsProvider + "\"")
                .append("}");
        return sb.toString();
    }
}
