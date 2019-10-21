package com.duoec.graphql.controller;

import com.duoec.graphql.BaseJunitTest;
import com.duoec.graphql.dto.req.ArticleSave;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Random;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author xuwenzhen
 */
public class ArticleControllerTest extends BaseJunitTest {
    @Test
    public void articleById() throws Exception {
        int articleId = new Random().nextInt(10);
        ResultActions result = this.mockMvc.perform(
                get("/api/article/" + articleId + "?selection=title!id")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .characterEncoding(UTF_8)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", CoreMatchers.startsWith("文章标题")));

        String content = result.andReturn().getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    public void articleByIds() throws Exception {
        Set<Integer> ids = Sets.newHashSet();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int articleId = random.nextInt(10) + 1;
            ids.add(articleId);
        }

        ResultActions result = this.mockMvc.perform(
                get("/api/article/?ids=" + Joiner.on(",").join(ids))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .characterEncoding(UTF_8)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", CoreMatchers.is(ids.size())))
                .andExpect(jsonPath("$.[0].title", CoreMatchers.startsWith("文章标题")))
                ;

        String content = result.andReturn().getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    public void articleSearch() throws Exception {
        ResultActions result = this.mockMvc.perform(
                get("/api/article/search?pageNo=1&pageSize=10")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .characterEncoding(UTF_8)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].title", CoreMatchers.startsWith("文章标题")))
                ;

        String content = result.andReturn().getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    public void articleSave() throws Exception {
        ArticleSave request = new ArticleSave();
        String title = "测试新增文章";
        request.setTitle(title);
        request.setContent("新的文章内容");
        ResultActions result = this.mockMvc.perform(
                post("/api/article")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .characterEncoding(UTF_8)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(title)));

        String content = result.andReturn().getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    public void articleDelete() throws Exception {
        int id = new Random().nextInt(99) + 1;
        ResultActions result = this.mockMvc.perform(
                delete("/api/article/" + id)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .characterEncoding(UTF_8)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)));

        String content = result.andReturn().getResponse().getContentAsString();
        System.out.println(content);
    }

}