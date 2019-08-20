package com.fangdd.graphql.pipeline;

/**
 * 管道
 *
 * @author xuwenzhen
 * @date 2019/6/3
 */
public interface Pipeline {
    /**
     * 处理
     *
     * @param registryState 当前注册信息
     */
    void doPipeline(RegistryState registryState);

    /**
     * 执行的顺序
     *
     * @return 数值，越小越前
     */
    int order();
}
