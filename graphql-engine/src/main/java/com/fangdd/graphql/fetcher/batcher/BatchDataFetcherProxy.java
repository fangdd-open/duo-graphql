package com.fangdd.graphql.fetcher.batcher;

import com.fangdd.graphql.core.exception.GraphqlInvocationException;
import com.fangdd.graphql.provider.dto.BatchResponse;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.fangdd.graphql.service.JsonService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 批量查询
 *
 * @author xuwenzhen
 * @date 2019/5/27
 */
public class BatchDataFetcherProxy extends BaseBatchLoader {
    private static final String DATA = "data";

    @Autowired
    private JsonService jsonService;

    /**
     * 批量响应的结果
     */
    private List<Object> batchDataList;

    /**
     * 批处理构造方法
     */
    public BatchDataFetcherProxy() {
        super(null, null);
    }

    /**
     * 处理批量处理结果
     *
     * @param jsonStr 返回结果
     */
    @Override
    protected void processResponseData(String jsonStr) {
        if (jsonStr == null) {
            return;
        }
        Object data = jsonService.toObject(jsonStr);
        if (isRefIdsMerge()) {
            if (!List.class.isInstance(data)) {
                throw new GraphqlInvocationException("通过refIds合并的批量请求，响应必须是一个列表！当前响应：" + jsonStr);
            }
            processRefIdsMergeResponseData((List) data);
        } else {
            if (!Map.class.isInstance(data)) {
                throw new GraphqlInvocationException("批量加载数据响应值错误，响应值必须是" + BatchResponse.class.getName() + "类型！当前响应：" + jsonStr);
            }
            processNormalResponseData((Map<String, List<Object>>) data);
        }
    }

    private void processRefIdsMergeResponseData(List data) {
        List<Integer> refIdsCounts = getRefIdsCounts();
        int start = 0;
        batchDataList = Lists.newArrayList();
        for (int i = 0; i < refIdsCounts.size(); i++) {
            int count = refIdsCounts.get(i);
            batchDataList.add(data.subList(start, start + count));
            start += count;
        }
    }

    private void processNormalResponseData(Map<String, List<Object>> batchResponse) {
        batchDataList = batchResponse.get(DATA);
        if (batchDataList != null && batchDataList.size() != getBatchSize()) {
            throw new GraphqlInvocationException("批量加载返回数据异常，批量数据量(" + batchDataList.size() + ")必须与请求量(" + getBatchSize() + ")一致！");
        }
    }

    @Override
    public Object get(int index) {
        await();
        //在上面保证了索引不会超出
        return batchDataList.get(index);
    }

    /**
     * 设置当前服务端信息
     *
     * @param provider 服务端信息
     */
    @Override
    public void setServiceInfo(TpDocGraphqlProviderServiceInfo provider) {
        this.provider = provider;
    }

    /**
     * 批量调用使用的接口
     *
     * @param api 批量接口
     */
    @Override
    public void setApi(Api api) {
        this.api = api;
    }
}
