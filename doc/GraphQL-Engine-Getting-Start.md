# Duo-GraphQL 引擎搭建

Duo-GraphQL引擎基于Spring Boot，建议使用2.x版本。本文档以maven为例，使用grandle的请自行转换。



## 一、定义一些变量（可选）

```xml
<properties>
  <java.version>1.8</java.version>
  <encoding>UTF-8</encoding>
  <compiler.version>1.8</compiler.version>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <maven-compiler.version>3.8.0</maven-compiler.version>
  <guava.version>26.0-jre</guava.version>
  <junit.version>4.12</junit.version>
  <slf4j.version>1.7.25</slf4j.version>
  <logback.version>1.2.3</logback.version>
  <spring-boot.version>2.1.3.RELEASE</spring-boot.version>
  <spring.version>5.1.5.RELEASE</spring.version>
  <graphql-java.version>13.0</graphql-java.version>
  <jedis.version>2.9.0</jedis.version>
  <duo-graphql.version>1.4.1-SNAPSHOT</duo-graphql.version>
  <jackson-databind.version>2.9.9.2</jackson-databind.version>
</properties>
```





## 二、引入Spring Boot

有两种方式：

### 以项目parent方式接入

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.3.RELEASE</version>
</parent>
```



### 如果项目已经有parent，则可以以import方式接入

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <!-- Import dependency management from Spring Boot -->
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```



## 二、引入基础包

```xml
<!-- restful服务必须 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <!-- 2.1.3.RELEASE版本的spring boot自带jackson有漏洞，需要升级 -->
  <exclusions>
    <exclusion>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
    </exclusion>
  </exclusions>
</dependency>

<dependency>
  <groupId>com.fangdd.graphql</groupId>
  <artifactId>graphql-engine</artifactId>
  <!-- duo-graphql版本，需要在properties中定义 -->
  <version>${duo-graphql.version}</version>
</dependency>

<!-- 使用Redis注册中心，也可以选择ZK注册中心 -->
<dependency>
  <groupId>com.fangdd.graphql</groupId>
  <artifactId>graphql-register-redis</artifactId>
  <version>${duo-graphql.version}</version>
</dependency>

<!-- 本Spring boot自带版本存在严重安全漏洞，需要使用2.9.9.2及以上版本 -->
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>${jackson-databind.version}</version>
</dependency>

<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <version>${guava.version}</version>
</dependency>
```



## 三、创建启动类

```java
@EnableWebMvc
@SpringBootApplication(scanBasePackages = "com.fangdd") //Spring启动时需要扫描的包名
public class GraphqlEngineApplication {
    public static void main(String[] args) {
        new SpringApplication(GraphqlEngineApplication.class).run(args);
    }
}
```



## 四、配置

创建`application.properties`文件，并添加以下配置

``` bash
#服务端口号
server.port = 12345

#Graphql服务接口名称，即是graphql请求入口：http://127.0.0.1:12345/graphql
graphql.url = graphql

#全局的provider调用地址，如果GraphQL Provider没有指定自己的地址时，会使用当前地址，比如在使用Mesh Service时可使用
#fdd.graphql.server.url = http://web-mesh-gw

#注册中心默认路径，如果是使用redis时，即为redis的keys前缀
fdd.graphql.register.root = graphql-dev

## 需要透传到各服务的请求头，下面的请求头，会透传给各GraphQL Provider
## 引擎本身不做鉴权工作，而是交给各Provider实现，多个使用半角逗号分隔
graphql.query.headers = user-id,client,trace-id

## 指定各GraphQL Provider数据提供端的服务地址（如果未指定，则使用各服务里配置的地址）
## 本配置主要用于多个GraphQL Provider场景，在本地开发时，可以只启动你正在开发或调试的服务
## 其它GraphQL Provider服务可以指向开发服务器或测试服务器
#graphql.provider.providerService[user.graphql.cp.fdd]=http://127.0.0.1:12347
#graphql.provider.providerService[article.graphql.cp.fdd]=http://127.0.0.1:12348

## 多个Schema时，指定url路由与schema的关系，GraphQL Provider未指定时，默认是注册到default
graphql.provider.urlSchemaMap[/graphql]=default


## redis配置
## redis配置 --哨兵模式配置
#spring.redis.host=sentinel01.redis.ip.fdd:26379;sentinel02.redis.ip.fdd:26379;sentinel03.redis.ip.fdd:26379
#spring.redis.type=sentinel
#spring.redis.master-name=redis.ip.fdd
 
 
## redis配置 --集群模式配置
#spring.redis.host=redis1.cp.fdd:6380;redis2.cp.fdd:6380;redis3.cp.fdd:6380;redis1.cp.fdd:6379;redis2.cp.fdd:6379;redis3.cp.fdd:6379
#spring.redis.type=cluster
 
 
## redis配置 --单机模式配置
spring.redis.host=127.0.0.1:6379
spring.redis.type=standalone
spring.redis.password=123456
 
##Redis通用配置##
#连接超时，单位：毫秒
spring.redis.connectTimeout=2000
 
#读取超时，单位：毫秒
spring.redis.readTimeout=2000
```



## 五、logback.xml

```xml
<!-- 这里只是演示的配置，直接输出到控制台，生产环境不要用这配置！！ -->
<configuration scan="true" scanPeriod="2 seconds">
    <appender name="STDOUT"
              class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{yyyy-MM-dd HH:mm:ss.SSS} %X{_timestamp_}  [%thread] %-5level %logger{36} %msg%n
            </pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```



目前为止，引擎需要的所有工作都已经完成了，可以尝试启动。如果没有接入GraphQL Provider时，转变Schema会是：

```graphql
type Query {
  hello: String
}
```

