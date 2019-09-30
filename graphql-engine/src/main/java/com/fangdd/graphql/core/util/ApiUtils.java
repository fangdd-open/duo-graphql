package com.fangdd.graphql.core.util;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.provider.dto.provider.Api;

/**
 *
 * @author xuwenzhen
 * @date 2019/9/25
 */
public class ApiUtils {
    public static String getApiName(Api api) {
        String code = api.getCode();
        int index = code.lastIndexOf(GraphqlConsts.CHAR_DOT);
        if (index != -1) {
            code = code.substring(index + 1);
        }
        return code;
    }
}
