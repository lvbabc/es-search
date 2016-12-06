package zx.soft.tksdn.es.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.tksdn.common.domain.KeywordsCount;
import zx.soft.tksdn.common.domain.OverAllRequest;
import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.common.index.SearchResult;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.domain.SimpleAggInfo;
import zx.soft.tksdn.es.utils.ESTransportClient;
import zx.soft.utils.json.JsonUtils;
import zx.soft.utils.log.LogbackUtil;
import zx.soft.utils.regex.RegexUtils;

/**
 * ES搜索类
 * 因ES搜索分类较多，暂时不进行合并，后期再合并
 * 已进行简单合并，后期还需修改
 * @author lvbing
 *
 */
public class ESQueryCore {

	private static Logger logger = LoggerFactory.getLogger(ESQueryCore.class);

	private final TransportClient client;

	private static final ESQueryCore core = new ESQueryCore();

	private ESQueryCore() {

		client = ESTransportClient.getClient();
	}

	public static ESQueryCore getInstance() {
		return core;
	}

	/**
	 *  获取关键词数量
	 * @param keywords
	 * @param queryParams
	 * @return
	 */
	public List<KeywordsCount> queryKeywords(List<String> keywords, QueryParams queryParams) {
		MultiSearchRequestBuilder mBuilder = client.prepareMultiSearch();
		List<KeywordsCount> kCounts = new ArrayList<>();
		List<Long> counts = new ArrayList<>();
		queryParams.setSize(0);

		for (String string : keywords) {
			SearchRequestBuilder search = null;
			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
			QueryBuilder qBuilder = QueryBuilders.multiMatchQuery(string, "title", "content");
			boolBuilder.must(qBuilder);
			if (queryParams.getRangeStart() != "") {
				qBuilder = QueryBuilders.rangeQuery(queryParams.getRangeFiled()).from(queryParams.getRangeStart())
						.to(queryParams.getRangeEnd()).format("yyyy-MM-dd HH:mm:ss");
				boolBuilder.must(qBuilder);
			}
			search = getSearcher(queryParams);
			search.setQuery(boolBuilder);
			mBuilder.add(search);
		}

		logger.info(queryParams.toString());

		MultiSearchResponse mResponse = mBuilder.execute().actionGet();

		for (MultiSearchResponse.Item item : mResponse.getResponses()) {
			SearchResponse response = item.getResponse();
			counts.add(response.getHits().getTotalHits());
		}

		for (int i = 0; i < keywords.size(); i++) {
			KeywordsCount keywordsCount = new KeywordsCount();
			keywordsCount.setKeyword(keywords.get(i));
			keywordsCount.setCount(counts.get(i));
			kCounts.add(keywordsCount);
		}
		return kCounts;
	}

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
					.to(request.getTimestampend().trim()).format("yyyy-MM-dd HH:mm:ss");
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

	public QueryResult querySingle(QueryParams queryParams) {

		SearchRequestBuilder search = getSearcher(queryParams);
		QueryBuilder qBuilder = null;
		if (queryParams.getQ() != "*") {
			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
			boolBuilder = QueryBuilders.boolQuery().should(QueryBuilders.termQuery("content", queryParams.getQ()))
					.should(QueryBuilders.termQuery("title", queryParams.getQ()));
			qBuilder = boolBuilder;
		} else if (queryParams.getId() != "") {
			qBuilder = QueryBuilders.idsQuery("record").addIds(queryParams.getId());
		} else if (queryParams.getRangeStart() != "") {
			queryParams.setRangeFiled("timestamp");
			qBuilder = QueryBuilders.rangeQuery(queryParams.getRangeFiled()).from(queryParams.getRangeStart())
					.to(queryParams.getRangeEnd()).format("yyyy-MM-dd HH:mm:ss");
		} else {
			qBuilder = QueryBuilders.matchAllQuery();
		}
		search.setQuery(qBuilder);
		QueryResult result = getData(queryParams, search);
		return result;
	}

	/**
	 * 构建SearchRequestBuilder,组合参数
	 */
	private SearchRequestBuilder getSearcher(QueryParams queryParams) {

		SearchRequestBuilder search = null;
		if (queryParams.getIndexName() != "") {
			search = client.prepareSearch(queryParams.getIndexName());
		}
		if (queryParams.getIndexType() != "") {
			search.setTypes(queryParams.getIndexType());
		}
		if (queryParams.getFrom() != 0) {
			search.setFrom(queryParams.getFrom());
		}
		if (queryParams.getSize() != 10) {
			search.setSize(queryParams.getSize());
		}
		if (queryParams.getSort() != "") {
			String sortStr = queryParams.getSort();
			List<String> funcs = RegexUtils.findMatchStrs(sortStr, "\\(.*\\)", true);
			int i = 0;
			for (String func : funcs) {
				sortStr = sortStr.replace(func, "(" + i + ")");
				i++;
			}
			for (String sort : sortStr.split(",")) {
				List<String> parterns = RegexUtils.findMatchStrs(sort, "\\((\\d+)\\)", false);
				if (!parterns.isEmpty()) {
					int tmp = Integer.parseInt(parterns.get(0));
					sort = sort.replaceAll("\\(" + parterns.get(0) + "\\)", funcs.get(tmp));
				}
				search.addSort(sort.split(":")[0],
						"desc".equalsIgnoreCase(sort.split(":")[1]) ? SortOrder.DESC : SortOrder.ASC);
			}
		}
		if (queryParams.getHlfl() != "") {
			for (String field : queryParams.getHlfl().split(",")) {
				search.addHighlightedField(field).setHighlighterRequireFieldMatch(false);
			}
			search.setHighlighterPreTags("<span style=\"color:red\">").setHighlighterPostTags("</span>")
			.setHighlighterFragmentSize(10000);
		}
		if (queryParams.getTermsAgg() != "") {
			search.addAggregation(
					AggregationBuilders.terms(queryParams.getTermsAgg()).field(queryParams.getTermsAgg()).size(10));
		}
		if (queryParams.getDateHistAgg() != "") {
			DateHistogramInterval interval = new DateHistogramInterval(queryParams.getDateInterval());
			search.addAggregation(
					AggregationBuilders.dateHistogram(queryParams.getDateHistAgg()).field(queryParams.getDateHistAgg())
					.interval(interval).minDocCount(0).format("yyy-MM-dd HH:mm:ss"));
		}
		//		search.setExtraSource("filter_path=took,hits.hits._id,hits.hits._score,hits.hits._source&_source=title");

		return search;
	}

	/**
	 * 获取具体数据
	 */
	private QueryResult getData(QueryParams queryParams, SearchRequestBuilder search) {

		SearchResponse response = null;
		try {
			response = search.setExplain(true).execute().actionGet();
		} catch (SearchException e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
			throw new RuntimeException(e);
		}
		if (response.getHits().getTotalHits() == 0) {
			logger.error("no response!");
			return new QueryResult();
		}
		QueryResult result = new QueryResult();
		SearchHits hits = response.getHits();
		SearchHit[] searchHists = hits.getHits();

		result.setAgg(transTermAgg(response, queryParams));
		result.setDateAgg(transDateAgg(response, queryParams));
		result.setSearchHit(transSearchHit(searchHists, queryParams));
		result.setQTime(response.getTookInMillis());
		result.setNumFound(response.getHits().getTotalHits());

		logger.info("numFound=" + result.getNumFound());
		logger.info("QTime=" + result.getQTime());
		return result;
	}

	/**
	 * 获取时间聚合查询结果
	 * @param response
	 * @param queryParams
	 * @return
	 */
	private List<Histogram.Bucket> transDateAgg(SearchResponse response, QueryParams queryParams) {
		List<Histogram.Bucket> agg = new ArrayList<>();

		if (queryParams.getDateHistAgg() != "") {
			Histogram histogram = response.getAggregations().get(queryParams.getDateHistAgg());
			@SuppressWarnings("unchecked")
			List<Histogram.Bucket> buckets = (List<Histogram.Bucket>) histogram.getBuckets();
			for (Histogram.Bucket bucket : buckets) {
				agg.add(bucket);
			}
		}
		return agg;
	}

	/**
	 * 获取聚合查询结果
	 * @param response
	 * @param queryParams
	 * @return
	 */
	private List<SimpleAggInfo> transTermAgg(SearchResponse response, QueryParams queryParams) {
		List<SimpleAggInfo> agg = new ArrayList<>();

		if (queryParams.getTermsAgg() != "") {
			Terms terms = response.getAggregations().get(queryParams.getTermsAgg());
			List<Terms.Bucket> buckets = terms.getBuckets();

			for (Terms.Bucket bucket : buckets) {
				SimpleAggInfo aggInfo = new SimpleAggInfo();
				aggInfo.setName(bucket.getKeyAsString());
				logger.info(bucket.getKeyAsString());
				aggInfo.setValue(bucket.getDocCount());
				System.out.println(bucket.getDocCount());
				agg.add(aggInfo);
			}
		}
		return agg;
	}

	/**
	 * 获取ES文档 把高亮加入到文档中方便调用
	 * @param searchHists
	 * @param queryParams
	 * @return
	 */
	private List<SearchResult> transSearchHit(SearchHit[] searchHists, QueryParams queryParams) {
		if (searchHists.length == 0) {
			return null;
		}
		List<SearchResult> sHits = new ArrayList<SearchResult>();

		Map<String, Object> fields = new LinkedHashMap<>();
		for (SearchHit sHit : searchHists) {

			String json = sHit.getSourceAsString();
			SearchResult record = JsonUtils.getObject(json, SearchResult.class);
			record.setId(sHit.getId());
			if (queryParams.getHlfl() != "") {
				for (String field : queryParams.getHlfl().split(",")) {

					if (sHit.getHighlightFields().get(field) != null) {
						Text[] titleTexts = sHit.getHighlightFields().get(field).getFragments();
						String tString = "";
						for (Text text : titleTexts) {
							tString += text;
						}
						fields.put(field, tString);
						record.setField(fields);
					}
				}
			}
			sHits.add(record);
		}
		return sHits;
	}

	public void close() {
		client.close();
	}
}
