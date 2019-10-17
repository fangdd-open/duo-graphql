# Duo-GraphQL

## <span style="color: #FF9900">※ Duo-GraphQL介绍</span>

Duo-GraphQL基于[graphql-java](https://github.com/graphql-java/graphql-java)的扩展，用于支持当前主流的多团队微服务敏捷开发模式。它主要实现了以下功能：

1. Schema、Resolver（DataFetcher）自动生成绑定，不依靠人工。

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

>  本项目是在`spring boot`上开发的，引擎和`Provider`都建议使用`spring boot 2.x`版本

至少需要两部分服务：GraphQL Engine和GraphQL Provider。搭建文档详见下面两个链接：

GraphQL Engine：《[GraphQL-Engine-Getting-Start](./doc/GraphQL-Engine-Getting-Start.md)》

GraphQL Provider：《[GraphQL-Provider-Getting-Start](./doc/GraphQL-Provider-Getting-Start.md)》



以上getting start里的代码，请查看当前项目的目录 [/demo](./demo)





## <span style="color: #FF9900">※ 文档中心</span>

### 一、引擎端: GraphQL-Engine

1. 请求头透传
2. 添加监控
3. 自定义请求上下文
4. 自定义指令: Directive
5. 订阅: Subscription
6. ScalarJson
7. Redis订阅
8. 分布式部署
9. Schema构建
10. 异常处理
11. GraphLQ Provider
    1. 远端数据供应端: RemoteProvider
    2. 内部数据供应端: InnerProvider
    3. 旧RESTful接口接入
12. 合并请求
13. 多GraphQL Schema实现
14. GraphQL Engine调试
15. 注册中心
    1. 使用Zookeeper注册中心
    2. 使用Redis注册中心
16. 依赖Duo-Doc服务
17. 权限控制
18. gql查询缓存
19. 开发期间设置GraphQL Provider请求地址
20. Redis配置
21. Mock & GraphiQL
22. Mesh Service支持
23. SkyWalking探针



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



