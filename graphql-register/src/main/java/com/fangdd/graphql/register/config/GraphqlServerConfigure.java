package com.fangdd.graphql.register.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @author xuwenzhen
 * @date 2019/4/2
 */
@Component
@ConfigurationProperties(prefix = "fdd.graphql.server")
public class GraphqlServerConfigure {
    /**
     * 全局的provider调用地址
     */
    private String url;

    /**
     * Duo-Doc服务地址
     */
    private String tpdocUrl;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTpdocUrl() {
        return tpdocUrl;
    }

    public void setTpdocUrl(String tpdocUrl) {
        this.tpdocUrl = tpdocUrl;
    }
}
