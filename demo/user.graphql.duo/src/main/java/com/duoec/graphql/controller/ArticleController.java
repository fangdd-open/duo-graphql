package com.duoec.graphql.controller;

import com.duoec.graphql.dto.req.ArticleQuery;
import com.duoec.graphql.dto.req.ArticleSave;
import com.duoec.graphql.dto.resp.Article;
import com.duoec.graphql.dto.resp.BasePagedList;
import com.duoec.graphql.service.ArticleService;
import com.fangdd.graphql.provider.SelectionHandler;
import com.fangdd.graphql.provider.annotation.*;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 *
 * @author xuwenzhen
 * @date 2019/10/12
 */
@RestController
@GraphqlModule("article") //可选，声明当前的领域，如果与配置中的graphql.schema.module值一致，则可以省略，一个GraphQL Provider允许实现多个领域，这也是为了在项目之初某些领域比较小时，先寄放在其它项目里面，可选
@RequestMapping("/api/article")
@SchemaProvider(clazz = Article.class, ids = {"articleId", "wzId"}) //声明为标准视图，并向外注册了这两个外键
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    /**
     * 通过文章ID拉取文章信息 (这些注释，会映射成GraphQL Schema里的注释)
     *
     * @param articleId 文章ID (这些注释，会映射成GraphQL Schema里的注释)
     * @return 文章信息
     */
    @IdProvider //声明成IdProvider，通过外键关联的就是通过此接口获取到实体的详情信息的
    @GetMapping("/{articleId:\\d+}")
    public Article articleById(@PathVariable int articleId) {
        return articleService.getById(
                articleId,
                //如果实现了selections，则可以精确的返回查询需要的字段，而不是全量返回，推荐都实现！selections的结构参见另外的文档
                SelectionHandler.getSelections()
        );
    }

    /**
     * 通过文章ids拉取文章列表
     *
     * @param ids 文章ids，多个id使用半角逗号分隔
     * @return 文章列表
     */
    @IdsProvider //批量查询接口，引擎检测到可以合并请求时，会合并多个id，调用这个接口。默认分隔符是半角逗号，也可以配置当前注解属性进行修改。批量接口，返回不需要按ids的顺序返回，而是交由引擎处理
    @GetMapping
    public List<Article> articleByIds(@RequestParam String ids) {
        Set<Integer> articleIds = Sets.newHashSet();
        Splitter.on(",")
                .omitEmptyStrings()
                .omitEmptyStrings()
                .split(ids)
                .forEach(idStr -> articleIds.add(Integer.parseInt(idStr)));
        return articleService.getByIds(articleIds, SelectionHandler.getSelections());
    }

    /**
     * 查询带分页的文章列表
     *
     * @param query 文章筛选条件
     * @return 文章列表
     */
    @GraphqlSelection("list") //如果返回的字段里包裹了多层，可以通过此声明指定selection的前缀
    @GetMapping("/search") //所有的Query都必须是Get请求，否则会变成Mutation！
    public BasePagedList<Article> articleSearch(ArticleQuery query) { //生成Schema时，会直接使用当前的方法名，所以请注意当前领域下的命名不要冲突，和前端的可识别性
        return articleService.search(query, SelectionHandler.getSelections());
    }

    /**
     * 文章保存
     *
     * @param request 保存请求参数
     * @return 保存成功后的文章信息
     */
    @PostMapping //非Get方法，会生成到GraphQL Schema的Mutation内，如果有需要写操作后也可以返回需要的字段
    public Article articleSave(@RequestBody ArticleSave request) {
        return articleService.save(request, SelectionHandler.getSelections());
    }

    /**
     * 删除文章
     * @param id 文章ID
     */
    @DeleteMapping ("/{id:\\d+}") //非Get方法，会生成到GraphQL Schema的Mutation内，如果有需要写操作后也可以返回需要的字段
    public Article articleDelete(@PathVariable int id) {
        return articleService.delete(id, SelectionHandler.getSelections());
    }
}
