package com.duoec.graphql.dto.req;

import java.util.List;

/**
 *
 * @author xuwenzhen
 * @date 2019/10/12
 */
public class ArticleQuery {
    /**
     * 需要查询的文章IDs
     */
    private List<Integer> ids;

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

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

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
