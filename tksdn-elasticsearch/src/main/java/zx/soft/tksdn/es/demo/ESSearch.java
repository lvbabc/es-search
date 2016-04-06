package zx.soft.tksdn.es.demo;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


	public SearchHit[] fuzzyquery() {

		FuzzyQueryBuilder fBuilder = QueryBuilders.fuzzyQuery("content", "协调");

		return query(fBuilder);

	}


	public SearchHit[] query(QueryBuilder qBuilder) {
		SearchResponse response = null;
		SearchRequestBuilder sBuilder = client.prepareSearch("tekuan").setTypes("record");
		response = sBuilder.setQuery(qBuilder).setFrom(0).setSize(5).setExplain(true)
				.execute().actionGet();

		SearchHits hits = response.getHits();
		SearchHit[] searchHist = hits.getHits();

		for (SearchHit sh : searchHist) {
			System.out.println("content:" + sh.getSource());
		}

		return searchHist;

	}

	public static void main(String[] args) {
		SearchHit[] searchHists = ESSearch.getInstance().fuzzyquery();



		for (SearchHit sh : searchHists) {
			System.out.println("content:" + sh.getSource());
		}
		//		System.out.println(hits.getTotalHits());
		//		System.out.println(hits.hits().length);
	}
}
