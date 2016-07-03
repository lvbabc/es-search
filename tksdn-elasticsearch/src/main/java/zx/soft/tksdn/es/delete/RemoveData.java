package zx.soft.tksdn.es.delete;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.tksdn.es.query.ESTransportClient;

public class RemoveData {

	private static Logger logger = LoggerFactory.getLogger(RemoveData.class);
	private Client client = null;
	private BulkRequestBuilder bulkRequest = null;

	public RemoveData() {
		client = ESTransportClient.getClient();
		bulkRequest = client.prepareBulk();
	}

	public static void main(String args[]) {
		RemoveData remove = new RemoveData();
		remove.run();
	}

	public void run() {
		logger.info("start");
		QueryBuilder qBuilder = QueryBuilders.matchAllQuery();

		while (true) {
			SearchResponse scrollResp = client.prepareSearch("tekuan").setQuery(qBuilder).setSize(10000).execute()
					.actionGet();
			long count = scrollResp.getHits().getTotalHits();
			logger.info("该段时间数据总量为:  " + count);

			for (SearchHit hit : scrollResp.getHits().getHits()) {
				String id = hit.getId();
				bulkRequest.add(client.prepareDelete("tekuansecond", "record", id));
			}
			logger.info(("删除量  " + bulkRequest.numberOfActions()));

			BulkResponse bulkResponse = bulkRequest.get();
			if (bulkResponse.hasFailures()) {
				logger.info("failures");
			}
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}
		logger.info("end");
	}
}
