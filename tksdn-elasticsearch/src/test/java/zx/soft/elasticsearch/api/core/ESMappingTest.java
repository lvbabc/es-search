package zx.soft.elasticsearch.api.core;

import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import zx.soft.tksdn.es.demo.ESMapping;
import zx.soft.tksdn.es.demo.ESTransportClient;

public class ESMappingTest {

	@Ignore
	@Test
	public void testExistTypeMapping() {

		Client client = ESTransportClient.getClient();
		ESMapping mapping = new ESMapping(client);
		Assert.assertEquals(true, mapping.existType("index", "fulltext"));
		Assert.assertEquals(false, mapping.existType("index", "full_text"));
	}

}
