# Duo-GraphQL是如何工作的？

![](https://oss-public.fangdd.com/prod/static/FlOv2WwLt3_FrxZ7j1qy0LcRuQHS.jpg)

看上面这个图，分成四个部分，分别代码Duo-GraphQL的四个阶段

### 一、GraphQL-Provider构建

GraphQL-Provider是个传统的SpringMVC服务，外加一些Duo-GraphQL注解。在项目的构建阶段，依赖了[Duo-Doc项目](https://github.com/fangdd-open/duo-doc)，用于在项目构建期`mvn package`生成接口信息文件`api.json`（如果依赖了Duo-Doc）服务的，则不需要生成接口文件，详见Duo-Doc项目说明。

Duo-Doc与Maven整合，可以在CI中自动完成，你需要做的只是写好注释。



### 二、GraphQL-Engine启动

1. GraphQL-Engine启动时，会先向注册中心订阅provider目录的变更。
2. 拉取所有已经注册进来的GraphQL-Provider信息。
3. 根据GraphQL-Provider信息，拉取对应的接口信息（即api.json内容）。
4. 根据GraphQL-Provider信息与接口信息，构建Schema（详见《[构建Schema](./schema-build.md)》）。
5. 注册进在线Schema（可能会有多个，如果没指定，则注册为`default`）。



### 三、GraphQL-Provider启动

1. Spring-boot启动
2. Spring-boot启动成功后，会获取当前GraphQL-Provider信息：`com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo`
3. 向注册中心注册当前版本GraphQL-Provider
   1. 向Zookeeper注册中心注册
   2. 向Redis注册中心注册
4. 注册成功后，GraphQL-Engine会收到GraphQL-Provider变更通知，然后触发拉取GraphQL-Provider信息->拉取接口信息->重新构建Schema->应用，详见上面`GraphQL-Engine启动`部分



### 四、GraphQL查询

GraphQL-Engine收到客户端的`gql`，会通过请求信息找到对应的Schema，然后通过`graphql-java`引擎解析获取数据。

在解析获取数据的阶段GraphQL-Engine做了很多事，比如`gql`缓存、获取数据编排、请求合并...

对于`gql`里的每个查询字段都会对应一个DataFetcher，有些可能是PropertyDataFetcher，则直接从上下文的数据中找到对应的字段值返回，有的则是绑定了GraphQL-Provider接口的DataFetcherProxy，则会触发RESTful请求获取数据，并将此请求需要的字段通过selections传递给GraphQL-Provider。

1. 接口 & 接口参数

   DataFetcherProxy内包含了具体的某个API信息，API信息里定义了具体的请求方法、请求参数、请求路径等一系列参数。

   GraphQL-Engine会在当前查询上下文中自动组装出接口需要的所有参数、请求头。

   另外，GraphQL-Engine会根据当前`gql`需要查询的字段信息和子节点可能会用到的信息一起组装成`selections`参数，一并发送给GraphQL-Provider。

   

2. 请求头

   请求头包括两部分内容：接口定义的请求头 和 由GraphQL-Engine定义的需要透传的请求头

   其中接口定义的请求头会直接在当前查询的上下文环境数据中获取，而配置了需要透传的请求头，则直接从`gql`请求中直接透传下去。

   

### 五、GraphQL-Provider响应

GraphQL-Provider接收到RESTful请求后，进行处理。这跟普通的RESTful请求没太大区别。唯一不同的是，GraphQL-Provider需要处理参数`selections`，按需返回。

当然，如果你不理会`selections`参数，直接全量字段返回也没有什么问题，就是会浪费这一环节的带宽，不会影响到最终`gql`的返回。



