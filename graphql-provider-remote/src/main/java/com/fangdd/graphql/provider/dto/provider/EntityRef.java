package com.fangdd.graphql.provider.dto.provider;

/**
 *
 * @author xuwenzhen
 * @date 2019/5/14
 */
public class EntityRef {
    /**
     * entity.name
     */
    private String entityName;

    /**
     * 字段名称
     */
    private String name;

    /**
     * 注释
     */
    private String comment;

    /**
     * 是否必填
     */
    private boolean required = false;

    /**
     * 默认值
     */
    private String demo;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 参数注解，在RestFul参数中有，比如@PathVariable / @RequestBody等
     */
    private String annotation;

    /**
     * GraphqlField注解
     */
    private String graphqlField;

    /**
     * GraphqlDirective注解，多个以半角逗号分隔
     *
     * @demo fddImg, subList
     */
    private String graphqlDirective;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    public String getDemo() {
        return demo;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getGraphqlField() {
        return graphqlField;
    }

    public void setGraphqlField(String graphqlField) {
        this.graphqlField = graphqlField;
    }

    public String getGraphqlDirective() {
        return graphqlDirective;
    }

    public void setGraphqlDirective(String graphqlDirective) {
        this.graphqlDirective = graphqlDirective;
    }
}
