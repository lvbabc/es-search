package zx.soft.tksdn.es.demo;

import java.util.Date;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;

import zx.soft.tksdn.es.utils.ESTransportClient;


public class ScrollDemo {

	public static void main(String[] args) {
		Client client = ESTransportClient.getClient();
		System.out.println("scroll模式已启动");
		Date begin = new Date();

		SearchResponse scrollResponse = client.prepareSearch("tekuanfirst").setSize(10000)
				.setScroll(TimeValue.timeValueMinutes(1)).execute().actionGet();

		long count = scrollResponse.getHits().getTotalHits();//第一次不返回数据

		for (int i = 0, sum = 0; sum < count; i++) {
			scrollResponse = client.prepareSearchScroll(scrollResponse.getScrollId())
					.setScroll(TimeValue.timeValueMinutes(8)).execute().actionGet();
			sum += scrollResponse.getHits().hits().length;
			System.out.println("总量" + count + " 已经查到" + sum);
		}
		Date end = new Date();
		System.out.println("耗时: " + (end.getTime() - begin.getTime()));
	}
}
