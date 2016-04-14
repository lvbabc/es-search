package zx.soft.tksdn.spring.demo;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.date.DateRangeBuilder;

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.query.ESQueryCore;

/**
 * 测试ES各种功能
 * @author lb
 *
 */
public class MainTest {

	/**
	 * 挑选QueryBuilder
	 */
	private void selectQueryBuilders(QueryParams queryParams) {
		/**
		 * 全部查询
		 */
		QueryBuilder matchAllQuery = QueryBuilders.matchAllQuery();
		/**
		 * 不区分字段查询
		 */
		QueryBuilder queryStringQuery = QueryBuilders.queryStringQuery(queryParams.getQ())
				.defaultOperator(QueryStringQueryBuilder.Operator.AND);
		/**
		 * 范围查询
		 */
		QueryBuilder rangeQuery = QueryBuilders.rangeQuery(queryParams.getRangeFiled())
				.from(queryParams.getRangeStart()).to(queryParams.getRangeEnd()).timeZone(queryParams.getTimeZone());
		/**
		 * 按字段过滤
		 */
		QueryBuilder termQuery = QueryBuilders.termQuery(queryParams.getTerm(), queryParams.getValue());
		/**
		 * 嵌套查询
		 */
		QueryBuilder boolQuery = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("", "")).must(null)
				.mustNot(null).should(null);
		/**
		 * 基础查询
		 */
		QueryBuilder matchQuery = QueryBuilders.matchQuery("", "");
		/**
		 *  Multi Match Query 可查询多个字段
		 */
		QueryBuilder multiQuery = QueryBuilders.multiMatchQuery("", "", "");
		/**
		 * 根据提供的字符串作为前缀进行查询
		 */
		QueryBuilder fuzzyQuery = QueryBuilders.fuzzyQuery("", "");
	}

	/**
	 * test
	 * MultiSearch
	 */
	private void test(Client client) {
		SearchRequestBuilder srb1 = client.prepareSearch().setQuery(QueryBuilders.queryStringQuery("elasticsearch"))
				.setSize(1);
		SearchRequestBuilder srb2 = client.prepareSearch().setQuery(QueryBuilders.termQuery("name", "kimchy"))
				.setSize(1);
		MultiSearchResponse sr = client.prepareMultiSearch().add(srb1).add(srb2).execute().actionGet();
		sr.getResponses();
		for (MultiSearchResponse.Item item : sr.getResponses()) {
			SearchResponse searchResponse = item.getResponse();
		}
	}

	/**
	 * agg
	 * test
	 */
	public QueryResult aggTest(QueryParams queryParams) {

		QueryBuilder matchAllQuery = QueryBuilders.termQuery("content", queryParams.getQ());
		SearchRequestBuilder search = ESQueryCore.getInstance().getSearcher(queryParams, matchAllQuery)
				.addAggregation(AggregationBuilders.terms("by_country").field("src_ip"));
		//						.subAggregation(AggregationBuilders.dateHistogram("by_year").field("dateOfBirth")
		//								.interval((DateHistogramInterval.YEAR))
		//								.subAggregation(AggregationBuilders.avg("avg_children").field("children"))));

		DateRangeBuilder agg = AggregationBuilders.dateRange("date");
		agg.field("index_time");




		return ESQueryCore.getInstance().queryData(queryParams, search);
	}
}
