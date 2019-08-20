package com.fangdd.graphql.provider.dto;

/**
 *
 * @author xuwenzhen
 * @date 2019/4/9
 */
public class EntityDto {
    /**
     * 如果是参数时，为参数名称，如果是响应体时为空
     * @demo ids
     */
    private String name;

    /**
     * 类型的全名称
     * @demo java.util.List<java.util.Long>;
     */
    private String fullType;

    /**
     * 描述
     * @demo 楼盘IDs列表
     */
    private String desc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullType() {
        return fullType;
    }

    public void setFullType(String fullType) {
        this.fullType = fullType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
