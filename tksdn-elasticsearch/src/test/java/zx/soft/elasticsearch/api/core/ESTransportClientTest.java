package zx.soft.elasticsearch.api.core;

import org.elasticsearch.client.transport.TransportClient;
import org.junit.Assert;
import org.junit.Test;

import zx.soft.tksdn.es.demo.ESTransportClient;

public class ESTransportClientTest {

	@Test
	public void testTransportClient1() {

		TransportClient client = ESTransportClient.getClient();
		Assert.assertNotNull(client);
	}

	@Test
	public void testTransportClient2() {

		TransportClient client1 = ESTransportClient.getClient();
		TransportClient client2 = ESTransportClient.getClient();
		Assert.assertEquals(client1, client2);
	}
}
