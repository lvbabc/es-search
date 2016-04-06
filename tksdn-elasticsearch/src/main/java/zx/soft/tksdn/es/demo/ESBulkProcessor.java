package zx.soft.tksdn.es.demo;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.config.ConfigUtil;
import zx.soft.utils.json.JsonUtils;

/**
 *批量索引器
 * @author xuwenjuan
 *
 */
public class ESBulkProcessor {

	public static Logger logger = LoggerFactory.getLogger(ESBulkProcessor.class);
	private final BulkProcessor bulkProcessor;
	private final Client client;

	public ESBulkProcessor(Client client) {
		Properties pros = ConfigUtil.getProps("utils.properties");

		this.client = client;
		bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
				logger.info("action数量", request.numberOfActions());
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
				if (response.hasFailures()) {
					logger.error("部分请求失败");
				}
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				logger.error("请求失败" + failure.getMessage());
			}
		}).setBulkActions(Integer.parseInt(pros.getProperty("bulk.action")))
				.setBulkSize(new ByteSizeValue(Integer.parseInt(pros.getProperty("bulk.size")), ByteSizeUnit.MB))
				.setFlushInterval(TimeValue.timeValueSeconds(Integer.parseInt(pros.getProperty("flush.interval"))))
				.setConcurrentRequests(Integer.parseInt(pros.getProperty("concurrent.requests")))
				.setBackoffPolicy(BackoffPolicy.exponentialBackoff(
						TimeValue.timeValueMillis(Long.parseLong(pros.getProperty("backoff.policy.time.millis"))),
						Integer.parseInt(pros.getProperty("backoff.policy.retry.times"))))
				.build();
	}

	public boolean doIndex(String index, String type, List<Object> objects) {
		//		ESMapping mapping = new ESMapping(this.client);
		//		if (!mapping.existType(index, type)) {
		//			logger.error("不存在对应的index和type");
		//			return false;
		//		}
		for (Object object : objects) {
			IndexRequest indexRequest = new IndexRequest(index, type).source(JsonUtils.toJsonWithoutPretty(object));
			bulkProcessor.add(indexRequest);
		}
		return true;
	}

	public boolean doIndex(String index, String type, Object object) {
		//		ESMapping mapping = new ESMapping(this.client);
		//		if (!mapping.existType(index, type)) {
		//			logger.error("不存在对应的index和type");
		//			return false;
		//		}
		IndexRequest indexRequest = new IndexRequest(index, type).source(JsonUtils.toJsonWithoutPretty(object));
		bulkProcessor.add(indexRequest);
		return true;
	}

	public void closeESBulkProcessor() {
		try {
			bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
