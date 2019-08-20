package com.fangdd.graphql.provider.dto.provider;

import java.util.List;

/**
 * @author xuwenzhen
 * @date 2019/5/14
 */
public class Api implements Comparable<Api> {
    /**
     * API编码，即{className}.{methodName}
     */
    private String code;

    /**
     * API编码，全局唯一
     */
    private String key;

    /**
     * 接口类型：0=RestFul, 1=Dubbo
     */
    private Integer type;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口版本
     */
    private String version;

    /**
     * 注释
     */
    private String comment;

    /**
     * since注释
     */
    private String since;

    /**
     * author注释
     */
    private String author;

    /**
     * deprecated 注释
     */
    private String deprecated;

    /**
     * type=restFul时有效 请求方法:GET / POST / DELETE...
     */
    private List<String> methods;

    /**
     * RestFul请求路径
     */
    private List<String> paths;

    /**
     * 响应
     */
    private EntityRef response;

    /**
     * 请求参数
     */
    private List<EntityRef> requestParams;

    /**
     * Dubbo接口客户端包的坐标信息，Dubbo接口时有值
     */
    private Artifact artifact;

    /**
     * 排序值，越小越前
     */
    private Integer order;

    /**
     * 模块名称
     *
     * @demo agent
     */
    private String moduleName;

    /**
     * Graphql的DataProvider名称
     */
    private String providerName;

    /**
     * 是否批量接口，与上面的providerName配套使用
     */
    private Boolean batchProvider;

    /**
     * batchProvider=true时，多个ID的串连字符
     */
    private String idSplitter;

    /**
     * 是否被标识为 GraphqlJson
     */
    private Boolean graphqlJson;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = deprecated;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public EntityRef getResponse() {
        return response;
    }

    public void setResponse(EntityRef response) {
        this.response = response;
    }

    public List<com.fangdd.graphql.provider.dto.provider.EntityRef> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(List<EntityRef> requestParams) {
        this.requestParams = requestParams;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public Boolean getBatchProvider() {
        return batchProvider;
    }

    public void setBatchProvider(Boolean batchProvider) {
        this.batchProvider = batchProvider;
    }

    public String getIdSplitter() {
        return idSplitter;
    }

    public void setIdSplitter(String idSplitter) {
        this.idSplitter = idSplitter;
    }

    public Boolean getGraphqlJson() {
        return graphqlJson;
    }

    public void setGraphqlJson(Boolean graphqlJson) {
        this.graphqlJson = graphqlJson;
    }

    @Override
    public int compareTo(Api o) {
        return this.order - o.order;
    }

    @Override
    public String toString() {
        return "[" + getModuleName() + ":" + getCode() + "]" + getPaths().get(0);
    }
}
