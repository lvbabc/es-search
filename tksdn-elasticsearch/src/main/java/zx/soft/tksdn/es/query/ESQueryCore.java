package zx.soft.tksdn.es.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.es.demo.ESTransportClient;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.utils.log.LogbackUtil;
import zx.soft.utils.regex.RegexUtils;

/**
 * ES搜索类
 *
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
	public QueryResult queryData(QueryParams queryParams) {

		SearchRequestBuilder search = getSearcher(queryParams);
		SearchResponse response = null;
		try {
			response = search.setExplain(true).execute().actionGet();
		} catch (SearchException e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
			throw new RuntimeException(e);
		}
		if (response == null || response.getHits() == null) {
			logger.error("no response!");
		}
		QueryResult result = new QueryResult();
		SearchHits hits = response.getHits();
		SearchHit[] searchHists = hits.getHits();

		result.setQTime(response.getTookInMillis());
		result.setNumFound(response.getHits().getTotalHits());

		List<Map<String, Object>> sHits = new ArrayList<Map<String, Object>>();
		for (SearchHit sHit : searchHists) {
			sHits.add(sHit.getSource());
		}
		result.setSearchHit(sHits);

		logger.info("numFound=" + result.getNumFound());
		logger.info("QTime=" + result.getQTime());

		return result;
	}

	/**
	 * 构建SearchRequestBuilder,将参数组合
	 */
	private SearchRequestBuilder getSearcher(QueryParams queryParams) {

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
			search.addHighlightedField(queryParams.getHlfl()).setHighlighterPreTags("<span style=\"color:red\">")
					.setHighlighterPostTags("</span>");
		}
		//
		//
		//
		/**
		 * 如何选择QueryBuilder 待定
		 */
		QueryBuilder queryStringQuery = QueryBuilders.queryStringQuery(queryParams.getQ());
		search.setQuery(queryStringQuery);
		//		search.setQuery(query).setPostFilter(query).addAggregation(null);
		//
		//
		//
		//
		//
		//

		return search;
	}

	/**
	 * 挑选QueryBuilder
	 */
	private List<QueryBuilder> getQueryBuilders(QueryParams queryParams) {
		List<QueryBuilder> queryBuilders = new ArrayList<QueryBuilder>();
		if (queryParams.getQ() == "") {
			QueryBuilder matchAllQuery = QueryBuilders.matchAllQuery();
			queryBuilders.add(matchAllQuery);
		} else {
			QueryBuilder queryStringQuery = QueryBuilders.queryStringQuery(queryParams.getQ());
			queryBuilders.add(queryStringQuery);
		}
		if (queryParams.getRangeFiled() != "") {
			QueryBuilder rangeQuery = QueryBuilders.rangeQuery(queryParams.getRangeFiled())
					.from(queryParams.getRangeStart()).to(queryParams.getRangeEnd())
					.timeZone(queryParams.getTimeZone());
			queryBuilders.add(rangeQuery);
		}
		return queryBuilders;
	}

	/**
	 * Bool
	 */
	private QueryBuilder getBoolQuery() {
		QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("", "")).mustNot(null)
				.should(null);

		return null;
	}

	/**
	 * Query 之 matchQuery
	 */
	private QueryBuilder getMatchQuery(String filed, String value) {
		QueryBuilder query = QueryBuilders.matchQuery(filed, value);
		return query;
	}

	/**
	 *  Multi Match Query 可查询多个字段
	 */
	private QueryBuilder getMultiMatchQuery(String value, String filed1, String filed2) {
		QueryBuilder query = QueryBuilders.multiMatchQuery(value, filed1, filed2);
		return query;
	}

	/**
	 * Filter 之 FuzzyQuery 根据提供的字符串作为前缀进行查询
	 * @param queryParams
	 * @return
	 */
	private QueryBuilder getFuzzyQuery(String filed, String value) {
		QueryBuilder query = QueryBuilders.fuzzyQuery(filed, value);
		return query;
	}

	/**
	 * test
	 */
	private void test() {
		SearchRequestBuilder srb1 = client.prepareSearch().setQuery(QueryBuilders.queryStringQuery("elasticsearch"))
				.setSize(1);
		SearchRequestBuilder srb2 = client.prepareSearch().setQuery(QueryBuilders.termQuery("name", "kimchy"))
				.setSize(1);
		MultiSearchResponse sr = client.prepareMultiSearch().add(srb1).add(srb2).execute().actionGet();
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

}
