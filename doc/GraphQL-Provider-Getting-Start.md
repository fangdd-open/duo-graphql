# GraphQL Provider搭建

Duo-GraphQL引擎基于Spring Boot，建议使用2.x版本。本文档以maven为例，使用grandle的请自行转换。



## 一、定义一些变量（可选）

```xml
<properties>
  <java.version>1.8</java.version>
  <compiler.version>1.8</compiler.version>
  <encoding>UTF-8</encoding>
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
  <duo-graphql.version>1.4.4</duo-graphql.version>
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



## 三、引入基本依赖

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <exclusions>
    <exclusion>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
    </exclusion>
  </exclusions>
</dependency>
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

<dependency>
  <groupId>com.fangdd.graphql</groupId>
  <artifactId>graphql-provider-remote-redis</artifactId>
  <version>${duo-graphql.version}</version>
</dependency>

```



## 三、添加Duo-Doc api.json生成

```xml
<build>
  <finalName>${project.artifactId}</finalName>

  <plugins>
    <!-- 用于读取git信息 -->
    <plugin>
      <groupId>pl.project13.maven</groupId>
      <artifactId>git-commit-id-plugin</artifactId>
      <version>2.2.4</version>
      <executions>
        <execution>
          <id>get-the-git-infos</id>
          <goals>
            <goal>revision</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <dotGitDirectory>${project.basedir}/.git</dotGitDirectory>
        <prefix>git</prefix>
        <verbose>false</verbose>
        <generateGitPropertiesFile>true</generateGitPropertiesFile>
        <generateGitPropertiesFilename>${project.build.outputDirectory}/git.properties
        </generateGitPropertiesFilename>
        <format>json</format>
        <gitDescribe>
          <skip>false</skip>
          <always>false</always>
          <dirty>-dirty</dirty>
        </gitDescribe>
      </configuration>
    </plugin>

    <!-- 重要！Duo-Doc，通过解析源码、注释自动生成接口信息 -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-javadoc-plugin</artifactId>
      <version>2.10.3</version>
      <configuration>
        <doclet>com.fangdd.tp.doclet.TpDoclet</doclet>
        <docletArtifact>
          <groupId>com.fangdd</groupId>
          <artifactId>doclet</artifactId>
          <version>1.0.0</version>
        </docletArtifact>
        <sourcepath>
          <!-- 指定源码路径，如果多个模块，需要包含进去 -->
          ${project.basedir}/src/main/java
        </sourcepath>
        <useStandardDocletOptions>false</useStandardDocletOptions>

        <additionalJOptions>
          <additionalJOption>-J-Dbasedir=${project.basedir}</additionalJOption>
          <!-- FDD Provider必须添加commitId，否则无法确定版本 -->
          <additionalJOption>-J-DcommitId=${git.commit.id}</additionalJOption>
          <!-- appID，指定了appId后，会替换成当前文档的名称 -->
          <additionalJOption>-J-DappId=${docker.project.id}</additionalJOption>
          <additionalJOption>-J-Dexporter=graphql</additionalJOption>
          <additionalJOption>-J-DoutputDirectory=${project.build.outputDirectory}</additionalJOption>
        </additionalJOptions>
      </configuration>
      <executions>
        <execution>
          <id>attach-javadocs</id>
          <!-- package可以在提交代码后由CI自动触发，如果不需要自动触发，可以设置为site，届时需要手工执行：mvn clean site -->
          <phase>compile</phase>
          <goals>
            <goal>javadoc</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>2.0.2</version>
      <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <encoding>UTF-8</encoding>
      </configuration>
    </plugin>

    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <version>2.1.3.RELEASE</version>
      <executions>
        <execution>
          <goals>
            <goal>repackage</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```



## 四、创建启动类

```java
@EnableWebMvc
@SpringBootApplication(scanBasePackages = "com.fangdd")
public class GraphqlProviderApplication {
    public static void main(String[] args) {
        new SpringApplication(RedisRegistryGraphqlApplication.class).run(args);
    }
}
```



## 五、配置

```bash
#服务端口号
server.port=12346

#当前服务的调用地址
application.server=http://127.0.0.1:12346

## spring服务名称
spring.application.name=common.graphql

## 当前Graphql Provider负责的领域名称，建议以小写字母开头，使用驼峰命名规则
graphql.schema.module=article

## 注册中心里的路径，一定要与引擎里的 fdd.graphql.register.root 值一致！！
graphql.registry.redis=graphql-dev

## redis配置，一定要与GraphQL引擎里的配置一致！
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



## 六、添加RESTful API

在开始之前，先明确一下几个概念：

标准视图：领域的实体对象，可以理解为数据库里的实体表。标准视图可以向外（全局性的）暴露0~N个外键名称，其它视图里面出现了此名称，引擎会自动关联上本实体。比如有个Article实体，它向外暴露了`articleId`和`wzId`两个外键。标准视图会映射成GraphQL Schema里的Type

非标准视图：领域的非固定数据，可以理解为数据库里的关联表。非标准视图它不是固定的实体，比如推荐的文章`RecommendArticle`，它可能只有文章ID(articleId)、推荐值等少量与推荐相关的信息。它不对外暴露外键，而是通过本实体内的articleId去关联上实体Article，这样相当于添加了一个属性：`RecommendArticle.article`，而`article`属性类型是`Article`，就可以获取到所有的文章信息了。



### 1. 添加实体类 pojo

Article.java

```java
/**
 * 文章（这里的注释会生成GraphQL Schema里对应Type的注释）
 */
public class Article {
  /**
   * 文章ID (这个注释很重要，会直接映射成GraphQL Schema文档里的注释，标准视图的id必须是id，不用用其它，比如_id)
   */
  private Integer id;
  
  /**
   * 文章标题
   */
  private String title;
  
  /**
   * 文章内容
   */
  private String content;
  
  //这里省略掉getter和setter方法
}
```



BasePagedList.java

```java
//这个实体是非标准视图中需要
public class BasePagedList<T> {
    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 数据列表
     */
    private List<T> list;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
```





### 2. 添加Controller

```java
@RestController
@GraphqlModule("article") //可选，声明当前的领域，如果与配置中的graphql.schema.module值一致，则可以省略，一个GraphQL Provider允许实现多个领域，这也是为了在项目之初某些领域比较小时，先寄放在其它项目里面，可选
@RequestMapping("/api/article")
@SchemaProvider(clazz = Article.class, ids = {"articleId", "wzId"}) //声明为标准视图，并向外注册了这两个外键
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    /**
     * 通过文章ID拉取文章信息 (这些注释，会映射成GraphQL Schema里的注释)
     *
     * @param articleId 文章ID (这些注释，会映射成GraphQL Schema里的注释)
     * @return 文章信息
     */
    @IdProvider //声明成IdProvider，通过外键关联的就是通过此接口获取到实体的详情信息的
    @GetMapping("/{articleId:\\d+}")
    public Article articleById(@PathVariable int articleId) {
        return articleService.getById(
          articleId, 
          SelectionHandler.getSelections()//如果实现了selections，则可以精确的返回查询需要的字段，而不是全量返回，推荐都实现！selections的结构参见另外的文档
        );
    }

    /**
     * 通过文章ids拉取文章列表
     *
     * @param ids 文章ids，多个id使用半角逗号分隔
     * @return 文章列表
     */
    @IdsProvider //批量查询接口，引擎检测到可以合并请求时，会合并多个id，调用这个接口。默认分隔符是半角逗号，也可以配置当前注解属性进行修改。批量接口，返回不需要按ids的顺序返回，而是交由引擎处理
    @GetMapping
    public List<Article> articleByIds(@RequestParam String ids) {
        Set<Integer> articleIds = Sets.newHashSet();
        Splitter.on(DuoecGraphqlConstant.STR_COMMA)
                .omitEmptyStrings()
                .omitEmptyStrings()
                .split(ids)
                .forEach(idStr -> articleIds.add(Integer.parseInt(idStr)));
        return articleService.getByIds(articleIds, SelectionHandler.getSelections());
    }

    /**
     * 查询带分页的文章列表
     *
     * @param query 文章筛选条件
     * @return 文章列表
     */
    @GraphqlSelection("list") //如果返回的字段里包裹了多层，可以通过此声明指定selection的前缀
    @GetMapping("/recommend") //所有的Query都必须是Get请求，否则会变成Mutation！
    public BasePagedList<RecommendArticle> articleRecommend(ArticleQuery query) { //生成Schema时，会直接使用当前的方法名，所以请注意当前领域下的命名不要冲突，和前端的可识别性
        return articleService.search(query, SelectionHandler.getSelections());
    }

    /**
     * 文章保存
     *
     * @param request 保存请求参数
     * @return 保存成功后的文章信息
     */
    @PostMapping //非Get方法，会生成到GraphQL Schema的Mutation内，如果有需要写操作后也可以返回需要的字段
    public Article articleSave(@RequestBody ArticleSave request) {
        return articleService.save(request, SelectionHandler.getSelections());
    }

  	/**
  	 * 删除文章
  	 * @param id 文章ID
  	 */
    @DeleteMapping ("/{id:\\d+}") //非Get方法，会生成到GraphQL Schema的Mutation内，如果有需要写操作后也可以返回需要的字段
    public Article articleDelete(@PathVariable int id) {
        return articleService.delete(id, SelectionHandler.getSelections());
    }
}

```

到此为止，所有的基础工作就完成了，一些高阶的玩法，请查看其它文档



以上代码会生成以下Schema，所有实体都会自动添加上领域名称的前缀

```graphql
{
  Query {
  	article: Article {
  		articleById(articleId: Int!) article_Article
			articleByIds(ids: String) 
			articleRecommend(query: article_ArticleQuery) article_BasePagedList_Article
		}
	}
	Mutation {
    article: M_Article {
    	articleSave(request: article_ArticleSave) article_Article
			articleDelete(id: Int!) article_Article
  	}
  }
}
```

