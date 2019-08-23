package com.fangdd.graphql.service.impl;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.exception.OkHttpInvocationException;
import com.fangdd.graphql.core.util.OkHttpUtils;
import com.fangdd.graphql.provider.dto.provider.ProviderApiDto;
import com.fangdd.graphql.register.JsonService;
import com.fangdd.graphql.register.config.GraphqlRegisterConfigure;
import com.fangdd.graphql.register.config.GraphqlServerConfigure;
import com.fangdd.graphql.register.utils.GzipUtils;
import com.fangdd.graphql.service.TpdocService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private JsonService jsonService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private GraphqlServerConfigure graphqlServerConfigure;

    @Autowired
    private GraphqlRegisterConfigure graphQLRegisterConfigure;

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
        if (StringUtils.isEmpty(vcsId)) {
            vcsId = GraphqlConsts.STR_DEFAULT_VCS_ID;
        }
        //尝试从redis中取
        String key = getProviderPath(appId);

        HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
        String providerDocStr = (String) opsForHash.get(key, vcsId);
        if (StringUtils.isEmpty(providerDocStr)) {
            providerDocStr = getProviderDoc(appId, vcsId, apiCodes);
            if (!StringUtils.isEmpty(providerDocStr)) {
                logger.info("put redis {}:{}", key, vcsId);
                opsForHash.put(key, vcsId, providerDocStr);
            }
        } else {
            logger.info("doc from redis. {}:{}", key, vcsId);
        }

        return jsonService.toObject(providerDocStr, ProviderApiDto.class);
    }

    private String getProviderDoc(String appId, String vcsId, String apiCodes) {
        String tpdocAddress = graphqlServerConfigure.getTpdocUrl();
        String url = tpdocAddress + "/api/doc/app/" + appId + GraphqlConsts.PATH_SPLITTER;
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new OkHttpInvocationException("调用发生错误，url异常：" + url);
        }

        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        //添加参数
        urlBuilder.addEncodedQueryParameter(GraphqlConsts.VCS_ID, vcsId);
        if (!StringUtils.isEmpty(apiCodes)) {
            urlBuilder.addEncodedQueryParameter(GraphqlConsts.API_CODES, apiCodes);
        }

        Request.Builder requestBuilder = OkHttpUtils.getRestFulRequestBuilder(urlBuilder);

        int index = tpdocAddress.indexOf(GraphqlConsts.STR_DOUBLE_PATH_SPLITTER) + GraphqlConsts.STR_DOUBLE_PATH_SPLITTER.length();
        String domain = tpdocAddress.substring(index);
        requestBuilder.addHeader(GraphqlConsts.DOMAIN, domain);

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
            if (bytes.length < GraphqlConsts.MIN_PROVIDER_API_INFO_LEN) {
                logger.error("获取服务{}文档数据异常，query: {}", appId, request);
                return null;
            }

            return GzipUtils.decompress(bytes);
        } catch (IOException e) {
            logger.error("获取服务{}文档数据失败，query: {}", appId, request.toString(), e);
            return null;
        }
    }

    private String getProviderPath(String appId) {
        return graphQLRegisterConfigure.getRoot()
                + GraphqlConsts.STR_CLN
                + GraphqlConsts.STR_APIS
                + GraphqlConsts.STR_CLN
                + appId;
    }
}
