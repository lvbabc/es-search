package zx.soft.tksdn.es.delete;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.elasticsearch.action.deletebyquery.DeleteByQueryAction;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.config.ConfigUtil;

public class RemoveData {

	private static Logger logger = LoggerFactory.getLogger(RemoveData.class);
	private TransportClient client = null;

	public RemoveData() {
		Properties prop = ConfigUtil.getProps("elasticsearch.properties");
		Settings settings = Settings.settingsBuilder().put("cluster.name", prop.getProperty("cluster.name"))
				.put("client.transport.ping_timeout", prop.getProperty("client.transport.ping_timeout"))
				.put("client.transport.nodes_sampler_interval",
						prop.getProperty("client.transport.nodes_sampler_interval"))
				/*.put("client.transport.sniff", true)*/.build();
		client = TransportClient.builder().settings(settings).addPlugin(DeleteByQueryPlugin.class).build();
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

	public static void main(String args[]) {
		RemoveData remove = new RemoveData();
		remove.run();
	}

	public void run() {
		logger.info("start");
		QueryBuilder qb = QueryBuilders.termsQuery("protocol_type", "UDP");
		DeleteByQueryResponse rsp = new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE)
				.setIndices("tekuan").setTypes("record").setQuery(qb).execute().actionGet();
		logger.info("The num found   " + rsp.getTotalFound());
		logger.info("The num deleted   " + rsp.getTotalDeleted());
		logger.info("The time took   " + rsp.getTook());
		logger.info("end");
	}
}
