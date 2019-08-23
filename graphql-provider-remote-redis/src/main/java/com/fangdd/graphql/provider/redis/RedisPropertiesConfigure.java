package com.fangdd.graphql.provider.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author xuwenzhen
 * @date 2019/8/21
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@ConditionalOnProperty(name = {"spring.redis.host"})
public class RedisPropertiesConfigure {
    private String host;

    private int port;

    private String password;
    
    private Integer database;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getDatabase() {
        return database;
    }

    public void setDatabase(Integer database) {
        this.database = database;
    }
}
