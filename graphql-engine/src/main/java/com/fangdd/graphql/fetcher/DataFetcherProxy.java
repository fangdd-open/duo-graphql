package com.fangdd.graphql.fetcher;

import com.fangdd.graphql.core.BatchDataFetcherData;
import com.fangdd.graphql.core.DataFetcherData;
import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.UserExecutionContext;
import com.fangdd.graphql.core.config.GraphqlProviderConfigure;
import com.fangdd.graphql.core.exception.GraphqlInvocationException;
import com.fangdd.graphql.core.util.*;
import com.fangdd.graphql.fetcher.batcher.BatchDataFetcherProxy;
import com.fangdd.graphql.fetcher.batcher.BatchLoader;
import com.fangdd.graphql.provider.BaseDataFetcher;
import com.fangdd.graphql.provider.ValueUtils;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.fangdd.graphql.provider.dto.provider.EntityRef;
import com.fangdd.graphql.service.DirectiveService;
import com.fangdd.graphql.service.JsonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import graphql.language.Directive;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 远端数据提供端数据获取代理
 *
 * @author xuwenzhen
 * @date 2019/4/9
 */
public class DataFetcherProxy extends BaseDataFetcher {
    private static final Logger logger = LoggerFactory.getLogger(DataFetcherProxy.class);
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(new byte[0]);

    /**
     * 用于保存路径参数与参数替换Pattern
     */
    private static final Map<String, Pattern> PATH_PARAM_PATTERN_MAP = Maps.newConcurrentMap();

    @Autowired
    private DirectiveService directiveService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private JsonService jsonService;

    @Autowired
    private GraphqlProviderConfigure graphqlProviderConfigure;

    protected Api api;

    protected TpDocGraphqlProviderServiceInfo provider;

    /**
     * api中的常量参数 => 参数值
     */
    private Map<String, Object> selectionConstMap;

    /**
     * api中的参数 => type中字段名称
     */
    private Map<String, String> selectionApiParamMap;

    /**
     * 数据提供接口的名称
     *
     * @demo userId
     */
    private String graphqlProviderName;

    /**
     * 接口响应字段返回的数据（jsonPath格式），仅在 TpdocBaseRestProvider提供的接口时有效
     *
     * @demo $.data
     */
    private String dataPath;

    public DataFetcherProxy(Api api, TpDocGraphqlProviderServiceInfo provider) {
        this.api = api;
        this.provider = provider;
    }

    @Override
    protected Object getData(DataFetchingEnvironment environment) {
        //获取需要返回的字段
        DataFetcherData dataFetcherData = SelectionUtils.analyseGql(environment);
        UserExecutionContext executionContext = environment.getContext();

        String path = environment.getExecutionStepInfo().getPath().toString();
        int index = path.indexOf(GraphqlConsts.ARRAY_INDEX_START);
        if (index > -1) {
            int end = path.indexOf(GraphqlConsts.ARRAY_INDEX_END);
            String indexStr = path.substring(index + 1, end);
            path = path.substring(0, index) + path.substring(end + 1);
            BatchLoader batcherLoader = executionContext.getBatchLoader(path);
            if (batcherLoader != null) {
                Object data = batcherLoader.get(Integer.parseInt(indexStr));
                //处理指令
                processDirective(environment, dataFetcherData, data);
                return data;
            }
        }

        List<String> selections = dataFetcherData.getSelections();

        Map<String, Object> params = getApiParamValues(environment, api);
        String body = null;
        if (params != null) {
            body = getApiObject(getProviderServer(), api, selections, params, executionContext.getHeaders());
        }
        Object data;
        if (StringUtils.isEmpty(dataPath)) {
            data = jsonService.toObject(body);
        } else {
            data = jsonService.toObject(body, dataPath);
        }

        //处理指令
        processDirective(environment, dataFetcherData, data);

        //取回数据后，会尝试分析下一步会执行的，检查是否有可以合并的
        doBatchFetch(environment, dataFetcherData, data);
        return data;
    }

    private void doBatchFetch(DataFetchingEnvironment environment, DataFetcherData dataFetcherData, Object data) {
        Map<String, BatchDataFetcherData> batchDataFetcherDataMap = dataFetcherData.getBatchDataFetcherDataMap();
        if (CollectionUtils.isEmpty(batchDataFetcherDataMap)) {
            return;
        }

        UserExecutionContext executionContext = environment.getContext();
        batchDataFetcherDataMap.entrySet().forEach(entry -> {
            BatchDataFetcherData batchDataFetcherData = entry.getValue();
            if (batchDataFetcherData == null) {
                return;
            }

            Map<String, List<Object>> params = Maps.newHashMap();
            DataFetcherProxy originDataFetcher = batchDataFetcherData.getDataFetcher();

            Api batchApi = batchDataFetcherData.getApi();
            Set<String> requestSet = Sets.newHashSet();
            batchApi.getRequestParams().forEach(rp -> requestSet.add(rp.getName()));
            originDataFetcher.selectionApiParamMap.entrySet().forEach(paramMapEntry -> {
                String paramName = paramMapEntry.getKey();
                String fieldName = paramMapEntry.getValue();
                if (!requestSet.contains(paramName)) {
                    paramName += GraphqlConsts.STR_S;
                }
                if (!requestSet.contains(paramName)) {
                    throw new GraphqlInvocationException(batchApi.toString() + "，无法找到对应的参数：" + paramName);
                }
                Object values = getParamValues(environment, data, fieldName, batchDataFetcherData.getFieldPath(), false);
                params.put(paramName, (List<Object>) values);
            });
            //处理常量参数
            Map<String, Object> selectionConstMap = originDataFetcher.getSelectionConstMap();
            if (!CollectionUtils.isEmpty(selectionConstMap)) {
                selectionConstMap.forEach((paramName, value) -> params.put(paramName, Lists.newArrayList(value)));
            }

            BatchDataFetcherProxy batchDataFetcherProxy = new BatchDataFetcherProxy();
            batchDataFetcherProxy.setServiceInfo(batchDataFetcherData.getContextModule().getProvider());
            batchDataFetcherProxy.setApi(batchApi);
            batchDataFetcherProxy.setParams(params);
            List<String> selections = batchDataFetcherData.getSelections();
            if (!CollectionUtils.isEmpty(selections) && !selections.contains(GraphqlConsts.STR_ID_LOWER)) {
                selections.add(GraphqlConsts.STR_ID_LOWER);
            }
            batchDataFetcherProxy.setSelections(selections);
            batchDataFetcherProxy.setRefIdsMerge(originDataFetcher.getApi() == batchApi);
            batchDataFetcherProxy.setPath(entry.getKey());
            GraphqlContextUtils.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(batchDataFetcherProxy);
            //发起请求
            executionContext.bathFetch(batchDataFetcherProxy);
        });
    }

    private Object getParamValues(DataFetchingEnvironment environment, Object data, String fieldName, String fieldPath, boolean overList) {
        if (data == null) {
            return overList ? Lists.newArrayList() : null;
        }
        if (Map.class.isInstance(data)) {
            //如果是个Map
            if (!StringUtils.isEmpty(fieldPath)) {
                int index = fieldPath.indexOf(GraphqlConsts.PATH_SPLITTER);
                String path;
                String subFieldPath = null;
                if (index > -1) {
                    path = fieldPath.substring(0, index);
                    subFieldPath = fieldPath.substring(index + 1);
                } else {
                    path = fieldPath;
                }
                Object fieldValue = ((Map) data).get(path);
                return getParamValues(environment, fieldValue, fieldName, subFieldPath, overList);
            }
            return ValueUtils.getParamValue(environment, data, null, fieldName);
        } else if (List.class.isInstance(data)) {
            //如果是个列表
            List<Object> values = Lists.newArrayList();
            ((List) data).forEach(item -> {
                Object itemValue = getParamValues(environment, item, fieldName, fieldPath, true);
                values.add(itemValue);
            });
            return values;
        }
        throw new GraphqlInvocationException("无法处理类型");
    }

    protected String getProviderServer() {
        if (graphqlProviderConfigure == null) {
            return provider.getServer();
        }
        String providerService = graphqlProviderConfigure.getProviderService(provider.getAppId());
        if (providerService != null) {
            return providerService;
        }
        return provider.getServer();
    }

    /**
     * 处理当前响应的指令
     *
     * @param environment     上下文环境
     * @param dataFetcherData 当前查询数据
     * @param data            当前数据
     */
    private void processDirective(DataFetchingEnvironment environment, DataFetcherData dataFetcherData, Object data) {
        Map<String, List<Directive>> fieldDirectiveMap = dataFetcherData.getFieldDirectives();
        if (CollectionUtils.isEmpty(fieldDirectiveMap)) {
            return;
        }
        directiveService.processDirective(environment, data, fieldDirectiveMap);
    }

    private String getObject(HttpMethod method, HttpUrl.Builder urlBuilder, Map<String, String> headers, RequestBody requestBody) {
        Request.Builder requestBuilder = OkHttpUtils.getRestFulRequestBuilder(urlBuilder);

        if (!StringUtils.isEmpty(provider.getAppId())) {
            //设置Mesh网格的请求头
            requestBuilder.addHeader(OkHttpUtils.HOST, provider.getAppId());
        }
        if (!CollectionUtils.isEmpty(headers)) {
            headers.entrySet().forEach(entry -> requestBuilder.addHeader(entry.getKey(), entry.getValue()));
        }

        if (HttpMethod.POST == method) {
            requestBuilder.post(requestBody);
        } else if (HttpMethod.DELETE == method) {
            requestBuilder.delete(requestBody);
        } else if (HttpMethod.PATCH == method) {
            requestBuilder.patch(requestBody);
        } else if (HttpMethod.PUT == method) {
            requestBuilder.put(requestBody);
        }

        Request request = requestBuilder.build();
        long t1 = System.currentTimeMillis();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() != HttpStatus.OK.value()) {
                throw new GraphqlInvocationException("调用发生错误，query:" + request.toString() + "，status:" + response.code());
            }

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    return null;
                }
                return responseBody.string();
            }
        } catch (IOException e) {
            throw new GraphqlInvocationException("调用失败：" + e.getMessage() + "," + request.toString(), e);
        } finally {
            logger.info("{}, 耗时：{}", urlBuilder, System.currentTimeMillis() - t1);
        }
    }

    protected String getApiObject(
            String server,
            Api api,
            List<String> selections,
            Map<String, Object> gqlParams,
            Map<String, String> headers
    ) {
        String url = server + api.getPaths().get(0);

        List<EntityRef> requestParams = api.getRequestParams();
        EntityRef requestBodyParam = null;
        List<EntityRef> urlParams = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(requestParams)) {
            for (EntityRef param : api.getRequestParams()) {
                if (GraphqlTypeUtils.REQUEST_BODY.equals(param.getAnnotation())) {
                    // @RequestBody
                    requestBodyParam = param;
                    continue;
                }
                if (!GraphqlTypeUtils.PATH_VARIABLE.equals(param.getAnnotation())) {
                    urlParams.add(param);
                    continue;
                }
                String name = param.getName();
                url = setPathParam(url, name, gqlParams.get(name));
            }
        }

        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new GraphqlInvocationException("调用发生错误，url异常:" + url);
        }

        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        if (!CollectionUtils.isEmpty(urlParams)) {
            urlParams
                    .forEach(param -> {
                        String paramName = param.getName();
                        Object paramValue = gqlParams.get(paramName);
                        if (paramValue == null) {
                            return;
                        }
                        if (Map.class.isInstance(paramValue)) {
                            //Map
                            Map<String, Object> paramMap = (Map<String, Object>) paramValue;
                            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                                addUrlParams(urlBuilder, entry.getKey(), entry.getValue());
                            }
                        } else {
                            addUrlParams(urlBuilder, paramName, paramValue);
                        }
                    });
        }

//        if (!CollectionUtils.isEmpty(selectionConstMap)) {
//            selectionConstMap.entrySet().forEach(entry -> addUrlParams(urlBuilder, entry.getKey(), entry.getValue()));
//        }
        //调用数据
        HttpMethod method = DataFetchUtils.getHttpMethod(api);
        String selectionStr = null;
        if (!CollectionUtils.isEmpty(selections)) {
            selectionStr = OkHttpUtils.formatSelections(selections);
            urlBuilder.addQueryParameter(GraphqlConsts.STR_SELECTIONS, selectionStr);
        }

        RequestBody requestBody;
        try {
            requestBody = getRequestBody(requestBodyParam, gqlParams);
        } catch (Exception e) {
            throw new GraphqlInvocationException("构建RequestBody失败：[" + method.name() + "]urlBuilder=" + urlBuilder + ",selections=" + selectionStr, e);
        }
        return getObject(method, urlBuilder, headers, requestBody);
    }

    private void addUrlParams(HttpUrl.Builder urlBuilder, String paramName, Object paramValue) {
        if (paramValue == null) {
            urlBuilder.addEncodedQueryParameter(paramName, GraphqlConsts.STR_EMPTY);
            return;
        }
        if (List.class.isInstance(paramValue)) {
            ((List) paramValue).forEach(pv -> addUrlParams(urlBuilder, paramName, pv));
        } else {
            urlBuilder.addEncodedQueryParameter(paramName, paramValue.toString());
        }
    }

    private RequestBody getRequestBody(EntityRef requestBodyParam, Map<String, Object> gqlParams) throws JsonProcessingException {
        if (requestBodyParam == null) {
            return EMPTY_REQUEST_BODY;
        }
        String paramName = requestBodyParam.getName();
        Object val = gqlParams.get(paramName);
        if (val == null) {
            return EMPTY_REQUEST_BODY;
        }

        return MultipartBody.create(objectMapper.writeValueAsBytes(val));
    }

    private String setPathParam(String url, String paramName, Object paramValue) {
        Pattern pattern = getPathParamPattern(paramName);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            url = matcher.replaceAll(paramValue.toString());
        }
        return url;
    }

    protected static Pattern getPathParamPattern(String pathParamName) {
        Pattern pathParamPattern = PATH_PARAM_PATTERN_MAP.get(pathParamName);
        if (pathParamPattern != null) {
            return pathParamPattern;
        }
        Pattern pattern = Pattern.compile("\\{\\s*" + pathParamName + "(\\s*:.*?)?\\}");
        PATH_PARAM_PATTERN_MAP.put(pathParamName, pattern);
        return pattern;
    }

    /**
     * 获取某个Selection的参数名称
     *
     * @param selection selection字段
     * @return selection对应的字段
     */
    public String getParamName(String selection) {
        if (selectionApiParamMap == null) {
            return selection;
        }
        String mappingName = selectionApiParamMap.get(selection);
        return StringUtils.isEmpty(mappingName) ? selection : mappingName;
    }

    public void addExtraSelection(String apiParamName, String selectionName) {
        if (this.selectionApiParamMap == null) {
            this.selectionApiParamMap = Maps.newHashMap();
        }
        this.selectionApiParamMap.put(apiParamName, selectionName);
    }

    public void setExtraSelections(List<String> extraSelections) {
        if (CollectionUtils.isEmpty(extraSelections)) {
            return;
        }
        if (this.getDependencyFields() == null) {
            this.setDependencyFields(Lists.newArrayList());
        }
        this.selectionApiParamMap = Maps.newHashMap();
        extraSelections.forEach(selectionConfig -> {
            int i = selectionConfig.indexOf(GraphqlConsts.STR_MAP);
            String selection = selectionConfig;
            if (i == -1) {
                //不包含映射关系
                i = selectionConfig.indexOf(GraphqlConsts.STR_EQ);
                if (i == -1) {
                    selectionApiParamMap.put(selectionConfig, selectionConfig);
                    addDependencyField(selectionConfig);
                } else {
                    //带了常量的
                    selection = selectionConfig.substring(0, i);
                    String constValue = selectionConfig.substring(i + 1).trim();
                    addApiConstParam(selection, constValue);
                }
            } else {
                selection = selectionConfig.substring(0, i);
                addDependencyField(selection);
                selectionApiParamMap.put(selectionConfig.substring(i + 2), selection);
            }
        });
    }

    private void addApiConstParam(String selection, String constValue) {
        Optional<EntityRef> paramOption = api.getRequestParams().stream().filter(param -> param.getName().equals(selection)).findFirst();
        if (!paramOption.isPresent()) {
            return;
        }
        if (selectionConstMap == null) {
            selectionConstMap = Maps.newHashMap();
        }
        selectionConstMap.put(selection, setConstValue(paramOption.get(), constValue));
    }

    private Object setConstValue(EntityRef entityRef, String constValue) {
        return DataFetchUtils.convertStringValue(entityRef.getEntityName(), constValue);
    }

    /**
     * 从上下文中抽取接口需要的参数值
     *
     * @param environment 上下文环境
     * @param api         接口
     * @return
     */
    private Map<String, Object> getApiParamValues(DataFetchingEnvironment environment, Api api) {
        Map<String, Object> apiParamValues = Maps.newHashMap();
        List<EntityRef> requestParams = api.getRequestParams();
        if (!CollectionUtils.isEmpty(requestParams)) {
            for (EntityRef param : api.getRequestParams()) {
                String paramName = param.getName();
                String fieldName = getParamName(paramName);
                Object paramValue = ValueUtils.getParamValue(environment, environment.getSource(), getSelectionConstMap(), fieldName);
                if (paramValue == null && param.isRequired()) {
                    //如果是必须时
                    logger.warn("接口：{}，必填参数：{}缺失！", api, paramName);
                    return null;
                }
                apiParamValues.put(paramName, paramValue);
            }
        }

        return apiParamValues;
    }

    /**
     * 获取变量映射表
     *
     * @return 变量映射表
     */
    public Map<String, Object> getSelectionConstMap() {
        return selectionConstMap;
    }

    public String getGraphqlProviderName() {
        return graphqlProviderName;
    }

    public void setGraphqlProviderName(String graphqlProviderName) {
        this.graphqlProviderName = graphqlProviderName;
    }

    public Api getApi() {
        return api;
    }

    /**
     * 获取领域名称
     *
     * @return 领域名称
     */
    @Override
    public String getModuleName() {
        return api.getModuleName();
    }

    /**
     * 获取本DataFetcher返回的GraphQL类型
     *
     * @return GraphQL类型
     */
    @Override
    public GraphQLOutputType getResponseGraphqlType() {
        return null;
    }

    public TpDocGraphqlProviderServiceInfo getProvider() {
        return provider;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getDataPath() {
        return dataPath;
    }
}
