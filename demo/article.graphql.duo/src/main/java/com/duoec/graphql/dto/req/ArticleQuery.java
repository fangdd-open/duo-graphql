package com.duoec.graphql.dto.req;

/**
 *
 * @author xuwenzhen
 * @date 2019/10/12
 */
public class ArticleQuery {
    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 当前分页，1表示第一页
     * @required
     */
    private Integer pageNo;

    /**
     * 每页最大记录数
     * @required
     */
    private Integer pageSize;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
