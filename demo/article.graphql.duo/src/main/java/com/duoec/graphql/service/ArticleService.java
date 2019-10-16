package com.duoec.graphql.service;

import com.duoec.graphql.dto.req.ArticleQuery;
import com.duoec.graphql.dto.req.ArticleSave;
import com.duoec.graphql.dto.resp.Article;
import com.duoec.graphql.dto.resp.BasePagedList;

import java.util.List;
import java.util.Set;

/**
 * @author xuwenzhen
 * @date 2019/10/12
 */
public interface ArticleService {
    /**
     * 通过文章ID获取文章信息
     *
     * @param articleId  文章ID
     * @param selections 需要查询的字段
     * @return 文章信息
     */
    Article getById(int articleId, List<String> selections);

    /**
     * 通过文章IDs，批量查询文章信息
     *
     * @param articleIds 文章IDs
     * @param selections 需要查询的字段
     * @return 文章列表
     */
    List<Article> getByIds(Set<Integer> articleIds, List<String> selections);

    /**
     * 查询文章
     *
     * @param query      查询条件
     * @param selections 需要查询的字段
     * @return 带分页信息的文章列表
     */
    BasePagedList<Article> search(ArticleQuery query, List<String> selections);

    /**
     * 保存文章
     *
     * @param request    需要保存的文章信息
     * @param selections 保存成功后，需要返回的文章字段
     * @return 保存成功后的文章信息
     */
    Article save(ArticleSave request, List<String> selections);

    /**
     * 删除文章
     *
     * @param id         文章ID
     * @param selections 需要返回删除前的文章信息字段
     * @return 删除前的文章信息
     */
    Article delete(int id, List<String> selections);
}
