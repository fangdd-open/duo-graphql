package com.fangdd.graphql.provider.dto;

import java.util.List;
import java.util.Map;

/**
 * TP-DOC项目的接入请求
 *
 * @author xuwenzhen
 * @date 2019/4/16
 */
public class TpDocGraphqlProviderServiceInfo {
    /**
     * Superdiamond 或 MeshService上的名称
     *
     * @demo house.graphql.cp.fdd
     */
    private String appId;

    /**
     * 版本ID，比如commitId
     *
     * @demo 17dd3706831f4dc183c8733ae197a1e74c2c53a4
     */
    private String vcsId;

    /**
     * Schema中的组名
     *
     * @demo xf
     * @deprecated 请使用 moduleName，本字段将在未来版本里删除！
     */
    private String group;

    /**
     * Schema中的领域名称
     *
     * @demo xf
     */
    private String moduleName;

    /**
     * 服务端地址，如果未设置，会使用全局的地址代替
     *
     * @demo http://127.0.0.1:123456
     */
    private String server;

    /**
     * Provider提供的基础视图信息
     */
    private List<ProvidModelInfo> models;

    /**
     * 如果本服务还提供了默认领域外的接口时配置
     * moduleName => controllers
     * 多个controller使用半角逗号分隔开
     */
    private Map<String, String> moduleMap;

    /**
     * GraphQL 引擎中Schema的名称
     */
    private String schemaName;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getVcsId() {
        return vcsId;
    }

    public void setVcsId(String vcsId) {
        this.vcsId = vcsId;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setGroup(String group) {
        this.moduleName = group;
    }

    /**
     * @return 领域名称
     * @deprecated 请使用 getModuleName
     */
    public String getGroup() {
        return moduleName;
    }

    public String getModuleName() {
        if (moduleName != null) {
            return moduleName;
        }
        return group;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public List<ProvidModelInfo> getModels() {
        return models;
    }

    public void setModels(List<ProvidModelInfo> models) {
        this.models = models;
    }

    public Map<String, String> getModuleMap() {
        return moduleMap;
    }

    public void setModuleMap(Map<String, String> moduleMap) {
        this.moduleMap = moduleMap;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * 为了少依赖，自己写吧...
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb
                .append("{\"appId\":").append(appId == null ? "null" : "\"" + appId + "\"")
                .append(",\"vcsId\":").append(vcsId == null ? "null" : "\"" + vcsId + "\"")
                .append(",\"group\":").append(moduleName == null ? "null" : "\"" + moduleName + "\"")
                .append(",\"moduleName\":").append(moduleName == null ? "null" : "\"" + moduleName + "\"")
                .append(",\"server\":").append(server == null ? "null" : "\"" + server + "\"")
                .append(",\"schemaName\":").append(schemaName == null ? "null" : "\"" + schemaName + "\"")
                .append(",\"models\":[");
        if (models != null && !models.isEmpty()) {
            for (int i = 0; i < models.size(); i++) {
                ProvidModelInfo model = models.get(i);
                sb.append(model.toString());
                if (i < models.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append("]");
        if (moduleMap != null && !moduleMap.isEmpty()) {
            sb.append(",\"moduleMap\":{");
            moduleMap.entrySet().forEach(entry -> {
                sb.append("\"");
                sb.append(entry.getKey());
                sb.append("\":\"");
                sb.append(entry.getValue());
                sb.append("\",");
            });
            sb.deleteCharAt(sb.length() - 1);
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }
}
