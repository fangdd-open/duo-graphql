# ScalarJson

Scalar是GraphQL自定义类型实现，GraphQL允许自定义类型，以实现用户的自定义输出。最经典的就是日志输出。

ScalarJson是Duo-GraphQL实现的一Json类型输出，本类型的数据，会以Json字符串的方式直接输出。适合一些不需要再拆解或循环嵌套的结构（比如树型结构）

在Duo-GraphQL中实现ScalarJson很简单，只需要在需要返回ScalarJson类型的接口上添加注解：`@com.fangdd.graphql.provider.annotation.GraphqlJson`，DEMO:

```java
    /**
     * 拉取所有的文章分类树
     *
     * @return 所有根节点的文章分类
     */
    @GraphqlJson
    @GetMapping("/tree")
    public List<ArticleCategorryNode> articleCategoryTree(ArticleCateQuery query) {
        return articleCategoryService.tree(query);
    }
```

