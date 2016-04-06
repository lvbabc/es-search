package zx.soft.tksdn.es.demo;

import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESMapping {

	public static Logger logger = LoggerFactory.getLogger(ESMapping.class);
	private Client client;

	public ESMapping(Client client) {
		this.client = client;
	}

	public boolean existType(String index, String type) {
		String[] indices = new String[1];
		indices[0] = index;
		String[] types = new String[1];
		types[0] = type;
		TypesExistsRequest request = new TypesExistsRequest(indices, types);
		TypesExistsResponse response = client.admin().indices().typesExists(request).actionGet();
		if (response.isExists()) {
			logger.info("the Type:" + type + " of Index:" + index + " exist.");
			return true;
		} else {
			logger.info("the Type:" + type + " of Index:" + index + " does not exist.");
			return false;
		}
	}

}
