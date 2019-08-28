package com.fangdd.graphql.provider;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuwenzhen
 * @date 2019/8/26
 * @deprecated 已经切换到引擎中实现
 */
public class RefIdsListUtils {
    public static <T, D> List<T> getResortedRefs(String ids, Class<D> idClass, Function<Set<D>, List<T>> getByIdsFunction, Function<T, D> getEntityIdSupplier) {
        Set<D> agentIds = new HashSet();
        List<D> agentIdList = new ArrayList<>();
        String[] idArray = ids.split(",");
        SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();
        for (String idStr : idArray) {
            if (StringUtils.isEmpty(idStr)) {
                agentIdList.add(null);
            } else {
                D id = toId(idStr, idClass, simpleTypeConverter);
                agentIds.add(id);
                agentIdList.add(id);
            }
        }

        List<T> agents = getByIdsFunction.apply(agentIds);
        if (CollectionUtils.isEmpty(agents)) {
            return emptyList(agentIdList.size());
        } else {
            Map<D, T> agentMap = agents.stream().collect(Collectors.toMap(getEntityIdSupplier::apply, obj -> obj));
            List<T> agentList = new ArrayList<>(agentIdList.size());
            agentIdList.forEach(id -> agentList.add(agentMap.get(id)));
            return agentList;
        }
    }

    private static <D> D toId(String idStr, Class<D> idClass, SimpleTypeConverter simpleTypeConverter) {
        return simpleTypeConverter.convertIfNecessary(idStr, idClass);
    }


    private static <T> List<T> emptyList(int size) {
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(null);
        }
        return list;
    }
}
