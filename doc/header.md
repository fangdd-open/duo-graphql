# 请求头透传

GraphQL-Engine只负责通用功能的实现不负责具体业务的实现。因此，有些业务功能，比如用户权限校验，一般通过请求头的token或session-id等进行验证。为了实现类似的功能，GraphQL-Engine将请求头的信息向下透传给GraphQL-Provider服务，由GraphQL-Provier服务去处理。

透传header只需要在GraphQL-Engine服务的配置文件里添加：

```properties
## 需要透传到各服务的请求头，下面的请求头，会透传给各GraphQL Provider
## 引擎本身不做鉴权工作，而是交给各GraphQL-Provider实现，多个使用半角逗号分隔
graphql.query.headers = user-id,client,trace-id,token
```

