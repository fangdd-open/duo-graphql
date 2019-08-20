package com.fangdd.graphql.directive.factory;

import com.fangdd.graphql.core.exception.GraphqlBuildException;
import com.fangdd.graphql.directive.BaseGraphqlDirectiveFactory;
import com.google.common.collect.Lists;
import graphql.introspection.Introspection;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLNonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static graphql.Scalars.GraphQLInt;

/**
 * @author xuwenzhen
 * @date 2019/6/28
 */
@Service
public class SubListDirectiveFactory extends BaseGraphqlDirectiveFactory {
    private static final String DIRECTIVE_NAME = "subList";
    private static final String SKIP = "skip";
    private static final String SIZE = "size";

    @Override
    protected GraphQLDirective buildGraphQLDirective() {
        return GraphQLDirective.newDirective()
                .description("获取子列表，可对列表进行截取，超出索引会返回空列表，不会报错")
                .argument(GraphQLArgument.newArgument().name(SKIP).description("跳过多少条记录").defaultValue(0).type(GraphQLInt))
                .argument(GraphQLArgument.newArgument().name(SIZE).description("取多少条记录").type(GraphQLNonNull.nonNull(GraphQLInt)))
                .name(DIRECTIVE_NAME)
                .validLocations(Introspection.DirectiveLocation.FIELD)
                .build();
    }

    @Override
    public Object process(DataFetchingEnvironment environment, Object data, Map<String, Object> args) {
        if (data == null) {
            return null;
        }
        if (!List.class.isInstance(data)) {
            throw new GraphqlBuildException("指令[" + DIRECTIVE_NAME + "]暂不支持类型：" + data.getClass().getName());
        }

        return doProcess((List) data, args);
    }

    private List doProcess(List list, Map<String, Object> args) {
        int length = list.size();
        int skip = (int) args.getOrDefault(SKIP, 0);
        int size = (int) args.get(SIZE);
        if (length <= skip) {
            return Lists.newArrayList();
        }
        if (skip + size > length) {
            size = length - skip;
        }
        return list.subList(skip, skip + size);
    }
}
