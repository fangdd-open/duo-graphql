package com.fangdd.graphql.region;

import com.fangdd.dp.geo.entity.dto.RegionNode;
import com.fangdd.graphql.provider.BaseDataFetcher;
import com.fangdd.graphql.provider.ValueUtils;
import com.fangdd.seed.RegionInitService;
import com.google.common.collect.Lists;
import graphql.schema.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author xuwenzhen
 * @date 2019/5/21
 */
public class RegionDataFetchers {
    private static final String ID = "id";
    private static final String PIN_YIN = "pinYin";
    private static final String FILTER = "filter";
    private static final String COUNT = "count";
    private static final String BIZ = "biz";
    private static final String XF = "xf";
    private static final String ESF = "esf";
    private static final String FJ = "fj";
    private static final String CJ = "cj";
    private static final String JJR = "jjr";
    private static final String JR = "jr";
    private static final String ZF = "zf";
    private static final String XQ = "xq";
    private static final String NET = "net";
    private static final String HOT = "hot";
    private static final int HOT_CITY_MIN_SCORE = 300;
    private static final String TEST_CITY = "测试";
    private static final Integer CITY_BAISHAN = 10204;
    private static final String CITY_ID = "cityId";
    private static final String KEYWORD = "keyword";
    private static final String REGION_REGION = "region_Region";
    private RegionInitService regionInitService;

    public RegionDataFetchers(RegionInitService regionInitService) {
        this.regionInitService = regionInitService;
    }

    public BaseRegionDataFetcher getById(final String dependencyId) {
        return new BaseRegionDataFetcher() {
            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {
                Integer id = ValueUtils.getParamValue(environment, environment.getSource(), null, dependencyId == null ? ID : dependencyId);
                return regionInitService.get(id);
            }

            @Override
            public GraphQLOutputType getResponseGraphqlType() {
                return GraphQLTypeReference.typeRef(REGION_REGION);
            }

            @Override
            public List<String> getDependencyFields() {
                return Lists.newArrayList(dependencyId);
            }
        };
    }

    public BaseRegionDataFetcher getByPinYin() {
        return new BaseRegionDataFetcher() {
            @Override
            public RegionNode get(DataFetchingEnvironment environment) throws Exception {
                String pinYin = environment.getArgument(PIN_YIN);
                return regionInitService.getByPinYin(pinYin);
            }

            @Override
            public GraphQLOutputType getResponseGraphqlType() {
                return GraphQLTypeReference.typeRef(REGION_REGION);
            }
        };
    }

    public BaseRegionDataFetcher cityList() {
        return new BaseRegionDataFetcher() {
            @Override
            public List<RegionNode> get(DataFetchingEnvironment environment) throws Exception {
                Map<String, Object> arguments = environment.getArgument(FILTER);

                Integer count = arguments == null ? null : (Integer) arguments.get(COUNT);
                String biz = arguments == null ? null : (String) arguments.get(BIZ);
                boolean hot = (arguments != null && arguments.containsKey(HOT)) && (boolean) arguments.get(HOT);
                List<RegionNode> regionNodeList = Lists.newArrayList();

                if (count == null) {
                    count = Integer.MAX_VALUE;
                }
                Integer finalCount = count;
                regionInitService.getCityList().forEach(city -> {
                    if (regionNodeList.size() >= finalCount) {
                        return;
                    }
                    if (hot && city.getScore() < HOT_CITY_MIN_SCORE) {
                        return;
                    }
                    if (matchBiz(city, biz)) {
                        regionNodeList.add(city);
                    }
                });

                return regionNodeList;
            }

            @Override
            public GraphQLOutputType getResponseGraphqlType() {
                return GraphQLList.list(GraphQLTypeReference.typeRef(REGION_REGION));
            }
        };
    }

    public BaseRegionDataFetcher getChildren() {
        return new BaseRegionDataFetcher() {
            @Override
            public List<RegionNode> get(DataFetchingEnvironment environment) throws Exception {
                Integer parentId = environment.getArgument(ID);
                RegionNode regionNode = regionInitService.get(parentId);
                if (regionNode == null) {
                    return null;
                } else {
                    return regionNode.getChildren();
                }
            }

            @Override
            public GraphQLOutputType getResponseGraphqlType() {
                return GraphQLList.list(GraphQLTypeReference.typeRef(REGION_REGION));
            }
        };
    }

    public DataFetcher<List<RegionNode>> searchRegion() {
        return new BaseRegionDataFetcher() {
            @Override
            public List<RegionNode> get(DataFetchingEnvironment environment) throws Exception {
                Integer cityId = environment.getArgument(CITY_ID);
                String keyword = environment.getArgument(KEYWORD);
                RegionNode city = regionInitService.getCity(cityId);
                if (city == null || StringUtils.isEmpty(keyword)) {
                    return null;
                }
                List<RegionNode> children = Lists.newArrayList();
                searchRegionNodeName(keyword, city, children);

                return children;
            }

            @Override
            public GraphQLOutputType getResponseGraphqlType() {
                return GraphQLList.list(GraphQLTypeReference.typeRef(REGION_REGION));
            }
        };
    }

    private void searchRegionNodeName(String keyword, RegionNode regionNode, List<RegionNode> children) {
        if (!CollectionUtils.isEmpty(regionNode.getChildren())) {
            regionNode.getChildren()
                    .forEach(child -> {
                        if (child.getName().contains(keyword)) {
                            //命中区域
                            children.add(child);
                        }
                        //搜索下级
                        searchRegionNodeName(keyword, child, children);
                    });
        }
    }

    private boolean matchBiz(RegionNode city, String biz) {
        if (CITY_BAISHAN.equals(city.getId())) {
            return false;
        }
        if (city.getName().contains(TEST_CITY)) {
            return false;
        }
        if (StringUtils.isEmpty(biz)) {
            return true;
        }
        boolean xf = city.getXf() != null && city.getXf() == 1;
        boolean esf = city.getEsf() != null && city.getEsf() == 1;
        boolean xq = city.getXq() != null && city.getXq() == 1;

        switch (biz) {
            case XF:
                return xf;
            case ESF:
                return esf;
            case FJ:
                return city.getFj() != null && city.getFj() == 1;
            case CJ:
                return city.getTransaction() != null && city.getTransaction() == 1;
            case JJR:
                return city.getJjr() != null && city.getJjr() == 1;
            case JR:
                return city.getJr() != null && city.getJr() == 1;
            case ZF:
                return city.getZf() != null && city.getZf() == 1;
            case XQ:
                return xq;
            case NET:
                return xf || esf || xq;
            default:
                return false;
        }
    }
}
