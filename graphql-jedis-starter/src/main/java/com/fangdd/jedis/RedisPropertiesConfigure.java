package com.fangdd.jedis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author xuwenzhen
 * @date 2019/8/21
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisPropertiesConfigure {
    /**
     * redis地址
     *
     * @demo sentinel01.redis.ip.fdd:26379;sentinel02.redis.ip.fdd:26379;sentinel03.redis.ip.fdd:26379
     */
    private String host;

    /**
     * 主库名称
     *
     * @demo redis.ip.fdd
     */
    private String masterName;

    /**
     * 密码
     */
    private String password;

    /**
     * 数据库索引
     */
    private Integer database;

    /**
     * redis类型：standalone=单例模式, sentinel=哨兵模式, cluster=集群模式
     */
    private String type;

    /**
     * redis client name
     */
    private String redisClientName;

    /**
     * 连接超时，单位：毫秒
     */
    private Long connectTimeout;

    /**
     * 读取超时，单位：毫秒
     */
    private Long readTimeout;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRedisClientName() {
        return redisClientName;
    }

    public void setRedisClientName(String redisClientName) {
        this.redisClientName = redisClientName;
    }

    public Long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Long readTimeout) {
        this.readTimeout = readTimeout;
    }
}
