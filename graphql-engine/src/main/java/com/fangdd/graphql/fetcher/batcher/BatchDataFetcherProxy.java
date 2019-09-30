package com.fangdd.graphql.fetcher.batcher;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.exception.GraphqlInvocationException;
import com.fangdd.graphql.provider.dto.BatchResponse;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.fangdd.graphql.provider.dto.provider.EntityRef;
import com.fangdd.graphql.register.JsonService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            if (Map.class.isInstance(data)) {
                //BatchResponse
                processNormalResponseData((Map<String, List<Object>>) data);
            } else if (Collection.class.isInstance(data) && isIdsProviderResp()) {
                EntityRef idPram = this.api.getRequestParams().get(0);
                String paramName = idPram.getName();
                List<Object> ids = this.getParams().get(paramName);

                //用于存储id与实体的映射关系
                Map<Object, Object> idMap = setIdMap((Collection) data);
                List<Object> orderedList = Lists.newArrayList();
                if (!CollectionUtils.isEmpty(ids)) {
                    ids.forEach(id -> orderedList.add(idMap.get(id)));
                }
                processListData(orderedList);
            } else {
                throw new GraphqlInvocationException("批量加载数据响应值错误，响应值必须是" + BatchResponse.class.getName() + "类型！当前响应：" + jsonStr);
            }
        }
    }

    private boolean isIdsProviderResp() {
        boolean idProvider = api.getIdProvider() != null && api.getIdProvider();
        boolean isBatch = api.getBatchProvider() != null && api.getBatchProvider();
        return idProvider && isBatch;
    }

    private void processRefIdsMergeResponseData(List data) {
        //需要做些特殊处理
        //用于存储id与实体的映射关系
        Map<Object, Object> idMap = setIdMap(data);

        List<List<Object>> refIdsCounts = getRefIdsList();
        batchDataList = Lists.newArrayList();
        refIdsCounts.forEach(ids -> {
            List<Object> entities = Lists.newArrayList();
            if (!CollectionUtils.isEmpty(ids)) {
                ids.forEach(id -> entities.add(idMap.get(id)));
            }
            batchDataList.add(entities);
        });
    }

    private Map<Object, Object> setIdMap(Collection data) {
        if (CollectionUtils.isEmpty(data)) {
            return Maps.newHashMap();
        }

        return (Map<Object, Object>) data.stream()
                .filter(item -> item instanceof Map)
                .collect(Collectors.toMap(
                        (Map map) -> map.get(GraphqlConsts.STR_ID_LOWER),
                        map -> map
                ));
    }

    private void processNormalResponseData(Map<String, List<Object>> batchResponse) {
        processListData(batchResponse.get(DATA));
    }

    private void processListData(List<Object> listData) {
        batchDataList = listData;
        if (batchDataList != null && batchDataList.size() != getBatchSize()) {
            throw new GraphqlInvocationException("批量加载返回数据异常，批量数据量(" + batchDataList.size() + ")必须与请求量(" + getBatchSize() + ")一致！");
        }
    }

    @Override
    public Object get(int index) {
        //在上面保证了索引不会超出
        await();
        if (batchDataList == null) {
            batchDataList = Lists.newArrayList();
        }
        if (batchDataList.size() <= index) {
            return null;
        }
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
