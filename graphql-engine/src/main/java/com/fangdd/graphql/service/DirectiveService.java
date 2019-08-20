package com.fangdd.graphql.service;

import graphql.language.Directive;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 指令服务
 *
 * @author xuwenzhen
 * @date 2019/7/15
 */
public interface DirectiveService {
    /**
     * 获取所有已注册的指令
     *
     * @return 指令
     */
    Set<GraphQLDirective> getDirectiveSet();

    /**
     * 处理返回结果里的指令
     *
     * @param environment       某个查询的上下文环境
     * @param data              查询响应结果
     * @param fieldDirectiveMap 当前查询字段定义的指令
     */
    void processDirective(DataFetchingEnvironment environment, Object data, Map<String, List<Directive>> fieldDirectiveMap);
}
