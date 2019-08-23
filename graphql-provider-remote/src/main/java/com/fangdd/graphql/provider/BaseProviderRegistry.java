package com.fangdd.graphql.provider;

import com.fangdd.graphql.provider.annotation.GraphqlModule;
import com.fangdd.graphql.provider.annotation.IdProvider;
import com.fangdd.graphql.provider.annotation.IdsProvider;
import com.fangdd.graphql.provider.annotation.SchemaProvider;
import com.fangdd.graphql.provider.dto.ProviderModelInfo;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * base grapqhl registry
 *
 * @author xuwenzhen
 * @date 2019/9/8
 */
public abstract class BaseProviderRegistry {
    private static final Logger logger = LoggerFactory.getLogger(BaseProviderRegistry.class);
    protected static final String CHARSET_NAME = "utf-8";
    private static final String GIT_PROPERTIES = "git.properties";
    private static final Pattern COMMIT_ID_PATTERN = Pattern.compile(".*\"git.commit.id\"\\s?:\\s?\"(.*?)\".*", Pattern.MULTILINE);
    private static final String STR_DOT = ".";

    /**
     * 当前服务提供的服务领域名称（为了方便，尽量简写，比如Xf / Esf / Agent...）
     *
     * @demo Xf
     */
    @Value("${graphql.schema.module:}")
    private String schemaModuleName;

    /**
     * 指定GraphQL Schema的名称
     *
     * @demo cp
     */
    @Value("${graphql.schema.name:}")
    private String schemaName;

    /**
     * 当前服务的调用地址，用于Graphql引擎DataProvider调用本服务接口
     *
     * @demo http://web-mesh-gw
     */
    @Value("${application.server:}")
    private String server;

    /**
     * 当前服务名称，需要与Mesh网格一致
     *
     * @demo house.graphql.cp.fdd
     */
    @Value("${spring.application.name:}")
    private String applicationName;

    @Autowired
    protected ApplicationContext applicationContext;

    /**
     * 基础验证
     */
    protected void validate() {
        if (applicationName == null) {
            throw new GraphqlProviderException("未配置spring.application.name!");
        }

        String commitId = getCommitId();
        if (commitId == null || commitId.length() == 0) {
            throw new GraphqlProviderException("读取Git信息文件失败！git.commit.id为空！");
        }
    }

    protected TpDocGraphqlProviderServiceInfo getTpDocGraphqlProviderServiceInfo() {
        String commitId = getCommitId();
        if (commitId == null || commitId.length() == 0) {
            throw new GraphqlProviderException("读取Git信息文件失败！git.commit.id为空！");
        }

        if (StringUtils.isEmpty(schemaModuleName)) {
            logger.error("作为GraphQL数据供应端，graphql.schema.module不能为空！");
            return null;
        }

        TpDocGraphqlProviderServiceInfo provider = new TpDocGraphqlProviderServiceInfo();
        provider.setAppId(applicationName);
        provider.setVcsId(commitId);
        provider.setModuleName(schemaModuleName);
        provider.setServer(server);
        provider.setSchemaName(schemaName);

        List<ProviderModelInfo> models = new ArrayList<>();
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(SchemaProvider.class);
        if (beans.isEmpty()) {
            return provider;
        }

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            ProviderModelInfo modelInfo = getProvidModelInfo(entry);
            if (modelInfo != null) {
                models.add(modelInfo);
            }
        }
        if (!CollectionUtils.isEmpty(models)) {
            provider.setModels(models);
        }

        //获取@GraphqlModule注解
        setGraphqlModuleMap(provider);

        return provider;
    }

    private void setGraphqlModuleMap(TpDocGraphqlProviderServiceInfo provider) {
        Map<String, Object> graphqlModuleMap = applicationContext.getBeansWithAnnotation(GraphqlModule.class);
        if (!graphqlModuleMap.isEmpty()) {
            Map<String, String> moduleMap = new HashMap<>(8);
            graphqlModuleMap.entrySet().forEach(entry -> {
                Object controller = entry.getValue();
                Class<?> controllerClass = controller.getClass();
                String moduleName = controllerClass.getAnnotation(GraphqlModule.class).value();
                String controllerName = controllerClass.getName();
                String existsController = moduleMap.get(moduleName);
                if (existsController == null) {
                    moduleMap.put(moduleName, controllerName);
                } else {
                    moduleMap.put(moduleName, existsController + "," + controllerName);
                }
            });
            provider.setModuleMap(moduleMap);
        }
    }

    private ProviderModelInfo getProvidModelInfo(Map.Entry<String, Object> entry) {
        Object providerObj = entry.getValue();
        Class<?> controllerClass = providerObj.getClass();
        SchemaProvider schemaProvider = controllerClass.getAnnotation(SchemaProvider.class);
        ProviderModelInfo modelInfo = new ProviderModelInfo();
        modelInfo.setModelName(schemaProvider.clazz().getSimpleName());
        Set<String> refIds = new HashSet<>();
        Collections.addAll(refIds, schemaProvider.ids());
        modelInfo.setRefIds(refIds);

        Method[] methods = controllerClass.getDeclaredMethods();
        if (methods.length == 0) {
            return null;
        }
        String className = controllerClass.getName();

        boolean hasIdApi = false;
        boolean hasIdsApi = false;
        for (Method method : methods) {
            if (method.isAnnotationPresent(IdProvider.class)) {
                modelInfo.setIdProvider(className + STR_DOT + method.getName());
                hasIdApi = true;
            } else if (method.isAnnotationPresent(IdsProvider.class)) {
                modelInfo.setIdsProvider(className + STR_DOT + method.getName());
                hasIdsApi = true;
            }
        }

        if (!hasIdApi && !hasIdsApi) {
            logger.warn("{}没有指定id查询接口！", className);
            return null;
        }
        return modelInfo;
    }

    /**
     * 构建时生成的git.properties文件中读取commitId
     *
     * @return commitId
     */
    private String getCommitId() {
        String gitInfo = readResourceString(GIT_PROPERTIES);
        if (gitInfo == null || gitInfo.length() == 0) {
            throw new GraphqlProviderException("读取Git信息文件失败！");
        }

        Matcher matcher = COMMIT_ID_PATTERN.matcher(gitInfo);
        if (!matcher.find()) {
            throw new GraphqlProviderException("读取Git信息文件失败！未找到git.commit.id");
        }

        return matcher.group(1);
    }

    /**
     * 通过资源ID，获取资源文件内容
     *
     * @param resourceName 资源ID
     * @return 资源文本
     */
    protected String readResourceString(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is != null) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new InputStreamReader(is, CHARSET_NAME));
                } catch (UnsupportedEncodingException e) {
                    throw new GraphqlProviderException("读取资源文件" + resourceName + "失败！", e);
                }
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new GraphqlProviderException("读取资源" + resourceName + "文件失败！", e);
        }
        return null;
    }

    /**
     * 注册服务
     *
     * @param provider 服务信息
     */
    protected abstract void registerProvider(TpDocGraphqlProviderServiceInfo provider);
}
