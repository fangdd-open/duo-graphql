# 自定义指令：Directive

Duo-GraphQL中实现自定义指令只需要实现接口：`com.fangdd.graphql.directive.BaseGraphqlDirectiveFactory`

以`com.fangdd.graphql.directive.factory.SubListDirectiveFactory`为例，包含了两部分内容：指令定义和指令执行。

```java
/**
 * 获取列表中指定起始位置、指定大小的子集
 *
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
        return GraphQLDirective.newDirective() //使用Directive Builder来创建
                .description("获取子列表，可对列表进行截取，超出索引会返回空列表，不会报错") //指令描述
                .argument(
                        GraphQLArgument.newArgument() //创建参数的Builder
                                .name(SKIP)    // 参数名称
                                .description("跳过多少条记录") //参数描述
                                .defaultValue(0) //参数默认值
                                .type(GraphQLInt) //参数类型
                ) // 定义第一个参数
                .argument(
                        GraphQLArgument.newArgument()
                                .name(SIZE)
                                .description("取多少条记录")
                                .type(GraphQLNonNull.nonNull(GraphQLInt))
                ) // 定义第二个参数
                .name(DIRECTIVE_NAME) // 指令名称，比如当前指令：@subList
                .validLocations(Introspection.DirectiveLocation.FIELD) //指令在可用在哪个位置，当前为字段里面
                .build();
    }

    /**
     * 执行指令
     *
     * @param environment 当前执行的上下文环境
     * @param data        当前值
     * @param args        指令参数
     * @return 通过指令计算之后的值
     */
    @Override
    public Object process(
      DataFetchingEnvironment environment, 
      Object data, 
      Map<String, Object> args
    ) {
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
```



