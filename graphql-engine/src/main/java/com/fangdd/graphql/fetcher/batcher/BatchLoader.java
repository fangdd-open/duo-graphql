package com.fangdd.graphql.fetcher.batcher;

import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.Api;

import java.util.List;
import java.util.Map;

/**
 * 批量加载器
 *
 * @author xuwenzhen
 * @date 2019/7/9
 */
public interface BatchLoader {
    /**
     * 批量加载数据
     */
    void fetchData();

    /**
     * 从批量查询中获取数据
     *
     * @param index 当前索引
     * @return
     */
    Object get(int index);

    /**
     * 获取当前批量请求的路径
     *
     * @return
     */
    String getPath();

    /**
     * 设置参数值
     *
     * @param params 参数值
     */
    void setParams(Map<String, List<Object>> params);

    /**
     * 设置请求头
     * @param headers 请求头
     */
    void setHeaders(Map<String, String> headers);

    /**
     * 设置选中字段
     *
     * @param selections 选中字段
     */
    void setSelections(List<String> selections);

    /**
     * 设置当前服务端信息
     *
     * @param serviceInfo 服务端信息
     */
    void setServiceInfo(TpDocGraphqlProviderServiceInfo serviceInfo);

    /**
     * 批量调用使用的接口
     *
     * @param batchApi 接口接口
     */
    void setApi(Api batchApi);

    /**
     * 设置当前请求字段的路径
     *
     * @param path 路径
     */
    void setPath(String path);
}
