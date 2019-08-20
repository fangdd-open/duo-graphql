package com.fangdd.graphql.region;

import com.fangdd.graphql.provider.BaseDataFetcher;
import com.fangdd.graphql.provider.InnerProvider;
import com.fangdd.seed.RegionInitService;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

/**
 * @author xuwenzhen
 * @date 2019/5/21
 */
@Configuration
@ConditionalOnBean(RegionInitService.class)
public class RegionInnerProviderConfigure {
    private static final String STR_EXCLAMATION = "!";
    static final String MODULE_NAME = "REGION";
    private static final Set<String> REF_IDS = Sets.newHashSet("cityId", "districtId", "sectionId", "regionId");
    private static final String STR_AT = "@";
    private static final String STR_S = "s";

    @Autowired
    private RegionInitService regionInitService;

    @Bean
    public InnerProvider getInnerProvider() {
        RegionDataFetchers regionDataFetcher = new RegionDataFetchers(regionInitService);
        Map<String, BaseDataFetcher> refIdDataFetcherMap = Maps.newHashMap();

        REF_IDS.forEach(refId -> {
            refIdDataFetcherMap.put(STR_AT + refId, regionDataFetcher.getById(refId));
        });

        return new InnerProvider(MODULE_NAME.toLowerCase(), REF_IDS)
                .setRefIdDataFetcherMap(refIdDataFetcherMap)
                .setDataFetcher(MODULE_NAME, "byId", regionDataFetcher.getById(null))
                .setDataFetcher(MODULE_NAME, "byPinYin", regionDataFetcher.getByPinYin())
                .setDataFetcher(MODULE_NAME, "cityList", regionDataFetcher.cityList())
                .setDataFetcher(MODULE_NAME, "children", regionDataFetcher.getChildren())
                .setDataFetcher(MODULE_NAME, "searchRegion", regionDataFetcher.searchRegion())
                .build();
    }
}
