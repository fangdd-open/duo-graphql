package com.fangdd.graphql.region;

import com.fangdd.graphql.provider.BaseDataFetcher;

/**
 * Region DataFetcher的基类
 *
 * @author xuwenzhen
 */
public abstract class BaseRegionDataFetcher extends BaseDataFetcher {
    /**
     * 获取领域名称
     *
     * @return 领域名称
     */
    @Override
    public String getModuleName() {
        return RegionInnerProviderConfigure.MODULE_NAME.toUpperCase();
    }
}
