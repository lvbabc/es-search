package zx.soft.tksdn.es.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.es.demo.ESTransportClient;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.utils.log.LogbackUtil;

/**
 * ES搜索工具类
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

		List<SearchHit> sHits = new ArrayList<SearchHit>();
		for (SearchHit sHit : searchHists) {
			sHits.add(sHit);
		}
		result.setSearchHit(sHits);

		logger.info("numFound=" + result.getNumFound());
		logger.info("QTime=" + result.getQTime());

		return null;
	}
	/**
	 * 构建SearchRequestBuilder
	 * @param queryParams
	 * @return
	 */
	private SearchRequestBuilder getSearcher(QueryParams queryParams) {

		SearchRequestBuilder search = null;
		if (queryParams.getIndexName() != "" && queryParams.getIndexType() != "") {
			search = client.prepareSearch(queryParams.getIndexName()).setSearchType(queryParams.getIndexType());
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

		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		return null;
	}

	/**
	 * 构建QueryBuilder
	 * @param queryParams
	 * @return
	 */
	private QueryBuilder getQuery(QueryParams queryParams) {
		QueryBuilder query = QueryBuilders.fuzzyQuery("content", "协调");
		return query;
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




	/**
	 * 返回高亮文本
	 *
	 * @param hit
	 * 				搜索高亮结果
	 * @param key
	 * 				搜索高亮字段
	 * @return
	 */
	public Text getHightText(SearchHit hit, String key) {
		return hit == null ? null
				: hit.highlightFields().isEmpty() ? null
						: hit.highlightFields().get(key) == null ? null
								: hit.highlightFields().get(key).getFragments() == null ? null
										: hit.highlightFields().get(key).getFragments()[0];
	}

}
