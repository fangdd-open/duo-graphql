package com.fangdd.graphql.provider.dto;

/**
 * 关联的ID，一般ES里面会将实体部分信息放进来，但我们只需要一个id，为此抽象出此实体，用于承载实体ID
 *
 * @author xuwenzhen
 * @date 2019/5/24
 */
public class RefId {
    /**
     * 关联ID,一般不会直接使用当前ID，而是使用关联的实体
     *
     * @demo 12323
     */
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
