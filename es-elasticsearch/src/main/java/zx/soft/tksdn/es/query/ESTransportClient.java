package zx.soft.tksdn.es.query;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import zx.soft.utils.config.ConfigUtil;

/**
 *es集群连接客户端
 * @author xuwenjuan
 *
 */
public class ESTransportClient {

	private static TransportClient client;

	public static TransportClient getClient() {

		if (client == null) {
			Properties prop = ConfigUtil.getProps("elasticsearch.properties");
			Settings settings = Settings.settingsBuilder().put("cluster.name", prop.getProperty("cluster.name"))
					.put("client.transport.ping_timeout", prop.getProperty("client.transport.ping_timeout"))
					.put("client.transport.sniff", true).put("client.transport.nodes_sampler_interval",
							prop.getProperty("client.transport.nodes_sampler_interval"))
					.build();
			client = TransportClient.builder().settings(settings).build();
			try {
				String host = prop.getProperty("es.ip");
				List<String> hosts = Arrays.asList(host.split(","));
				if (hosts.size() > 0) {
					for (int i = 0; i < hosts.size(); i++) {
						client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hosts.get(i)),
								Integer.parseInt(prop.getProperty("es.port"))));
					}
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return client;
	}
}
