# selection

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

