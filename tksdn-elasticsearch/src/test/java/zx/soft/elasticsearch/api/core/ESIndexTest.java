package zx.soft.elasticsearch.api.core;

import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Test;

import zx.soft.tksdn.es.demo.ESIndex;
import zx.soft.tksdn.es.demo.ESTransportClient;

public class ESIndexTest {

	@Test
	public void testExistCreateDeleteIndex() {
		Client client = ESTransportClient.getClient();
		ESIndex esIndex = new ESIndex(client);
		String indexName = "books";
		Assert.assertEquals(false, esIndex.existIndex(indexName));
		esIndex.createIndex("books");
		Assert.assertEquals(true, esIndex.existIndex(indexName));
		esIndex.deleteIndex("books");
		Assert.assertEquals(false, esIndex.existIndex(indexName));
	}

}
