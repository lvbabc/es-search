package zx.soft.tksdn.es.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
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

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.common.index.BrowsingRecord;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.domain.SimpleAggInfo;
import zx.soft.utils.json.JsonUtils;
import zx.soft.utils.log.LogbackUtil;
import zx.soft.utils.regex.RegexUtils;

/**
 * ES搜索类
 * 因ES搜索分类较多，暂时不进行合并，后期再合并
 * @author lvbing
 *
 */
public class ESQueryCore {

	private static Logger logger = LoggerFactory.getLogger(ESQueryCore.class);

	private final Client client;

	private static ESQueryCore core = new ESQueryCore();

	private ESQueryCore() {
		client = ESTransportClient.getClient();
	}

	public static ESQueryCore getInstance() {
		return core;
	}

	/**
	 * 查询具体数据
	 */
	public QueryResult queryData(QueryParams queryParams, SearchRequestBuilder search) {

		//		SearchRequestBuilder search = getSearcher(queryParams);
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
		if (hits.getHits().length != 0) {
			result.setSort(hits.getHits()[0].getSortValues());
		}
		result.setSearchHit(transSearchHit(searchHists, queryParams));
		//		result.setHighlighting(highlighting);
		logger.info("numFound=" + result.getNumFound());
		logger.info("QTime=" + result.getQTime());

		return result;
	}

	/**
	 * 构建SearchRequestBuilder,将参数组合
	 */
	public SearchRequestBuilder getSearcher(QueryParams queryParams, QueryBuilder queryBuilder) {

		SearchRequestBuilder search = null;
		QueryBuilder query = null;
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
		if (queryParams.getTermAgg() != "") {
			search.addAggregation(
					AggregationBuilders.terms(queryParams.getTermAgg()).field(queryParams.getTermAgg()).size(15));
		}
		if (queryParams.getDateHistAgg() != "") {
			DateHistogramInterval interval = new DateHistogramInterval(queryParams.getDateInterval());
			search.addAggregation(
					AggregationBuilders.dateHistogram(queryParams.getDateHistAgg()).field(queryParams.getDateHistAgg())
							.interval(interval).minDocCount(0).format("yyy-MM-dd HH:mm:ss"));
		}
		//
		//
		//
		/**
		 * 如何选择QueryBuilder 待定
		 */
		//		QueryBuilder queryStringQuery = QueryBuilders.termQuery("content", queryParams.getQ());
		//		QueryBuilder bool = QueryBuilders.boolQuery().should(QueryBuilders.termQuery("content", queryParams.getQ()))
		//				.should(QueryBuilders.termQuery("keyword", queryParams.getQ()));

		//				.operator(MatchQueryBuilder.Operator.AND);
		//		search.setPostFilter(queryBuilder);
		search.setQuery(queryBuilder);
		//		search.setQuery(query).setPostFilter(query).addAggregation(null);
		//

		return search;
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

		if (queryParams.getTermAgg() != "") {
			Terms terms = response.getAggregations().get(queryParams.getTermAgg());
			List<Terms.Bucket> buckets = terms.getBuckets();
			SimpleAggInfo aggInfo = new SimpleAggInfo();
			HashMap<String, Long> t = new LinkedHashMap<>();
			for (Terms.Bucket bucket : buckets) {
				t.put(bucket.getKeyAsString(), bucket.getDocCount());
			}
			aggInfo.setName(queryParams.getDateHistAgg());
			aggInfo.setValues(t);
			agg.add(aggInfo);
		}
		return agg;
	}

	/**
	 * 获取ES文档
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

	/**
	 * 返回节点信息（详细信息）
	 * ClusterStatsResponse.getStatus() 返回节点状态(green yellow red)
	 * ClusterStatsResponse.getClusterNameAsString() 返回节点名称
	 * ClusterStatsResponse.getIndicesStats() 返回节点中所有索引的信息
	 *
	 * @return
	 */
	public ClusterStatsResponse getClusterInfo() {
		return client.admin().cluster().prepareClusterStats().execute().actionGet();
	}

	/**
	 *  得到一个指定索引的source
	 *
	 *  @param indexName
	 *  @param indexType
	 *  @param indexId
	 *
	 *  @return
	 */
	public Map<String, Object> getElasticSearchSource(String indexName, String indexType, String indexId) {
		return client.prepareGet(indexName, indexType, indexId).execute().actionGet().getSource();
	}

	/**
	 *  删除一个指定索引的source
	 *
	 *  @param indexName
	 *  @param indexType
	 *  @param indexId
	 *
	 *  @return
	 */
	public boolean deleteIndex(String indexName, String indexType, String indexId) {
		boolean isSucceed = true;
		try {
			client.prepareDelete(indexName, indexType, indexId).execute().actionGet();
		} catch (Exception e) {
			isSucceed = false;
			logger.error(LogbackUtil.expection2Str(e));
		}
		return isSucceed;
	}

	/**
	 * 是否有分片信息错误
	 *
	 * @param response
	 * @return
	 */
	public boolean isHasShardFailed(SearchResponse response) {
		boolean isFailed = false;
		if (response != null) {
			isFailed = response.getFailedShards() > 0;
		}
		return isFailed;
	}

	public void close() {
		client.close();
	}

}
