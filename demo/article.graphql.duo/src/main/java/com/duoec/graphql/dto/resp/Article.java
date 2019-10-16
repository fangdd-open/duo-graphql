package com.duoec.graphql.dto.resp;

/**
 * 文章（这个实体也可以放在其它包下。这里的注释会生成GraphQL Schema里对应Type的注释）
 * @author xuwenzhen
 */
public class Article {
    /**
     * 文章ID (这个注释很重要，会直接映射成GraphQL Schema文档里的注释，标准视图的id必须是id，不用用其它，比如_id)
     */
    private Integer id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
