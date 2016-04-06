package zx.soft.elasticsearch.api.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.elasticsearch.client.Client;
import org.junit.Test;

import zx.soft.tksdn.es.demo.ESBulkProcessor;
import zx.soft.tksdn.es.demo.ESTransportClient;

public class ESBulkProcessorTest {

	@Test
	public void testAddBrowsingRecord() {
		Client client = ESTransportClient.getClient();
		ESBulkProcessor processor = new ESBulkProcessor(client);
		String index = "tekuan";
		String type = "record";
		List<Object> records = new ArrayList<>();
		BrowsingRecord r1 = new BrowsingRecord(
				"192.168.6.126",
				80,
				"192.168.6.222",
				1000,
				"HTTP",
				new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis() + 2334),
				"总有一天你会想到把这些对象存储到数据库中。将这些数据保存到由行和列组成的关系数据库中，就好像是把一个丰富，信息表现力强的对象拆散了放入一个非常大的表格中：你不得不拆散对象以适应表模式（通常一列表示一个字段），然后又不得不在查询的时候重建它们。",
				"数据库");
		records.add(r1);
		processor.doIndex(index, type, records);
		processor.closeESBulkProcessor();
	}
}
