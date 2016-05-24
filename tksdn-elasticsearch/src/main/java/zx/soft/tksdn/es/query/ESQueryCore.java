package zx.soft.tksdn.es.query;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
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
import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.common.index.BrowsingRecord;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.domain.SimpleAggInfo;
import zx.soft.utils.config.ConfigUtil;
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

	private static ESQueryCore core = new ESQueryCore();

	private ESQueryCore() {
		//		client = ESTransportClient.getClient();
		//		if (client == null) {
		Properties prop = ConfigUtil.getProps("elasticsearch.properties");
		Settings settings = Settings.settingsBuilder().put("cluster.name", prop.getProperty("cluster.name"))
				.put("client.transport.ping_timeout", prop.getProperty("client.transport.ping_timeout"))
				.put("client.transport.nodes_sampler_interval",
						prop.getProperty("client.transport.nodes_sampler_interval"))
				.build();
		client = TransportClient.builder().settings(settings).build();
		try {
			String host = prop.getProperty("es.ip");
			List<String> hosts = Arrays.asList(host.split(","));
			if (hosts.size() > 0) {
				for (int i = 0; i < hosts.size(); i++) {
					client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hosts.get(i)),
							Integer.parseInt(prop.getProperty("es.port"))));
				}
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		//		}
	}

	public static ESQueryCore getInstance() {
		return core;
	}

	/**
	 * 查询具体数据
	 */
	public QueryResult queryData(QueryParams queryParams, boolean isDefault) {

		SearchRequestBuilder search = getSearcher(queryParams, isDefault);
		logger.info(search.toString());

		SearchResponse response = null;
		try {
			response = search.setExplain(true).execute().actionGet();
		} catch (SearchException e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
			throw new RuntimeException(e);
		}
		if (response == null) {//|| response.getHits() == null || response.getHits().getHits().length == 0) {
			logger.error("no response!");
			return new QueryResult();
		}
		QueryResult result = new QueryResult();
		SearchHits hits = response.getHits();
		SearchHit[] searchHists = hits.getHits();

		result.setAgg(transTermAgg(response, queryParams));
		result.setDateAgg(transDateAgg(response, queryParams));
		result.setQTime(response.getTookInMillis());
		result.setNumFound(response.getHits().getTotalHits());
//		if (hits.getHits().length != 0) {
//			result.setSort(hits.getHits()[0].getSortValues());
//		}
		result.setSearchHit(transSearchHit(searchHists, queryParams));
		//		result.setHighlighting(highlighting);
		logger.info("numFound=" + result.getNumFound());
		logger.info("QTime=" + result.getQTime());
		//		close();
		return result;
	}

	/**
	 * 构建SearchRequestBuilder,组合参数
	 */
	public SearchRequestBuilder getSearcher(QueryParams queryParams, boolean isDefault) {

		SearchRequestBuilder search = null;
		if (queryParams.getIndexName() != "") {
			search = client.prepareSearch(queryParams.getIndexName());
		}
		if (queryParams.getIndexType() != "") {
			search.setTypes(queryParams.getIndexType());
		}
		if (queryParams.getPreferenceType() != "") {
			search.setPreference(queryParams.getPreferenceType());
		}
		if (queryParams.getSearchType() != "") {
			search.setSearchType(queryParams.getSearchType());
		}
		if (queryParams.getIncludes() != null) {
			search.setFetchSource(queryParams.getIncludes(), null);
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
				search.addHighlightedField(field);
			}
			search.setHighlighterPreTags("<span style=\"color:red\">").setHighlighterPostTags("</span>")
					.setHighlighterFragmentSize(10000);
		}
		if (queryParams.getTermsAgg() != "") {
			search.addAggregation(
					AggregationBuilders.terms(queryParams.getTermsAgg()).field(queryParams.getTermsAgg()).size(15));
		}
		if (queryParams.getDateHistAgg() != "") {
			DateHistogramInterval interval = new DateHistogramInterval(queryParams.getDateInterval());
			search.addAggregation(
					AggregationBuilders.dateHistogram(queryParams.getDateHistAgg()).field(queryParams.getDateHistAgg())
							.interval(interval).minDocCount(0).format("yyy-MM-dd HH:mm:ss"));
		}
		/**
		 * 如何选择QueryBuilder 待定
		 */
		QueryBuilder queryBuilder = getQueryBuilder(queryParams, isDefault);

		search.setQuery(queryBuilder);

		return search;
	}

	/**
	 * 构建QueryBuilder
	 * @param queryParams
	 * @return
	 */
	private QueryBuilder getQueryBuilder(QueryParams queryParams, boolean isDefault) {

		QueryBuilder queryBuilder = null;
		//默认查询,返回全部数据
		if (queryParams.getQ() == "*" && isDefault) {
			queryBuilder = QueryBuilders.matchAllQuery();
		}
		//默认查询,返回指定数据
		if (queryParams.getQ() != "*" && isDefault) {
			//默认字段待定
			queryBuilder = QueryBuilders.queryStringQuery(queryParams.getQ());
		}
		//范围查询,目前只用查询日期
		if (queryParams.getRangeFiled() != "*" && (!isDefault)) {
			queryBuilder = QueryBuilders.rangeQuery(queryParams.getRangeFiled()).from(queryParams.getRangeStart())
					.to(queryParams.getRangeEnd()).format("yyyy-MM-dd HH:mm:ss").timeZone(queryParams.getTimeZone());
		}
		//bool查询,对查询进行嵌套处理
		if (queryParams.getbQ() != "*" && (!isDefault)) {
			Map<String, String> test = new LinkedHashMap<>();
			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
			for (Map.Entry<String, String> entry : test.entrySet()) {
				QueryBuilder termBuildFirst = QueryBuilders.termQuery("", entry.getKey());
				QueryBuilder termBuildSecond = QueryBuilders.termQuery("", entry.getValue());

				BoolQueryBuilder builder = QueryBuilders.boolQuery().must(termBuildFirst).must(termBuildSecond);
				boolBuilder.should(builder);
			}
			queryBuilder = boolBuilder;
		}
		return queryBuilder;
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
			SimpleAggInfo aggInfo = new SimpleAggInfo();
			HashMap<String, Long> t = new LinkedHashMap<>();
			for (Terms.Bucket bucket : buckets) {
				t.put(bucket.getKeyAsString(), bucket.getDocCount());
			}
			aggInfo.setName(queryParams.getTermsAgg());
			aggInfo.setValues(t);
			agg.add(aggInfo);
		}
		return agg;
	}

	/**
	 * 获取ES文档 把高亮加入到文档中方便调用
	 * @param searchHists
	 * @param queryParams
	 * @return
	 */
	private List<BrowsingRecord> transSearchHit(SearchHit[] searchHists, QueryParams queryParams) {
		if (searchHists.length == 0) {
			return null;
		}
		List<BrowsingRecord> sHits = new ArrayList<BrowsingRecord>();
		//		List<String> highlighting = new ArrayList<String>();

		Map<String, Object> fields = new LinkedHashMap<>();
		for (SearchHit sHit : searchHists) {

			String json = sHit.getSourceAsString();
			BrowsingRecord browsingRecord = JsonUtils.getObject(json, BrowsingRecord.class);

			if (queryParams.getHlfl() != "") {
				for (String field : queryParams.getHlfl().split(",")) {

					if (sHit.getHighlightFields().get(field) != null) {
						Text[] titleTexts = sHit.getHighlightFields().get(field).getFragments();
						String tString = "";
						for (Text text : titleTexts) {
							tString += text;
						}
						fields.put(field, tString);
						browsingRecord.setField(fields);
					}
				}
			}
			browsingRecord.setId(sHit.getId());
			sHits.add(browsingRecord);
		}
		return sHits;
	}

	/*
	 * 获取关键词数量
	 */
	public List<KeywordsCount> queryKeywords(List<String> keywords ){
		MultiSearchRequestBuilder mBuilder=client.prepareMultiSearch();

		List<KeywordsCount> kCounts = new ArrayList<>();
		List<Long> counts = new ArrayList<>();

		for (String string : keywords) {
			SearchRequestBuilder search = null;
			QueryBuilder qBuilder = QueryBuilders.multiMatchQuery("\""+string+"\"", "content", "title","keyword");
			search=client.prepareSearch("tekuan").setTypes("record").setSize(0).setQuery(qBuilder);
			mBuilder.add(search);
		}
		MultiSearchResponse mResponse = mBuilder.execute().actionGet();

		for (MultiSearchResponse.Item item : mResponse.getResponses()) {
		    SearchResponse response = item.getResponse();
		    counts.add(response.getHits().getTotalHits());
		}
		for (int i=0;i<keywords.size();i++) {
			KeywordsCount keywordsCount = new KeywordsCount();
			keywordsCount.setKeyword(keywords.get(i));
			keywordsCount.setCount(counts.get(keywords.size()-1-i));
			kCounts.add(keywordsCount);
		}
		return kCounts;
	}

	public void close() {
		client.close();
	}

}
