package com.fangdd.graphql.service.impl;

import com.fangdd.graphql.core.exception.OkHttpInvocationException;
import com.fangdd.graphql.core.util.OkHttpUtils;
import com.fangdd.graphql.pipeline.impl.ApiDataLoadPipeline;
import com.fangdd.graphql.provider.dto.provider.ProviderApiDto;
import com.fangdd.graphql.register.config.GraphqlServerConfigure;
import com.fangdd.graphql.register.utils.GzipUtils;
import com.fangdd.graphql.service.TpdocService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author xuwenzhen
 * @date 2019/8/6
 */
@Service
public class TpdocServiceImpl implements TpdocService {
    private static final Logger logger = LoggerFactory.getLogger(TpdocServiceImpl.class);
    private static final String VCS_ID = "vcsId";
    private static final String DOMAIN = "domain";
    private static final String API_CODES = "apiCodes";
    private static final int MIN_PROVIDER_API_INFO_LEN = 10;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private GraphqlServerConfigure graphqlServerConfigure;

    /**
     * 拉取TP-DOC上的配置
     *
     * @param appId    appId
     * @param vcsId    版本号，如果为空时，取最新的
     * @param apiCodes 需要拉取的接口code，多个使用半角逗号分隔
     * @return 返回接口文档信息
     */
    @Override
    public ProviderApiDto fetchDocData(String appId, String vcsId, String apiCodes) {
        String tpdocAddress = graphqlServerConfigure.getTpdocUrl();
        String url = tpdocAddress + "/api/doc/app/" + appId + "/";
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new OkHttpInvocationException("调用发生错误，url异常：" + url);
        }

        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        //添加参数
        urlBuilder.addEncodedQueryParameter(VCS_ID, vcsId);
        if(!StringUtils.isEmpty(apiCodes)) {
            urlBuilder.addEncodedQueryParameter(API_CODES, apiCodes);
        }

        Request.Builder requestBuilder = OkHttpUtils.getRestFulRequestBuilder(urlBuilder);

        String domain = tpdocAddress.substring(tpdocAddress.indexOf("//") + 2);
        requestBuilder.addHeader(DOMAIN, domain);

        Request request = requestBuilder.build();
        logger.info("获取Provider API信息请求：{}", request);
        try (Response response = okHttpClient.newCall(request).execute()) {
            byte[] bytes;
            try (ResponseBody body = response.body()) {
                if (body == null) {
                    logger.error("获取服务{}文档数据为空，query: {}", appId, request);
                    return null;
                }
                bytes = body.bytes();
            }
            if (bytes.length < MIN_PROVIDER_API_INFO_LEN) {
                logger.error("获取服务{}文档数据异常，query: {}", appId, request);
                return null;
            }

            String decompress = GzipUtils.decompress(bytes);
            return objectMapper.readValue(decompress, ProviderApiDto.class);
        } catch (IOException e) {
            logger.error("获取服务{}文档数据失败，query: {}", appId, request.toString(), e);
            return null;
        }
    }
}
