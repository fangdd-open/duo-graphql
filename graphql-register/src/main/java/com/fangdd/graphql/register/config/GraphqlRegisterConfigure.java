package com.fangdd.graphql.register.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *
 * @author xuwenzhen
 * @date 2019/4/2
 */
@Component
@ComponentScan(basePackages = "com.fangdd.graphql")
@ConfigurationProperties(prefix = "fdd.graphql.register")
public class GraphqlRegisterConfigure {
    /**
     * 注册中心地址，目前支持zookeeper
     */
    private String address;

    /**
     * zk根目录名称，默认为：graphql
     */
    private String root;

    /**
     * 以客户端模式运行时，当前服务ID
     * 需要用此进行mesh调用，所以需要保持一致！
     */
    private String serviceId;

    private Map<String, String> providerService;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Map<String, String> getProviderService() {
        return providerService;
    }

    public void setProviderService(Map<String, String> providerService) {
        this.providerService = providerService;
    }
}
