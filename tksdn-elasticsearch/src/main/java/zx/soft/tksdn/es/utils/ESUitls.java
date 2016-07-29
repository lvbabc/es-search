package zx.soft.tksdn.es.utils;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.google.common.collect.Maps;


public class ESUitls {

	private Client client = ESTransportClient.getClient();;

	private void createMapping(String index, String type) throws IOException {
		XContentBuilder builder_mapping = XContentFactory.jsonBuilder().startObject().startObject(type)
				.startObject("properties").startObject("Message").field("type", "string").endObject().endObject()
				.endObject().endObject();

		Map<String, String> settings = Maps.newHashMap();
		settings.put("number_of_shards", "6");
		settings.put("number_of_replicas", "1");

		ActionFuture<CreateIndexResponse> response = client.admin().indices()
				.create(Requests.createIndexRequest(index).settings(settings).mapping(type, builder_mapping));

	}

	private void putMapping() throws IOException {
		XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject("general")
				.startObject("properties").startObject("message").field("type", "string").field("index", "not_analyzed")
				.endObject().startObject("source").field("type", "string").endObject().endObject().endObject()
				.endObject();

		PutMappingResponse putMappingResponse = client.admin().indices().preparePutMapping("test").setType("general")
				.setSource(mapping).execute().actionGet();
	}
	/**
	 * 判断索引是否存在
	 */
	private boolean exists(String index) {

		ActionFuture<IndicesExistsResponse> response = client.admin().indices()
				.exists(Requests.indicesExistsRequest(index));

		//        client.close();

		return response.actionGet().isExists();
	}

	/**
	 * 主函数
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		ESUitls esUitls = new ESUitls();
		esUitls.createMapping("test", "type");

	}
}
