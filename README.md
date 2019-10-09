# Duo-GraphQL

Duo-GraphQL基于[graphql-java](https://github.com/graphql-java/graphql-java)的扩展，用于支持当前主流的多团队微服务敏捷开发模式。它主要实现了以下功能：

1. Schema、Resolver（DataFetcher）自动生成绑定，不依靠人工。

2. 支持领域。各领域微服务（GraphQL Provider）以传统RESTful API项目的方式独立开发、维护、部署，多个领域服务共同组建一张大图，最大程度上保留传统微服务组织架构与开发模式。

3. 一个引擎支持多Schema。默认会有两个：业务Schema、当前GraphQL状态Schema。

4. 实现三大类DataProvider：innerProvider / remoteProvider / tpdocProvider，分别对应常用的固定少变的基础领域服务、复杂多变的业务领域服务和旧的RESTful领域服务接入。

5. 做了大量性能优化，任务编排，请求合并等工作，最大限度的提高性能。最新统计近7天的服务SLA:100%，响应95线:20.3ms，99.9线:65.3ms。

6. 自定义指令支持，只需要实现接口`com.fangdd.graphql.directive.BaseGraphqlDirectiveFactory`，不需要直接面对GraphQL规则。

    

感谢graphql-java项目，写得真不错，得以在后续的扩展中得以实现



- ### Getting Started

本项目是在spring boot上开发的，引擎和Provider都建议使用spring boot 2.x版本。

<img src="https://oss-public.fangdd.com/prod/static/FsaLdNehBzL3Q-0EmU95mz5gvmUc.jpg" title="Duo-GraphQL框架图"/>

至少需要两部分服务：GraphQL Engine和GraphQL Provider。搭建文档详见下面两个链接：

GraphQL Engine：《[GraphQL-Engine-Getting-Start](./doc/GraphQL-Engine-Getting-Start.md)》

GraphQL Provider：《[GraphQL-Provider-Getting-Start](./doc/GraphQL-Provider-Getting-Start.md)》



- ### 
