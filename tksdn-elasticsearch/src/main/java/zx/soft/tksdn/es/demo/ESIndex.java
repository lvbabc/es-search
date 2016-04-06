package zx.soft.tksdn.es.demo;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *索引操作：判断是否存在，创建，删除
 * @author xuwenjuan
 *
 */
public class ESIndex {

	public static Logger logger = LoggerFactory.getLogger(ESIndex.class);

	private Client client;

	public ESIndex(Client client) {
		this.client = client;
	}

	public boolean existIndex(String index) {
		IndicesAdminClient indicesAdminClient = client.admin().indices();
		IndicesExistsRequest exist = new IndicesExistsRequest(index);
		IndicesExistsResponse response = indicesAdminClient.exists(exist).actionGet();
		return response.isExists();
	}

	public boolean createIndex(String index) {
		if (!existIndex(index)) {
			IndicesAdminClient indicesAdminClient = client.admin().indices();
			CreateIndexRequest request = new CreateIndexRequest(index);
			CreateIndexResponse response = indicesAdminClient.create(request).actionGet();
			if (response.isAcknowledged()) {
				logger.info("create index:" + index + " succeed.");
			} else {
				logger.error("create index:" + index + " failed.");
				return false;
			}
		}
		logger.info("the specific index:" + index + " already exist.");
		return false;
	}

	public void deleteIndex(String index) {
		if (existIndex(index)) {
			IndicesAdminClient indicesAdminClient = client.admin().indices();
			DeleteIndexRequest request = new DeleteIndexRequest(index);
			DeleteIndexResponse response = indicesAdminClient.delete(request).actionGet();
			if (response.isAcknowledged()) {
				logger.info("delete index:" + index + " succeed.");
			} else {
				logger.error("delete index:" + index + " failed.");
			}
		} else {
			logger.info("the specific index does not exist.");
		}

	}
}
