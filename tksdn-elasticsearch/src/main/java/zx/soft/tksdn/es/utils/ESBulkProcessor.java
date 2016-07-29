package zx.soft.tksdn.es.utils;

import java.io.IOException;
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
import zx.soft.utils.time.TimeUtils;

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
				logger.info("action数量: " + request.numberOfActions());
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
				if (response.hasFailures()) {
					logger.error("部分请求失败");
				}
				logger.info("The time took  " + response.getTook());
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
				for (RecordInfo recordInfo : recordInfos) {
					IndexRequest indexRequest = new IndexRequest(index, type, recordInfo.getId())
							.source(generateJson(recordInfo));
					bulkProcessor.add(indexRequest);
				}
			}
		} catch (Exception e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
		}
	}

	public void doIndex(String index, String type, RecordInfo recordInfo) {

		try {
			if (recordInfo != null) {
				IndexRequest indexRequest = new IndexRequest(index, type)
						.source(JsonUtils.toJsonWithoutPretty(recordInfo));
				bulkProcessor.add(indexRequest);
			}
		} catch (Exception e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
		}
	}

	/**
	 * transToJson
	 * @param recordInfo
	 * @return
	 */
	private XContentBuilder generateJson(RecordInfo recordInfo) {
		String json = "";
		XContentBuilder content = null;
		try {
			XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject();
			contentBuilder.field("username", recordInfo.getUsername());
			contentBuilder.field("identity_id", recordInfo.getIdentity_id());
			contentBuilder.field("phone_num", recordInfo.getPhone_num());
			contentBuilder.field("ICCID", recordInfo.getICCID());
			contentBuilder.field("timestamp", TimeUtils.transStrToCommonDateStr(recordInfo.getTimestamp().toString()));
			contentBuilder.field("src_ip", recordInfo.getSrc_ip());
			contentBuilder.field("des_ip", recordInfo.getDes_ip());
			contentBuilder.field("src_port", recordInfo.getSrc_port());
			contentBuilder.field("des_port", recordInfo.getDes_port());
			contentBuilder.field("protocol_type", recordInfo.getProtocol_type());
			contentBuilder.field("header", recordInfo.getHeader());
			contentBuilder.field("url", recordInfo.getUrl());
			contentBuilder.field("flow_type", recordInfo.getFlow_type());
			contentBuilder.field("resource_type", recordInfo.getResource_type());
			contentBuilder.field("domain_name", recordInfo.getDomain_name());
			contentBuilder.field("size", recordInfo.getSize());
			contentBuilder.field("content", recordInfo.getContent());
			contentBuilder.field("title", recordInfo.getTitle());
			content = contentBuilder.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public void closeESBulkProcessor() {
		try {
			bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
		}
	}
}
