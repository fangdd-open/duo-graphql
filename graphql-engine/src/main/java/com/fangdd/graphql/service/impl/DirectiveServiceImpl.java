package com.fangdd.graphql.service.impl;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.exception.GraphqlInvocationException;
import com.fangdd.graphql.directive.BaseGraphqlDirectiveFactory;
import com.fangdd.graphql.service.DirectiveService;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import graphql.language.*;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xuwenzhen
 * @date 2019/7/15
 */
@Service
public class DirectiveServiceImpl implements DirectiveService {
    private static final Logger logger = LoggerFactory.getLogger(DirectiveServiceImpl.class);

    @Autowired
    private List<BaseGraphqlDirectiveFactory> graphqlDirectiveList;

    private Set<GraphQLDirective> directiveSet;

    private Map<String, BaseGraphqlDirectiveFactory> graphqlDirectiveMap;

    /**
     * 获取所有已注册的指令
     *
     * @return 指令
     */
    @Override
    public Set<GraphQLDirective> getDirectiveSet() {
        init();
        return directiveSet;
    }

    @Override
    public void processDirective(DataFetchingEnvironment environment, Object data, Map<String, List<Directive>> fieldDirectiveMap) {
        if (data == null) {
            return;
        }

        fieldDirectiveMap.entrySet().forEach(fieldEntry -> {
            String path = fieldEntry.getKey();
            List<Directive> directives = fieldEntry.getValue();
            directives.forEach(directive -> processFieldDirective(environment, path, data, directive));
        });
    }

    private void processFieldDirective(DataFetchingEnvironment environment, String path, Object data, Directive directive) {
        String directiveName = directive.getName();
        init();
        BaseGraphqlDirectiveFactory graphqlDirective = graphqlDirectiveMap.get(directiveName);
        if (graphqlDirective == null) {
            logger.warn("{}指令@{}，暂未实现！", path, directiveName);
            return;
        }

        //获取参数
        Map<String, Object> argMap = Maps.newHashMap();
        List<Argument> args = directive.getArguments();
        if (args != null) {
            args.forEach(argument -> argMap.put(argument.getName(), getArgumentValue(argument.getValue())));
        }
        getPathValue(environment, path, data, graphqlDirective, argMap);
    }

    private Object getPathValue(DataFetchingEnvironment environment, String path, Object data, BaseGraphqlDirectiveFactory directive, Map<String, Object> argMap) {
        if (List.class.isInstance(data)) {
            List listData = (List) data;
            for (int i = 0; i < listData.size(); i++) {
                Object item = listData.get(i);
                Object pathValue;
                pathValue = getPathValue(environment, path, item, directive, argMap);
                listData.set(i, pathValue);
            }
            return listData;
        } else if (Map.class.isInstance(data)) {
            int index = path.indexOf(GraphqlConsts.CHAR_DOT);
            String currentPath;
            Map mapData = (Map) data;
            if (index == -1) {
                Object pathValue = mapData.get(path);
                pathValue = directive.process(environment, pathValue, argMap);
                mapData.put(path, pathValue);
            } else {
                currentPath = path.substring(0, index);
                String nextPath = path.substring(index + 1);
                Object currentData = mapData.get(currentPath);
                if (currentData != null) {
                    getPathValue(environment, nextPath, currentData, directive, argMap);
                }
            }
            return mapData;
        } else {
            throw new GraphqlInvocationException("无法解决路径:" + path + "的值：" + data);
        }
    }

    private Object getArgumentValue(Value value) {
        if (IntValue.class.isInstance(value)) {
            return ((IntValue) value).getValue().intValue();
        } else if (BooleanValue.class.isInstance(value)) {
            return ((BooleanValue) value).isValue();
        } else if (FloatValue.class.isInstance(value)) {
            return ((FloatValue) value).getValue().floatValue();
        } else if (StringValue.class.isInstance(value)) {
            return ((StringValue) value).getValue();
        } else if (NullValue.class.isInstance(value)) {
            return null;
        } else {
            throw new GraphqlInvocationException("暂不支持指令参数类型，value=" + value.toString());
        }
    }

    private synchronized void init() {
        if (graphqlDirectiveMap != null) {
            return;
        }
        graphqlDirectiveMap = Maps.newHashMap();
        directiveSet = Sets.newHashSet();
        if (CollectionUtils.isEmpty(graphqlDirectiveList)) {
            return;
        }
        graphqlDirectiveList.forEach(directiveFactory -> {
            GraphQLDirective directive = directiveFactory.getGraphQLDirective();
            graphqlDirectiveMap.put(directive.getName(), directiveFactory);
            directiveSet.add(directive);
        });
    }
}
