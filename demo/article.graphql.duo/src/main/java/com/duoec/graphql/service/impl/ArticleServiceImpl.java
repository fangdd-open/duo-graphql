package com.duoec.graphql.service.impl;

import com.duoec.graphql.dao.ArticleDao;
import com.duoec.graphql.dto.req.ArticleQuery;
import com.duoec.graphql.dto.req.ArticleSave;
import com.duoec.graphql.dto.resp.Article;
import com.duoec.graphql.dto.resp.BasePagedList;
import com.duoec.graphql.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 *
 * @author xuwenzhen
 * @date 2019/10/12
 */
@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleDao articleDao;

    /**
     * 通过文章ID获取文章信息
     *
     * @param articleId  文章ID
     * @param selections 需要查询的字段
     * @return 文章信息
     */
    @Override
    public Article getById(int articleId, List<String> selections) {
        return articleDao.getById(articleId, selections);
    }

    /**
     * 通过文章IDs，批量查询文章信息
     *
     * @param articleIds 文章IDs
     * @param selections 需要查询的字段
     * @return 文章列表
     */
    @Override
    public List<Article> getByIds(Set<Integer> articleIds, List<String> selections) {
        return articleDao.getByIds(articleIds, selections);
    }

    /**
     * 查询文章
     *
     * @param query      查询条件
     * @param selections 需要查询的字段
     * @return 带分页信息的文章列表
     */
    @Override
    public BasePagedList<Article> search(ArticleQuery query, List<String> selections) {
        return articleDao.search(query, selections);
    }

    /**
     * 保存文章
     *
     * @param request    需要保存的文章信息
     * @param selections 保存成功后，需要返回的文章字段
     * @return 保存成功后的文章信息
     */
    @Override
    public Article save(ArticleSave request, List<String> selections) {
        int id = articleDao.save(request);
        return getById(id, selections);
    }

    /**
     * 删除文章
     *
     * @param id         文章ID
     * @param selections 需要返回删除前的文章信息字段
     * @return 删除前的文章信息
     */
    @Override
    public Article delete(int id, List<String> selections) {
        Article article = articleDao.getById(id, selections);
        articleDao.deleteById(id);
        return article;
    }
}
