package com.fangdd.graphql.register.zk;

import com.fangdd.graphql.exceptions.GraphqlRegistryException;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.register.GraphqlRegister;
import com.fangdd.graphql.register.config.GraphqlRegisterConfigure;
import com.fangdd.graphql.register.config.GraphqlServerConfigure;
import com.fangdd.graphql.register.server.GraphqlEngineService;
import com.fangdd.graphql.register.utils.GzipUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * ZK实现的GraphQLRegister
 *
 * @author xuwenzhen
 * @date 2019/4/24
 */
@Component
public class GraphqlZookeeperRegister implements Watcher, GraphqlRegister<TpDocGraphqlProviderServiceInfo> {
    private static final Logger logger = LoggerFactory.getLogger(GraphqlZookeeperRegister.class);
    private static final String ZOOKEEPER = "zookeeper://";
    private static final int SESSION_TIMEOUT = 20000;
    private static final String DEFAULT_ROOT_PATH = "/graphql";
    private static final List<ACL> FDD_GRAPHQL_ACL = ZooDefs.Ids.OPEN_ACL_UNSAFE;
    private static final String HELLO_WORLD = "hello youyu-graphql!";
    private static final String PATH_SPLITTER = "/";
    private static final String APPS = "apps";
    private static final String EMPTY_STR = "";
    private static final String JSON_PREFIX = "{";

    private static String rootPath;
    private static String appsPath;

    private CountDownLatch connectedSemaphore;

    private ZooKeeper zk = null;

    @Autowired
    private GraphqlServerConfigure graphqlServerConfigure;

    @Autowired
    private GraphqlRegisterConfigure graphQLRegisterConfigure;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GraphqlEngineService<TpDocGraphqlProviderServiceInfo> graphqlEngineService;

    /**
     * 初始化zk client
     */
    @PostConstruct
    public void initZookeeperClient() {
        //连接
        connect();

        //初始化根路径名称
        initPath();

        //创建根目录
        pathCreateIfNotExists(rootPath, HELLO_WORLD, CreateMode.PERSISTENT, false);

        //创建app目录
        pathCreateIfNotExists(appsPath, EMPTY_STR, CreateMode.PERSISTENT, false);

        //获取path目录节点的配置数据，并注册默认的监听器
        watchAndLoadData();
    }

    /**
     * 向注册中心注册供应端服务的信息
     *
     * @param providerServiceInfo 供应端服务的信息数据
     */
    @Override
    public void register(TpDocGraphqlProviderServiceInfo providerServiceInfo) {
        String dataStr = providerServiceInfo.toString();

        //写入zk节点
        String appId = providerServiceInfo.getAppId();
        pathCreateIfNotExists(appsPath + PATH_SPLITTER + appId, dataStr, CreateMode.PERSISTENT, true);
    }

    /**
     * emit exists provider info list
     *
     * @param providerList exists provider info list
     */
    @Override
    public void emitProviderList(List<TpDocGraphqlProviderServiceInfo> providerList) {
        graphqlEngineService.emitProviderList(providerList);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (Event.KeeperState.SyncConnected != watchedEvent.getState()) {
            if (Event.KeeperState.Disconnected == watchedEvent.getState()) {
                //断开了，需要重新连接
                connect();
            } else if (Event.KeeperState.Expired == watchedEvent.getState()) {
                //过期了
                logger.warn("watchedEvent.getState()状态异常, watchedEvent.getState()={}", watchedEvent.getState());
            } else {
                logger.warn("watchedEvent.getState()状态异常, watchedEvent.getState()={}", watchedEvent.getState());
            }
            return;
        }
        String nodePath = watchedEvent.getPath();
        logger.info("已经触发了{}事件, nodePath={}", watchedEvent.getType(), nodePath);
        if (Watcher.Event.EventType.None == watchedEvent.getType() && null == nodePath) {
            connectedSemaphore.countDown();
            logger.info("connectedSemaphore.countDown();count={}", connectedSemaphore.getCount());
        } else {
            Stat stat = new Stat();
            String data = getNodeData(nodePath, stat, false);
            logger.info("节点变更,path={}, data={}", nodePath, data);
            if (nodePath.startsWith(appsPath)) {
                //注册服务节点
                TpDocGraphqlProviderServiceInfo providerServiceInfo = getTpDocGraphqlProviderServiceInfo(data);
                emitProviderList(Lists.newArrayList(providerServiceInfo));
            }
        }
    }

    /**
     * 销毁zk client
     */
    @PreDestroy
    public void destroy() {
        if (zk != null) {
            try {
                zk.close();
                logger.info("ZooKeeper关闭！");
            } catch (Exception e) {
                throw new GraphqlRegistryException("关闭ZooKeeper失败！", e);
            }
        }
    }

    private String getNodeData(String path, Stat stat, boolean watchNode) {
        try {
            byte[] data;
            if (watchNode) {
                data = zk.getData(path, this, stat);
            } else {
                data = zk.getData(path, false, stat);
            }
            return GzipUtils.decompress(data);
        } catch (Exception e) {
            throw new GraphqlRegistryException("读取节点数据失败，path=" + path, e);
        }
    }

    /**
     * 设置根节点名称
     */
    private void initPath() {
        String configedRootPath = graphQLRegisterConfigure.getRoot();
        if (Strings.isNullOrEmpty(configedRootPath)) {
            setPath(DEFAULT_ROOT_PATH);
            return;
        }
        if (!configedRootPath.startsWith(PATH_SPLITTER)) {
            setPath(PATH_SPLITTER + configedRootPath);
        } else {
            setPath(configedRootPath);
        }
    }

    private static void setPath(String rootPath) {
        GraphqlZookeeperRegister.rootPath = rootPath;
        GraphqlZookeeperRegister.appsPath = rootPath + PATH_SPLITTER + APPS;
    }

    private boolean pathExists(String path, boolean watch) {
        try {
            return zk.exists(path, watch) != null;
        } catch (Exception e) {
            throw new GraphqlRegistryException("查询路径是否存在失败，path=" + path, e);
        }
    }

    private String pathCreateIfNotExists(String path, String strData, CreateMode createMode, boolean watch) {
        byte[] bytes = GzipUtils.compress(strData);
        return pathCreateIfNotExists(path, bytes, createMode, watch);
    }

    private String pathCreateIfNotExists(String path, byte[] data, CreateMode createMode, boolean watch) {
        if (pathExists(path, watch)) {
            try {
                zk.setData(path, data, -1);
            } catch (Exception e) {
                throw new GraphqlRegistryException("写节点(path=" + path + ")数据失败, data=" + GzipUtils.decompress(data), e);
            }
            return path;
        }
        try {
            String result = zk.create(path, data, FDD_GRAPHQL_ACL, createMode);
            logger.info("创建节点：{}", result);
            return result;
        } catch (Exception e) {
            throw new GraphqlRegistryException("创建节点失败，path=" + path, e);
        }
    }

    private void watchAndLoadData() {
        Stat stat = new Stat();
        List<String> serviceNodeList;
        try {
            serviceNodeList = zk.getChildren(appsPath, this, stat);
        } catch (Exception e) {
            throw new GraphqlRegistryException("拉取服务节点列表失败！", e);
        }
        if (!CollectionUtils.isEmpty(serviceNodeList)) {
            List<TpDocGraphqlProviderServiceInfo> providerServiceInfoList = Lists.newArrayList();
            serviceNodeList.forEach(serviceNodePath -> {
                Stat serviceNodeStat = new Stat();
                String providerServicePath = appsPath + PATH_SPLITTER + serviceNodePath;
                String serviceData = getNodeData(providerServicePath, serviceNodeStat, true);

                if (!serviceData.startsWith(JSON_PREFIX)) {
                    //删除无效节点
                    deleteErrorAppNode(providerServicePath);
                }
                logger.info("加载节点：{}:{}", providerServicePath, serviceData);
                TpDocGraphqlProviderServiceInfo providerServiceInfo = getTpDocGraphqlProviderServiceInfo(serviceData);
                providerServiceInfoList.add(providerServiceInfo);
            });

            //注册现存的所有服务
            logger.info("emitter exists providers");
            emitProviderList(providerServiceInfoList);
        }
    }

    private TpDocGraphqlProviderServiceInfo getTpDocGraphqlProviderServiceInfo(String serviceData) {
        TpDocGraphqlProviderServiceInfo providerServiceInfo = null;
        try {
            providerServiceInfo = objectMapper.readValue(serviceData.getBytes(Charsets.UTF_8), TpDocGraphqlProviderServiceInfo.class);
        } catch (IOException e) {
            logger.error("反序列化失败！", e);
        }
        if (providerServiceInfo == null) {
            throw new GraphqlRegistryException("注册信息异常！");
        }
        if (StringUtils.isEmpty(providerServiceInfo.getServer())) {
            //如果没有指定服务时，使用全局的填写
            providerServiceInfo.setServer(graphqlServerConfigure.getUrl());
        }
        return providerServiceInfo;
    }

    private void deleteErrorAppNode(String providerServicePath) {
        try {
            zk.delete(providerServicePath, -1);
            logger.info("删除节点：{}", providerServicePath);
        } catch (Exception e) {
            logger.error("删除节点失败，path={}", providerServicePath, e);
        }
    }

    private void connect() {
        String address = graphQLRegisterConfigure.getAddress();
        String zkAddress = address.substring(ZOOKEEPER.length());
        connectedSemaphore = new CountDownLatch(1);
        try {
            zk = new ZooKeeper(zkAddress, SESSION_TIMEOUT, this);

            //等待zk连接成功的通知
            connectedSemaphore.await();
        } catch (Exception e) {
            throw new GraphqlRegistryException("连接ZooKeeper失败！", e);
        }
    }
}
