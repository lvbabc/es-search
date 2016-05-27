package zx.soft.tksdn.es.index;

import java.io.IOException;
import java.util.Date;
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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.tksdn.common.index.RecordInfo;
import zx.soft.utils.config.ConfigUtil;
import zx.soft.utils.json.JsonUtils;
import zx.soft.utils.log.LogbackUtil;

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

	public void doIndex(String index, String type, List<RecordInfo> recordInfos) {

		if (recordInfos.size() == 0) {
			return;
		}
		try {
			if (recordInfos != null) {
				logger.info(Integer.toString(recordInfos.size()));
				for (RecordInfo recordInfo : recordInfos) {
					//					IndexRequest indexRequest = new IndexRequest(index, type).source(getTksdnDoc(recordInfo));
					IndexRequest indexRequest = new IndexRequest(index, type)
							.source(JsonUtils.toJsonWithoutPretty(recordInfo));
					bulkProcessor.add(indexRequest);
				}
			}

		} catch (Exception e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
		}
	}

	public void doIndex(String index, String type, RecordInfo recordInfo) {

		//		if (recordInfos.size() == 0) {
		//			return;
		//		}
		try {
			if (recordInfo != null) {
//				logger.info(JsonUtils.toJsonWithoutPretty(recordInfo).toString());
//				logger.info(getTksdnDoc(recordInfo).string());
				IndexRequest indexRequest = new IndexRequest(index, type,recordInfo.getId()).source(JsonUtils.toJsonWithoutPretty(recordInfo));
				bulkProcessor.add(indexRequest);
			}

		} catch (Exception e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
		}
	}

	/**
	 * 每次更改字段的时候，这里也需要更改。
	 */
	public static XContentBuilder getTksdnDoc(RecordInfo record) {

		if (record.getId() == null || record.getId() == "" || record.getId().length() == 0) {
			logger.error("Record's id is null,{}", record);
			return null;
		}
		XContentBuilder builder = null;
		try {
			builder = XContentFactory.jsonBuilder()
					.startObject()
					.field("keyword", record.getKeyword().trim())
					.field("title", record.getTitle().trim())
					.field("url", record.getUrl().trim())
					.field("content", record.getContent().trim())
					.field("timestamp", new Date(record.getTimestamp()))
					.field("lasttime", new Date(record.getLasttime()))
					.field("ip_addr", record.getIp_addr())
					.field("type", record.getType()).endObject();
		} catch (IOException e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
		}
		return builder;
	}

	public void closeESBulkProcessor() {
		try {
			bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
		}
	}
}
