package com.fangdd.graphql.provider.tpdoc;

import com.fangdd.graphql.provider.BaseProviderRegistry;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 注册当前服务为Graphql Provider
 *
 * @author xuwenzhen
 * @date 2019/4/16
 */
@Component
public class TpDocBaseProviderConfigure extends BaseProviderRegistry {
    private static final Logger logger = LoggerFactory.getLogger(TpDocBaseProviderConfigure.class);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
    private static final String HOST = "host";
    private static final int HTTP_CODE_SUCCESS = 200;

    /**
     * Graphql Duo-Doc注册器地址
     *
     * @demo http://127.0.0.1:17040/api/register/tpdoc
     */
    @Value("${graphql.registry.tpdoc-address}")
    private String tpdocRegistryAddress;

    /**
     * Graphql服务的Mesh网格appId
     */
    @Value("${graphql.registry.host:}")
    private String tpdocRegistryHost;

    @Autowired
    private OkHttpClient okHttpClient;

    @PostConstruct
    public void registryService() {
        validate();

        TpDocGraphqlProviderServiceInfo provider = getTpDocGraphqlProviderServiceInfo();
        if (provider == null) {
            return;
        }

        //上报
        registerProvider(provider);
    }

    @Override
    protected void registerProvider(TpDocGraphqlProviderServiceInfo provider) {
        HttpUrl httpUrl = HttpUrl.parse(tpdocRegistryAddress);
        if (httpUrl == null) {
            TpDocBaseProviderConfigure.logger.error("调用发生错误，url异常:{}", tpdocRegistryAddress);
            return;
        }

        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());
        requestBuilder.addHeader(TpDocBaseProviderConfigure.CONTENT_TYPE, TpDocBaseProviderConfigure.CONTENT_TYPE_VALUE);
        if (!StringUtils.isEmpty(tpdocRegistryHost)) {
            //设置Mesh网格的请求头
            requestBuilder.addHeader(TpDocBaseProviderConfigure.HOST, tpdocRegistryHost);
        }
        RequestBody requestBody = null;
        try {
            requestBody = MultipartBody.create(provider.toString().getBytes(CHARSET_NAME));
        } catch (UnsupportedEncodingException e) {
            TpDocBaseProviderConfigure.logger.error("不支持字符编码：{}", CHARSET_NAME, e);
            return;
        }
        requestBuilder.post(requestBody);
        Request request = requestBuilder.build();
        long t1 = System.currentTimeMillis();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() != TpDocBaseProviderConfigure.HTTP_CODE_SUCCESS) {
                TpDocBaseProviderConfigure.logger.error("注册服务失败！，status:{}", response.code());
                return;
            }

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    TpDocBaseProviderConfigure.logger.error("注册服务，返回为空！，status:{}", response.code());
                    return;
                }
                TpDocBaseProviderConfigure.logger.info("注册Fdd Graphql Provider:{}, 结果：{}", provider, responseBody.string());
            }
        } catch (IOException e) {
            TpDocBaseProviderConfigure.logger.error("调用失败：{},{}", e.getMessage(), request.toString(), e);
        } finally {
            TpDocBaseProviderConfigure.logger.info("{}, 耗时 {}", urlBuilder, System.currentTimeMillis() - t1);
        }
    }
}
