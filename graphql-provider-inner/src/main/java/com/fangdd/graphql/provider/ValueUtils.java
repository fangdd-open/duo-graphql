package com.fangdd.graphql.provider;

import graphql.schema.DataFetchingEnvironment;

import java.util.Map;

/**
 * @author xuwenzhen
 * @date 2019/8/2
 */
public class ValueUtils {
    private ValueUtils() {
    }

    public static <T> T getParamValue(
            DataFetchingEnvironment environment,
            Object extraData,
            Map<String, Object> constParamValues,
            String fieldName
    ) {
        if (constParamValues != null && !constParamValues.isEmpty()) {
            //尝试从常量中取
            if (constParamValues.containsKey(fieldName)) {
                return (T) constParamValues.get(fieldName);
            }
        }

        Object paramValue = environment.getArgument(fieldName);
        if (paramValue != null) {
            return (T) paramValue;
        }

        //尝试从上下文中找
        if (extraData != null && Map.class.isInstance(extraData)) {
            paramValue = ((Map<String, Object>) extraData).get(fieldName);
            if (paramValue != null) {
                return (T) paramValue;
            }
        }

        return (T) paramValue;
    }
}
