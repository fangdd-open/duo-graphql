package com.fangdd.graphql.core.util;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.exception.GraphqlBuildException;
import com.fangdd.graphql.provider.dto.provider.Api;
import com.google.common.base.Strings;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author xuwenzhen
 * @date 2019/7/9
 */
public class DataFetchUtils {
    private static final String PATH_SPLITTER = "/";
    private static final String INT = "int";
    private static final String INTEGER = "Integer";
    private static final String LONG = "long";
    private static final String LONG1 = "Long";
    private static final String DOUBLE = "double";
    private static final String FLOAT = "float";
    private static final String DOUBLE1 = "Double";
    private static final String FLOAT1 = "Float";
    private static final String STRING = "String";

    private static final int MIN_STR_CONST_VALUE_LEN = 3;

    private DataFetchUtils() {
    }

    public static HttpMethod getHttpMethod(Api api) {
        if (api == null) {
            return HttpMethod.GET;
        }
        List<String> methods = api.getMethods();
        return StringUtils.isEmpty(methods) ? HttpMethod.GET : HttpMethod.valueOf(methods.get(0).toUpperCase());
    }

    public static String getFieldPath(DataFetchingEnvironment environment, String path) {
        String key = environment.getExecutionStepInfo().getPath().toString();
        if (!StringUtils.isEmpty(path)) {
            key += PATH_SPLITTER + path;
        }
        return key;
    }

    public static Object convertStringValue(String typeName, String strValue) {
        if (INT.equals(typeName) || typeName.endsWith(INTEGER)) {
            return Integer.parseInt(strValue);
        } else if (LONG.equals(typeName) || typeName.endsWith(LONG1)) {
            return Long.parseLong(strValue);
        } else if (DOUBLE.equals(typeName) || typeName.endsWith(DOUBLE1)) {
            return Double.parseDouble(strValue);
        } else if (FLOAT.equals(typeName) || typeName.endsWith(FLOAT1)) {
            return Float.parseFloat(strValue);
        } else if (typeName.endsWith(STRING)) {
            if (strValue.length() < MIN_STR_CONST_VALUE_LEN) {
                return "";
            }
            return strValue.substring(1, strValue.length() - 2);
        }

        return null;
    }

    /**
     * 添加接口到一个Map里去
     * @param api 接口
     * @param apiMap Map容器
     */
    public static void putApi(Api api, Map<String, Api> apiMap) {
        Api existsApi = apiMap.get(api.getCode());
        if (existsApi != null) {
            throw new GraphqlBuildException("重复方法签名:" + existsApi + "," + api);
        }
        apiMap.put(api.getCode(), api);
        String providerName = api.getProviderName();
        if (!Strings.isNullOrEmpty(providerName)) {
            Boolean batchProvider = api.getBatchProvider();
            String providerKey = GraphqlConsts.STR_AT + providerName;
            if (batchProvider != null && batchProvider) {
                //批量接口
                providerKey += "!";
            }
            existsApi = apiMap.get(providerKey);
            if (existsApi != null) {
                throw new GraphqlBuildException("重复方法签名:" + existsApi + "," + api);
            }
            apiMap.put(providerKey, api);
        }
    }
}
