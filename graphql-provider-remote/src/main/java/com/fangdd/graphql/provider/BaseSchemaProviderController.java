package com.fangdd.graphql.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 标准视图接口，仅作参考，不用继承
 *
 * @author xuwenzhen
 * @date 2019/5/13
 */
public interface BaseSchemaProviderController<T, D> {
    /**
     * 通过ID查询基本视图
     *
     * @param id id
     * @return
     */
    T getById(D id);

    /**
     * 通过IDs批量查询基本视图，返回不需要按顺序
     *
     * @param ids id串，使用半角逗号分隔
     * @return
     */
    List<T> getByIds(String ids);
}
