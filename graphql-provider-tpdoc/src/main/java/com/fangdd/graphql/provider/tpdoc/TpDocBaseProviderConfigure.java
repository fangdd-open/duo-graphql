package com.fangdd.graphql.provider.tpdoc;

import com.fangdd.graphql.provider.BaseSchemaProviderController;
import com.fangdd.graphql.provider.GraphqlProviderException;
import com.fangdd.graphql.provider.annotation.GraphqlModule;
import com.fangdd.graphql.provider.annotation.IdProvider;
import com.fangdd.graphql.provider.annotation.IdsProvider;
import com.fangdd.graphql.provider.annotation.SchemaProvider;
import com.fangdd.graphql.provider.dto.ProvidModelInfo;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 注册当前服务为Graphql Provider
 *
 * @author xuwenzhen
 * @date 2019/4/16
 */
@Component
public class TpDocBaseProviderConfigure {
    private static final Logger logger = LoggerFactory.getLogger(TpDocBaseProviderConfigure.class);
    private static final String CHARSET_NAME = "utf-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
    private static final String GIT_PROPERTIES = "git.properties";
    private static final Pattern COMMIT_ID_PATTERN = Pattern.compile(".*\"git.commit.id\"\\s?:\\s?\"(.*?)\".*", Pattern.MULTILINE);
    private static final String HOST = "host";
    private static final int HTTP_CODE_SUCCESS = 200;

    /**
     * Graphql TP-DOC注册器地址
     *
     * @demo http://127.0.0.1:17040/api/register/tpdoc
     */
    @Value("${graphql.registry.tpdoc-address}")
    private String tpdocRegistryAddress;

    /**
     * Graphql服务的Mesh网格appId
     */
    @Value("${graphql.registry.host:}")
    private String tpdocRegistryHost;

    /**
     * 当前服务提供的服务领域名称（为了方便，尽量简写，比如Xf / Esf / Agent...）
     *
     * @demo Xf
     * @deprecated 请使用配置：graphql.schema.module
     */
    @Value("${graphql.schema.group:}")
    private String schemaGroupName;

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
    public String applicationName;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OkHttpClient okHttpClient;

    @PostConstruct
    public void registryService() {
        if (applicationName == null) {
            throw new GraphqlProviderException("未配置spring.application.name!");
        }

        String commitId = getCommit();
        if (commitId == null || commitId.length() == 0) {
            throw new GraphqlProviderException("读取Git信息文件失败！git.commit.id为空！");
        }

        TpDocGraphqlProviderServiceInfo provider = getTpDocGraphqlProviderServiceInfo(commitId);
        if (provider == null) {
            return;
        }
        if (tpdocRegistryHost != null && tpdocRegistryHost.length() > 0) {
            //使用了Mesh网格，需要设置
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        }

        //获取@GraphqlModule注解
        setGraphqlModuleMap(provider);

        //上报
        registerProvider(provider);
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

    /**
     * 构建时生成的git.properties文件中读取commitId
     *
     * @return commitId
     */
    private String getCommit() {
        String gitInfo = getResourceFileAsString(GIT_PROPERTIES);
        if (gitInfo == null || gitInfo.length() == 0) {
            throw new GraphqlProviderException("读取Git信息文件失败！");
        }

        Matcher matcher = COMMIT_ID_PATTERN.matcher(gitInfo);
        if (!matcher.find()) {
            throw new GraphqlProviderException("读取Git信息文件失败！未找到git.commit.id");
        }

        return matcher.group(1);
    }

    private String getResourceFileAsString(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is != null) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new InputStreamReader(is, CHARSET_NAME));
                } catch (UnsupportedEncodingException e) {
                    throw new GraphqlProviderException("读取Git信息文件" + GIT_PROPERTIES + "失败！是否构建代码中没有添加插件：pl.project13.maven:git-commit-id-plugin", e);
                }
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new GraphqlProviderException("读取Git信息文件失败！", e);
        }
        return null;
    }

    private void registerProvider(TpDocGraphqlProviderServiceInfo provider) {
        HttpUrl httpUrl = HttpUrl.parse(tpdocRegistryAddress);
        if (httpUrl == null) {
            logger.error("调用发生错误，url异常:{}", tpdocRegistryAddress);
            return;
        }

        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());
        requestBuilder.addHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        if (!StringUtils.isEmpty(tpdocRegistryHost)) {
            //设置Mesh网格的请求头
            requestBuilder.addHeader(HOST, tpdocRegistryHost);
        }
        RequestBody requestBody = null;
        try {
            requestBody = MultipartBody.create(provider.toString().getBytes(CHARSET_NAME));
        } catch (UnsupportedEncodingException e) {
            logger.error("不支持字符编码：{}", CHARSET_NAME, e);
            return;
        }
        requestBuilder.post(requestBody);
        Request request = requestBuilder.build();
        long t1 = System.currentTimeMillis();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() != HTTP_CODE_SUCCESS) {
                logger.error("注册服务失败！，status:" + response.code());
                return;
            }

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    logger.error("注册服务，返回为空！，status:" + response.code());
                    return;
                }
                logger.info("注册Fdd Graphql Provider:{}, 结果：{}", provider, responseBody.string());
            }
        } catch (IOException e) {
            logger.error("调用失败：{},{}", e.getMessage(), request.toString(), e);
        } finally {
            logger.info("{}, 耗时：{}", urlBuilder, System.currentTimeMillis() - t1);
        }
    }

    private TpDocGraphqlProviderServiceInfo getTpDocGraphqlProviderServiceInfo(String commitId) {
        if (StringUtils.isEmpty(schemaModuleName)) {
            schemaModuleName = schemaGroupName;
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
        List<ProvidModelInfo> models = new ArrayList<>();

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(SchemaProvider.class);
        if (beans.isEmpty()) {
            return provider;
        }

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            ProvidModelInfo modelInfo = getProvidModelInfo(entry);
            if (modelInfo != null) {
                models.add(modelInfo);
            }
        }
        if (!CollectionUtils.isEmpty(models)) {
            provider.setModels(models);
        }

        return provider;
    }

    private ProvidModelInfo getProvidModelInfo(Map.Entry<String, Object> entry) {
        Object providerObj = entry.getValue();
        Class<?> controllerClass = providerObj.getClass();
        SchemaProvider schemaProvider = controllerClass.getAnnotation(SchemaProvider.class);
        ProvidModelInfo modelInfo = new ProvidModelInfo();
        modelInfo.setModelName(schemaProvider.clazz().getSimpleName());
        Set<String> refIds = new HashSet<>();
        Collections.addAll(refIds, schemaProvider.ids());
        modelInfo.setRefIds(refIds);

        Method[] methods = controllerClass.getDeclaredMethods();
        if (methods.length == 0) {
            return null;
        }
        String className = controllerClass.getName();
        if (BaseSchemaProviderController.class.isAssignableFrom(controllerClass)) {
            modelInfo.setIdProvider(className + ".getById");
            modelInfo.setIdsProvider(className + ".getByIds");
        } else {
            boolean hasIdApi = false;
            boolean hasIdsApi = false;
            for (Method method : methods) {
                if (method.isAnnotationPresent(IdProvider.class)) {
                    modelInfo.setIdProvider(className + "." + method.getName());
                    hasIdApi = true;
                } else if (method.isAnnotationPresent(IdsProvider.class)) {
                    modelInfo.setIdsProvider(className + "." + method.getName());
                    hasIdsApi = true;
                }
            }

            if (!hasIdApi && !hasIdsApi) {
                logger.warn("{}没有指定id查询接口！", className);
                return null;
            }
        }
        return modelInfo;
    }
}
