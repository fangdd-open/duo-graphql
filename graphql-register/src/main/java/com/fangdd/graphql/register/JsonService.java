package com.fangdd.graphql.register;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author xuwenzhen
 * @date 2019/7/31
 */
public interface JsonService {
    /**
     * 将Json字符串转换为对象
     *
     * @param jsonStr Json字符串
     * @param <T>     对应类型
     * @return 对象
     */
    <T> T toObject(String jsonStr);

    /**
     * 将Json字符串转换为对象
     *
     * @param jsonStr  Json字符串
     * @param dataPath Json路径
     * @return 对象
     */
    JsonNode toObject(String jsonStr, String dataPath);

    /**
     * 将Json字符串转换为对象
     *
     * @param jsonStr Json字符串
     * @param clazz   对应类型Class
     * @param <T>     对应类型
     * @return 对象
     */
    <T> T toObject(String jsonStr, Class<T> clazz);

    /**
     * 将对象转换成json字符串
     * @param json 对象
     * @return
     */
    String toJsonString(Object json);
}
