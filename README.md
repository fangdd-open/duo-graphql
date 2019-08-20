# Duo GraphQL

本项目是基于graphql-java项目，对原支持单一服务进行扩展，实现以下功能：

1. 抽象出领域的概念。各领域由独立的RestFul服务提供，并向全局注册唯一的"外键"，只需要任一地方出现此"外键"，即可将其实体绑定上去

2. 实现两大类DataProvider：innerProvider / remoteProvider，分别对应常用的、固定少变的领域服务和复杂多变的领域服务

3. 支持将现有已接入TP-DOC的RestFul接进来（响应以Scalar返回）

4. 优化了DataFetcher，对可合并的请求进行了异步合并处理，最大限度的提高性能

5. 支持多schema

6. 自定义指令支持，实现接口`com.fangdd.graphql.directive.BaseGraphqlDirectiveFactory`快速支持

   

感谢graphql-java项目，写得真不错，得以在后续的扩展中得以实现



### Getting Started





### Directive



### selection

每个查询，都需要指定响应字段，响应字段会以参数sections传递给Provider
比如有段筛选：

```
query {
    houseById(id: 1234) {
        name,
        albums {
            cate
            url
        }
    }
}
```

引擎会生成参数：`selections=name!albums!.cate!.url`
可以把!理解成换行，.理解成tab就是这样：

```
name
albums
    cate
    url
```