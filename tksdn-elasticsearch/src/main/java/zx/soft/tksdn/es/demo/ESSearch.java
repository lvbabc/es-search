package zx.soft.tksdn.es.demo;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.log.LogbackUtil;

public class ESSearch {

	private static Logger logger = LoggerFactory.getLogger(ESSearch.class);

	private static Client client;

	private static ESSearch search = new ESSearch();

	public ESSearch() {
		client = ESTransportClient.getClient();
	}

	public static ESSearch getInstance() {
		return search;
	}

	public SearchHit[] query() {
		SearchResponse response = null;
		SearchHit[] searchHist = null;
		try {
			SearchRequestBuilder sBuilder = client.prepareSearch("tekuan").setTypes("record");
			response = sBuilder.setQuery(QueryBuilders.fuzzyQuery("content", "协调")).setFrom(0).setSize(5)
					.setExplain(true).execute().actionGet();

			SearchHits hits = response.getHits();
			searchHist = hits.getHits();

			for (SearchHit sh : searchHist) {
				System.out.println("content:" + sh.getSource());
			}
		} catch (Exception e) {
			logger.error(LogbackUtil.expection2Str(e));
		}
		return searchHist;

	}

	public static void main(String[] args) {
		ESSearch search = new ESSearch();
		search.query();
	}
}
