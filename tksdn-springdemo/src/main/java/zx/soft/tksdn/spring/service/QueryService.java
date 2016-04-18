package zx.soft.tksdn.spring.service;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.query.ESQueryCore;

/**
 * @author lvbing
 */
@Service
public class QueryService {
	private static Logger logger = LoggerFactory.getLogger(QueryService.class);

	public QueryResult queryData(QueryParams params) {

		logger.info(params.toString());

		QueryBuilder matchAllQuery = QueryBuilders.rangeQuery("index_time").from("2016-05-24 13:00:00")
				.to("2016-09-24 13:00:00").format("yyyy-MM-dd HH:mm:ss").timeZone("+08:00");
		SearchRequestBuilder search = ESQueryCore.getInstance().getSearcher(params, matchAllQuery);

		search.addAggregation(AggregationBuilders.dateHistogram("index_time").field("index_time")
				.interval(DateHistogramInterval.HOUR).minDocCount(0).format("yyyy-MM-dd HH:mm:ss"));
		return ESQueryCore.getInstance().queryData(params, search);

	}

	public QueryResult termsQuery(QueryParams queryParams) {
		logger.info(queryParams.toString());
		List<String> values = new ArrayList<>();
		values.add("类");
		QueryBuilder termsQuery = QueryBuilders.termsQuery(queryParams.getQ(), values);

		SearchRequestBuilder search = ESQueryCore.getInstance().getSearcher(queryParams, termsQuery);

		return ESQueryCore.getInstance().queryData(queryParams, search);
	}

	public QueryResult boolQuery(QueryParams queryParams) {

		logger.info(queryParams.toString());
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		return null;

	}

	public QueryResult queryStringQuery(QueryParams queryParams) {

		String q = queryParams.getQ().toUpperCase().replaceAll("(AND|OR|NOT)", " " + "$0" + " ")
				.replaceAll("(\\+|-|\\|)", " " + "$0");
		System.out.println(q);
		QueryBuilder queryStringQuery = QueryBuilders.queryStringQuery(q).field("content").field("keyword");
		SearchRequestBuilder search = ESQueryCore.getInstance().getSearcher(queryParams, queryStringQuery);

		return ESQueryCore.getInstance().queryData(queryParams, search);

	}

}
