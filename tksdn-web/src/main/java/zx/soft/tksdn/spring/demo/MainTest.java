package zx.soft.tksdn.spring.demo;

import java.util.Map;

import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.es.query.ESQueryCore;
import zx.soft.utils.log.LogbackUtil;

/**
 * 记录ES功能代码
 * @author lb
 *
 */
public class MainTest {
	private static Logger logger = LoggerFactory.getLogger(ESQueryCore.class);

	/**
	 * 挑选QueryBuilder
	 */
	private void selectQueryBuilders(QueryParams queryParams) {
		/**
		 * 不区分字段查询
		 */
		QueryBuilder queryStringQuery = QueryBuilders.queryStringQuery(queryParams.getQ())
				.defaultOperator(QueryStringQueryBuilder.Operator.AND);
		/**
		 * 按字段过滤
		 */
		QueryBuilder termQuery = QueryBuilders.termQuery(queryParams.getTerm(), queryParams.getValue());
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
	 * 返回节点信息（详细信息）
	 * ClusterStatsResponse.getStatus() 返回节点状态(green yellow red)
	 * ClusterStatsResponse.getClusterNameAsString() 返回节点名称
	 * ClusterStatsResponse.getIndicesStats() 返回节点中所有索引的信息
	 *
	 * @return
	 */
	public ClusterStatsResponse getClusterInfo() {
		Client client = null;
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
		Client client = null;
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
		Client client = null;
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
