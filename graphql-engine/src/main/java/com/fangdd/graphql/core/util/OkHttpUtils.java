package com.fangdd.graphql.core.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author xuwenzhen
 * @date 2019/4/30
 */
public class OkHttpUtils {
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
    public static final String HOST = "host";
    private static final String SELECTION_DOT = ".";
    private static final String SELECTION_TURNLINE = "!";

    public static Request.Builder getRestFulRequestBuilder(HttpUrl.Builder urlBuilder) {
        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());
        requestBuilder.addHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        return requestBuilder;
    }

    public static String formatSelections(List<String> selections) {
        if (CollectionUtils.isEmpty(selections)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        //当前路径
        List<String> paths = Lists.newArrayList();
        for (String selection : selections) {
            List<Integer> indexList = getSelectionDotIndex(selection);
            String field;
            if (!indexList.isEmpty()) {
                int level = 0;
                int start = 0;
                for (int i = 0; i < indexList.size(); i++) {
                    int index = indexList.get(i);
                    field = selection.substring(start, index);
                    start = index + 1;
                    level++;

                    if (paths.size() <= i || !paths.get(i).equals(field)) {
                        //与当前路径不一致
                        renewPath(paths, field, i);
                        sb.append(SELECTION_TURNLINE);
                        sb.append(Strings.repeat(SELECTION_DOT, level - 1));
                        sb.append(field);
                    }
                }
                field = selection.substring(start);
            } else {
                field = selection;
            }

            sb.append(SELECTION_TURNLINE);
            sb.append(Strings.repeat(SELECTION_DOT, indexList.size()));
            sb.append(field);
        }
        sb.deleteCharAt(0);
        return sb.toString();
    }

    private static void renewPath(List<String> paths, String fieldName, int index) {
        if (paths.size() > index) {
            paths.set(index, fieldName);
            while (paths.size() > index + 1) {
                paths.remove(paths.size() - 1);
            }
        } else {
            paths.add(fieldName);
        }
    }

    private static List<Integer> getSelectionDotIndex(String selection) {
        List<Integer> indexs = Lists.newArrayList();
        for (int i = 0; i < selection.length(); i++) {
            if (selection.charAt(i) == '.') {
                indexs.add(i);
            }
        }
        return indexs;
    }
}
