package com.fangdd.graphql.dto;

/**
 * 服务端信息，包括：schema / dataProvider信息等
 * @author xuwenzhen
 * @date 2019/4/2
 */
public class GraphQLServiceDto {
    /**
     * 服务ID
     */
    private String serviceId;

    /**
     * 当前服务schema / query数据
     */
    private String idl;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setIdl(String idl) {
        this.idl = idl;
    }

    public String getIdl() {
        return idl;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
