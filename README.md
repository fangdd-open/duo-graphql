# Duo-GraphQL

## <span style="color: #FF9900">※ Duo-GraphQL介绍</span>

Duo-GraphQL基于[graphql-java](https://github.com/graphql-java/graphql-java)引擎实现，对Schema构建、DataFetch绑定、任务编排优化做了自动化实现，让开发者专注于领域功能开发，与当前主流的多团队的微服务敏捷开发模式无缝契合。

GraphQL是BFF的技术实现之一，BFF层相当大一部分风险和技术负担是：底层领域服务会在这里交汇。对于新人来说，要理解这么多领域、各领域的关系，是个不小的门槛（特别是对领域还划分还不太清晰的情况）。基于GraphQL的BFF实现，可以解耦这一错综复杂的关系网。使用GraphQL技术，各领域服务还可以专注在自己的领域数据内，而不需要太过关注其它领域的数据整合。



Duo-GraphQL实现了以下功能：

1. Schema、Resolver（DataFetcher）自动生成和绑定，不依靠人工。

2. 支持领域。各领域微服务（GraphQL Provider）以传统RESTful API项目的方式独立开发、维护、部署，多个领域服务共同组建一张大图，最大程度上保留传统微服务组织架构与开发模式。

3. 一个引擎支持多Schema。默认会有两个：业务Schema、当前GraphQL状态Schema。

4. 实现三大类DataProvider：`innerProvider` / remoteProvider / tpdocProvider，分别对应常用的固定少变的基础领域服务、复杂多变的业务领域服务和旧的RESTful领域服务接入。

5. 做了大量性能优化，任务编排，请求合并等工作，最大限度的提高性能。最新统计近7天的服务SLA:100%，响应95线:20.3ms，99.9线:65.3ms。

6. 自定义指令支持，只需要实现接口`com.fangdd.graphql.directive.BaseGraphqlDirectiveFactory`，不需要直接面对GraphQL规则。

7. 一个GraphQL Provider工程支持多个领域。本功能用于领域过小，还不需要单独一个项目时使用。另外支持向其它领域服务注入字段。比如在文章领域向用户领域注入userArticles字段等。

      

感谢graphql-java项目，写得真不错，得以在后续的扩展中得以实现



## <span style="color: #FF9900">※ Duo-GraphQL架构</span>

![Duo-GraphQL框架图](https://oss-public.fangdd.com/prod/static/FhIP4N6EHp4M4NE3VRzB-tzSIUvl.png)

（上面图片地址：https://oss-public.fangdd.com/prod/static/FhIP4N6EHp4M4NE3VRzB-tzSIUvl.png）



## <span style="color: #FF9900">※ Getting Start</span>

> 本项目是在`spring boot`上开发的，引擎和`Provider`都建议使用`spring boot 2.x`版本

至少需要两部分服务：GraphQL Engine和GraphQL Provider。搭建文档详见下面两个链接：

GraphQL Engine：《[GraphQL-Engine-Getting-Start](./doc/GraphQL-Engine-Getting-Start.md)》

GraphQL Provider：《[GraphQL-Provider-Getting-Start](./doc/GraphQL-Provider-Getting-Start.md)》



以上getting start里的代码，请查看当前项目的目录 [/demo](./demo)





## <span style="color: #FF9900">※ 文档中心</span>

### 一、引擎端: GraphQL-Engine

1. [请求头透传](./doc/header.md)
2. [添加监控](./doc/monitor.md)
3. [自定义请求上下文](./doc/execute-context.md)
4. [自定义指令: Directive](./doc/directive.md)
5. [订阅: Subscription](./doc/subscription.md)
6. [ScalarJson](./doc/scalar-json.md)
7. [分布式部署](./doc/distributed-deploy.md)
8. Schema构建
9. 异常处理
10. GraphLQ Provider
    1. 远端数据供应端: RemoteProvider
    2. 内部数据供应端: InnerProvider
    3. 旧RESTful接口接入
11. 合并请求
12. 多GraphQL Schema实现
13. GraphQL Engine调试
14. 注册中心
    1. 使用Zookeeper注册中心
    2. 使用Redis注册中心
15. 依赖Duo-Doc服务
16. 权限控制
17. gql查询缓存
18. 开发期间设置GraphQL Provider请求地址
19. Redis配置
20. Mock & GraphiQL
21. Mesh Service支持
22. SkyWalking探针



### 二、数据供应端: GraphQL-Provider

1. 视图
   1. 基础视图
   2. 非基础视图
2. [选择字段: Selection](./doc/selection.md)
3. GraphQL操作实现
   1. 实现Query
   2. 实现Mutation
   3. 实现Subscription
4. 批量接口
5. 一个GraphQL Provider提供多个领域服务
6. 向其它领域图（Type）注入字段
7. 字段映射
8. 生成api.json
9. GraphQL Provider服务调试



