package com.fangdd.graphql.provider.tpdoc;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.internal.Util;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * Http客户端配置
 *
 * @author xuwenzhen
 * @date 2019/7/16
 */
@Configuration
@ConditionalOnMissingBean(OkHttpClient.class)
public class OkHttpClientConfigure {
    private OkHttpClient okHttpClient;

    /**
     * 创建OkHttp客户端
     *
     * @return OkHttp客户端实例
     */
    @Bean
    public OkHttpClient client() {
        okHttpClient = new OkHttpClient.Builder()
                .protocols(Util.immutableListOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .callTimeout(5, TimeUnit.SECONDS)
                .build();

        return okHttpClient;
    }

    /**
     * destroy OkHttp instance
     */
    @PreDestroy
    public void destroy() {
        if (this.okHttpClient != null) {
            this.okHttpClient.dispatcher().executorService().shutdown();
            this.okHttpClient.connectionPool().evictAll();
        }
    }
}
