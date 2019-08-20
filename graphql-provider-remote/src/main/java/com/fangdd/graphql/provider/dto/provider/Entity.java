package com.fangdd.graphql.provider.dto.provider;

import java.util.List;

public class Entity {
    /**
     * UUID
     * MongoDB属性
     */
    private String id;

    /**
     * doc._id
     * MongoDB属性
     */
    private String docId;

    /**
     * 版本号，直接使用更新时间即可
     * MongoDB属性
     */
    private Long docVersion;

    /**
     * 名称
     */
    private String name;

    /**
     * since
     */
    private String since;

    /**
     * deprecated
     */
    private String deprecated;

    /**
     * 测试数据
     */
    private String demo;

    /**
     * 注释
     */
    private String comment;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 本类下的属性列表
     */
    private List<EntityRef> fields;

    /**
     * 本类的泛型
     */
    private List<EntityRef> parameteredEntityRefs;

    /**
     * 是否集合
     */
    private Boolean collection;

    /**
     * 是否Map
     */
    private Boolean map;

    /**
     * 是否原始数据类型： int / long / boolean / double / ...
     */
    private Boolean primitive;

    /**
     * 是否枚举
     */
    private Boolean enumerate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Long getDocVersion() {
        return docVersion;
    }

    public void setDocVersion(Long docVersion) {
        this.docVersion = docVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = deprecated;
    }

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setFields(List<EntityRef> fields) {
        this.fields = fields;
    }

    public List<EntityRef> getFields() {
        return fields;
    }

    public void setParameteredEntityRefs(List<EntityRef> parameteredEntityRefs) {
        this.parameteredEntityRefs = parameteredEntityRefs;
    }

    public List<EntityRef> getParameteredEntityRefs() {
        return parameteredEntityRefs;
    }

    public Boolean getCollection() {
        return collection;
    }

    public void setCollection(Boolean collection) {
        this.collection = collection;
    }

    public Boolean getMap() {
        return map;
    }

    public void setMap(Boolean map) {
        this.map = map;
    }

    public Boolean getPrimitive() {
        return primitive;
    }

    public void setPrimitive(Boolean primitive) {
        this.primitive = primitive;
    }

    public Boolean getEnumerate() {
        return enumerate;
    }

    public void setEnumerate(Boolean enumerate) {
        this.enumerate = enumerate;
    }
}
