package com.fangdd.graphql.fetcher.batcher;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.exception.GraphqlEngineException;
import com.fangdd.graphql.core.util.GraphqlContextUtils;
import com.fangdd.graphql.fetcher.DataFetcherProxy;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.fangdd.graphql.provider.dto.provider.Entity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 批量加载基类
 *
 * @author xuwenzhen
 * @date 2019/7/9
 */
public abstract class BaseBatchLoader extends DataFetcherProxy implements BatchLoader {
    private static final String STRING_CLASS_NAME = String.class.getName();

    private CountDownLatch countDownLatch;

    private String path;

    private Map<String, List<Object>> params;

    private Map<String, String> headers;

    private List<String> selections;

    /**
     * 是否是refIds方法合并的
     */
    private boolean refIdsMerge;

    /**
     * 如果refIdsMerge=true，则当前存储各段数量
     */
    private List<List<Object>> refIdsList;

    /**
     * 批量查询多少数据？
     */
    private int[] batchSize = {0};

    /**
     * 批处理构造方法
     *
     * @param api      批量接口
     * @param provider GraphQL Provider
     */
    public BaseBatchLoader(Api api, TpDocGraphqlProviderServiceInfo provider) {
        super(api, provider);
    }

    /**
     * 批量加载数据
     */
    @Override
    public void fetchData() {
        countDownLatch = new CountDownLatch(1);
        try {
            batchRequest();
        } finally {
            countDownLatch.countDown();
        }
    }

    /**
     * 处理批量处理结果
     *
     * @param data 返回结果
     */
    protected abstract void processResponseData(String data);

    /**
     * 发起批量加载
     */
    private void batchRequest() {
        Map<String, Object> paramMap = Maps.newHashMap();
        boolean[] paramReady = new boolean[]{true};
        this.api.getRequestParams().forEach(param -> {
            if (!paramReady[0]) {
                return;
            }
            String name = param.getName();
            List<Object> paramValues = params.get(name);
            if (paramValues == null) {
                if (name.endsWith(GraphqlConsts.STR_S)) {
                    String newName = name.substring(0, name.length() - 1);
                    paramValues = params.get(newName);
                }
            }

            if (paramValues == null && param.isRequired()) {
                //如果为null时，不必发起请求
                paramReady[0] = false;
                return;
            }

            String entityName = param.getEntityName();
            Entity entity = GraphqlContextUtils.getEntity(entityName);
            Assert.notNull(entity, "无法匹配批量接口：" + this.api + "参数：" + entityName + "类型定义！");
            boolean isString = STRING_CLASS_NAME.equals(entityName);
            boolean isCollection = entity.getCollection() != null && entity.getCollection();
            if (isCollection || isString) {
                //如果是个列表 或 字段串时
                paramMap.put(name, getParamValues(paramValues));
                batchSize[0] = paramValues.size();
            } else {
                //如果是其它类型时，会直接取第一个参数的值
                paramMap.put(name, paramValues.get(0));
            }
        });

        String data;
        if (!paramReady[0]) {
            //参数没准备好，有些值为空 ！
            data = null;
        } else {
            data = getApiObject(getProviderServer(), this.api, selections, paramMap, headers);
        }

        processResponseData(data);
    }

    private String getParamValues(List<Object> paramValues) {
        if (paramValues == null) {
            return GraphqlConsts.STR_EMPTY;
        }

        if (!refIdsMerge) {
            return join(paramValues);
        }
        refIdsList = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paramValues.size(); i++) {
            List<Object> ps = (List<Object>) paramValues.get(i);
            refIdsList.add(ps);
            if (i > 0) {
                sb.append(GraphqlConsts.STR_COMMA);
            }
            sb.append(join(ps));
        }
        return sb.toString();
    }

    private String join(List<Object> paramValues) {
        if (CollectionUtils.isEmpty(paramValues)) {
            return GraphqlConsts.STR_EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paramValues.size(); i++) {
            Object v = paramValues.get(i);
            if (i > 0) {
                sb.append(GraphqlConsts.STR_COMMA);
            }
            if (v != null) {
                sb.append(v);
            }
        }

        return sb.toString();
    }

    /**
     * 等待执行完毕
     */
    protected void await() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraphqlEngineException("countDownLatch.await() error", e);
        }
    }

    protected int getBatchSize() {
        return batchSize[0];
    }

    public Map<String, List<Object>> getParams() {
        return params;
    }

    @Override
    public void setParams(Map<String, List<Object>> params) {
        this.params = params;
    }

    public List<String> getSelections() {
        return selections;
    }

    @Override
    public void setSelections(List<String> selections) {
        this.selections = selections;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setRefIdsMerge(boolean refIdsMerge) {
        this.refIdsMerge = refIdsMerge;
    }

    boolean isRefIdsMerge() {
        return refIdsMerge;
    }

    List<List<Object>> getRefIdsList() {
        return refIdsList;
    }
}
