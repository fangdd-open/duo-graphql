package com.duoec.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author xuwenzhen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GraphqlProviderApplication.class)
public abstract class BaseJunitTest {
    protected static final String UTF_8 = "UTF-8";

    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Before
    public void evnLoad() {
        //初始化 mvc 测试类
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @After
    public void tested() {

    }
}
