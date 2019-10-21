package com.duoec.graphql.dao;

import com.duoec.graphql.core.exceptions.BaseServiceException;
import com.duoec.graphql.dto.req.ArticleQuery;
import com.duoec.graphql.dto.req.ArticleSave;
import com.duoec.graphql.dto.resp.Article;
import com.duoec.graphql.dto.resp.BasePagedList;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 模拟数据库、ES操作，这里把数据写入redis中
 *
 * @author xuwenzhen
 * @date 2019/10/12
 */
@Repository
public class ArticleDao {
    private static final LinkedList<Article> ARTICLE_LIST = Lists.newLinkedList();
    private static final int ARTICLE_SIZE = 100;

    /**
     * 初始化文章
     */
    @PostConstruct
    public void initArticleData() {
        for (int i = 0; i < ARTICLE_SIZE; i++) {
            Article article = new Article();
            int id = i + 1;
            article.setId(id);
            article.setTitle("文章标题" + id);
            article.setContent("文章内容：" + id);
            ARTICLE_LIST.add(article);
        }
    }

    /**
     * 通过文章ID获取文章信息
     *
     * @param articleId  文章ID
     * @param selections 需要筛选的字段 （本方法暂且不实现）
     * @return 文章基本信息
     */
    public Article getById(int articleId, List<String> selections) {
        Optional<Article> articleOpt = ARTICLE_LIST.stream().filter(article -> article.getId().equals(articleId)).findFirst();
        return articleOpt.isPresent() ? articleOpt.get() : null;
    }

    public List<Article> getByIds(Set<Integer> articleIds, List<String> selections) {
        List<Article> articleList = Lists.newArrayList();
        articleIds.forEach(articleId -> articleList.add(getById(articleId, selections)));

        return articleList;
    }

    public BasePagedList<Article> search(ArticleQuery query, List<String> selections) {
        List<Article> articleList = Lists.newArrayList();
        BasePagedList<Article> pagedList = new BasePagedList<>();
        pagedList.setList(articleList);


        int pageSize = query.getPageSize();
        int pageNo = query.getPageNo();
        int skip = 0;
        int total = 0;
        String keyword = query.getKeyword();

        for (Article article : ARTICLE_LIST) {
            boolean match = StringUtils.isEmpty(keyword) || article.getTitle().contains(keyword);

            if (match) {
                total++;
                if (skip < (pageNo - 1) * pageSize || articleList.size() >= pageSize) {
                    //跳过
                    skip++;
                } else {
                    articleList.add(article);
                }
            }
        }
        pagedList.setTotal(total);
        return pagedList;
    }

    public int save(ArticleSave request) {
        Integer id = request.getId();
        Article article = new Article();
        String title = request.getTitle();
        if (StringUtils.isEmpty(title)) {
            throw new BaseServiceException(500, "文章标题不能为空！");
        }
        article.setTitle(title);
        article.setContent(request.getContent());
        if (id == null) {
            //新增
            id = ARTICLE_LIST.getLast().getId() + 1;
            article.setId(id);
            ARTICLE_LIST.add(article);
            return id;
        }

        //修改
        Optional<Article> articleOpt = ARTICLE_LIST.stream()
                .filter(item -> item.getId().equals(request.getId()))
                .findFirst();
        if (!articleOpt.isPresent()) {
            //找不到
            return 0;
        }
        Article existsArticle = articleOpt.get();
        existsArticle.setTitle(title);
        existsArticle.setContent(request.getContent());
        return existsArticle.getId();
    }

    public void deleteById(int id) {
        for (int i = 0; i < ARTICLE_LIST.size(); i++) {
            Article article = ARTICLE_LIST.get(i);
            if (article.getId().equals(id)) {
                ARTICLE_LIST.remove(i);
                return;
            }
        }
    }
}
