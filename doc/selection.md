# selection

### selections数据格式

每个接口查询，除了ScalarJson类型以外，都需要指定响应字段，响应字段会以URL参数selections传递给GraphQL-Provider
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

在调用houseById接口时，会生成参数：`selections=name!albums!.cate!.url`
可以把!理解成换行，.理解成tab就是这样：

```
name
albums
    cate
    url
```

GraphQL-Provider需要实现`selections`查询，只返回指定参数的结果。



有时，selections也并不只是在`gql`中写的这些字段，如果涉及到的一些字段，绑定了DataFetcher，则会尝试添加DataFetcher内对应的API依赖到的参数，比如：上面的查询，如果houseById再添加一个`recommendAgent`(推荐经纪人)时，`recommendAgent`字段依赖到了`cityId`的值，GraphQL-Engine会自动添加上`cityId`字段。

如果GraphQL-Provider未实现selections返回，对实际结果也没有什么影响，只是增加了GraphQL-Engine到GraphQL-Provider之间的网络和性能开销。



在GraphQL-Provider的实现里面，可以通过`SelectionHandler.getSelections()`方法获取到selections。demo:

```java
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
                //如果实现了selections，则可以精确的返回查询需要的字段，而不是全量返回，推荐都实现！selections的结构参见另外的文档
                SelectionHandler.getSelections()
        );
    }
```



### @GraphqlSelection

另外，如果响应的实体被多包一层时，比如：

```javascript
{
	"total": 10,
  "list": [
  	{
    	"name": "张三"  
    },
    {
      "name": "李四"
    }
	]
}
```



对应的`gql`可能是这样子的(省略了前面的两层)：

```graphql
articleSearch {
	total
	list {
		title
	}
}
```



GraphQL-Engine组成的`selections=total!list!.title`但是，我们需要的GraphQL-Provider只需要`title`，那么可以通过`@GraphqlSelection`注解来实现，本注解的参数是前缀，带了此注解后，`SelectionHandler.getSelections()`方法会自动筛选出`list`下的字段

```java
    /**
     * 查询带分页的文章列表
     *
     * @param query 文章筛选条件
     * @return 文章列表
     */
    @GraphqlSelection("list") //如果返回的字段里包裹了多层，可以通过此声明指定selection的前缀
    @GetMapping("/search") //所有的Query都必须是Get请求，否则会变成Mutation！
    public BasePagedList<Article> articleSearch(ArticleQuery query) { //生成Schema时，会直接使用当前的方法名，所以请注意当前领域下的命名不要冲突，和前端的可识别性
        return articleService.search(
          query, 
          SelectionHandler.getSelections() //这里取到的值是：["title"]
        );
    }
```

