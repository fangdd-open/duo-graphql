package com.fangdd.graphql.fetcher;

import com.fangdd.graphql.core.config.WebConfigure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author wtx
 * @date 2019/7/1 19:22
 */
public class BaseDataFetcherTest {



    @Test
    public void getObject() throws IOException {
        ObjectMapper objectMapper = new WebConfigure().getObjectMapper();
        objectMapper.writer();
        String s = objectMapper.readValue("\"123,123\"", String.class);
        Assert.assertEquals(s,"123,123");
    }
}