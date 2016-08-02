
# Elasticsearch数据索引服务项目

> 基于Java，Elasticsearch,Spring实现。

项目内容
```
    项目简介
    项目架构
    开发人员
```

## 项目简介
### 项目起因

本项目主要应用于底层搜索。
###项目框架
```
tksdn-common: 公共类

tksdn-dao: 数据接口层

tksdn-rediscache: 数据持久化

tksdn-elasticsearch: elasticsearch索引搜索工程

tksdn-web: API接口服务层
```

> 备注: 框架持续更新中。

### API文档
## 项目架构

Elasticsearch 查询分成3个模块，分别是: QueryBuilder SearchRequestBuilder SearchResponse SearchRequestBuilder SearchResponse 基本是固定的，QueryBuilder需要根据具体功能修改
示例代码

查询时针对具体功能写QueryBuilder，如下
```
 /**
     * 综合搜索
     * @param queryParams
     * @return
     */
     public QueryResult getOverAll(QueryParams queryParams) {
        OverAllRequest request = queryParams.getRequest();
        SearchRequestBuilder search = getSearcher(queryParams);
        QueryBuilder qBuilder = null;
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        if (request.getKey() != "") {
            qBuilder = QueryBuilders.multiMatchQuery(request.getKey(), "title", "content");
            boolBuilder.must(qBuilder);
        }
        if (request.getTimestampstart() != "") {
            qBuilder = QueryBuilders.rangeQuery("timestamp").from(request.getTimestampstart().trim())
                    .to(request.getTimestampend().trim()).format("yyyy-MM-dd HH:mm:ss").timeZone("+08:00");
            boolBuilder.must(qBuilder);
        }
        if (!request.getProtocol_type().isEmpty()) {
            qBuilder = QueryBuilders.termsQuery("protocol_type", request.getProtocol_type());
            boolBuilder.must(qBuilder);
        }
        if (request.getFlow_type() != "") {
            qBuilder = QueryBuilders.termQuery("flow_type", request.getFlow_type().trim());
            boolBuilder.must(qBuilder);
        }
        if (!request.getResource_type().isEmpty()) {
            qBuilder = QueryBuilders.termsQuery("resource_type", request.getResource_type());
            boolBuilder.must(qBuilder);
        }
        if (request.getPhone_num() != "") {
            String num = request.getPhone_num();
            if (!num.startsWith("1")) {
                num = "1" + num;
            }
            if (num.length() < 11) {
                num = num + "*";
            }
            qBuilder = QueryBuilders.wildcardQuery("phone_num", num);
            boolBuilder.must(qBuilder);
        }
        search.setQuery(boolBuilder);
        QueryResult result = getData(queryParams, search);
        return result;
    }
```


## 开发人员

WeChat: lv1559744776

QQ: 1559744776

E-mail: binglvabc@gmail.com

