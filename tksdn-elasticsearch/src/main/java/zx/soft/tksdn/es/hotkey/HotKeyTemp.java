package zx.soft.tksdn.es.hotkey;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsItemResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequestBuilder;
import org.elasticsearch.action.termvectors.MultiTermVectorsResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import zx.soft.tksdn.dao.insight.RiakInsight;
import zx.soft.tksdn.es.query.ESQueryCore;
import zx.soft.tksdn.es.utils.ESTransportClient;
import zx.soft.utils.algo.TopN;
import zx.soft.utils.algo.TopN.KeyValue;
import zx.soft.utils.config.ConfigUtil;
import zx.soft.utils.json.JsonUtils;
import zx.soft.utils.log.LogbackUtil;
import zx.soft.utils.time.TimeUtils;

/**
 * 分时段统计热们关键词：
 *
 * 运行目录：/home/elasticsearch/run-work/timer/hoykey
 * 运行命令：./timer_hotkey.sh &
 * @author lvbing
 *
 */
public class HotKeyTemp {

	private static Logger logger = LoggerFactory.getLogger(HotKeyTemp.class);

	private final static int NUM_EACH_POST = 40;

	private final int num;

	private RiakInsight insight = null;

	private Client client = null;

	public HotKeyTemp() {
		Properties prop = ConfigUtil.getProps("cache-config.properties");
		num = Integer.parseInt(prop.getProperty("cache.num"));
		insight = new RiakInsight();
		client = ESTransportClient.getClient();
	}

	/**
	 * 主函数
	 */
	public static void main(String[] args) {
		HotKeyTemp firstPageRun = new HotKeyTemp();

		try {
			firstPageRun.run();
			//			firstPageRun.insertTrueUserHotKey();
		} catch (Exception e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
		}
		System.exit(0);
	}

	public void run() {
		logger.info("Starting generate data...");
		long current = getHourTime(System.currentTimeMillis());
		for (int i = 0; i < 720; i++) {
			long hours = TimeUtils.transCurrentTime(current, 0, 0, 0, -i);
			int hour = Integer.parseInt(TimeUtils.timeStrByHour(hours).split(",")[1]);
			if (hour % 6 != 0) {
				continue;
			}
			Multiset<String> counts = getOneHourHotKeys(hours);
			Map<String, Integer> hotKeys = getTopNHotKey(counts, 100);
			if (!hotKeys.isEmpty()) {
				insight.insertHotkeys("hotkeys", TimeUtils.timeStrByHour(hours),
						JsonUtils.toJsonWithoutPretty(hotKeys));
				logger.info(hotKeys.toString());
			}
		}
		// 关闭资源
		insight.close();
		ESQueryCore.getInstance().close();
		logger.info("Finishing query OA-FirstPage data...");
	}

	private Multiset<String> getOneHourHotKeys(long milliTime) {
		Multiset<String> counts = HashMultiset.create();
		long current = milliTime;
		long last = TimeUtils.transCurrentTime(current, 0, 0, 0, -6);

		QueryBuilder qBuilder = QueryBuilders.rangeQuery("timestamp").from(TimeUtils.transToCommonDateStr(last))
				.to(TimeUtils.transToCommonDateStr(current)).format("yyyy-MM-dd HH:mm:ss");
		SearchResponse scrollResp = client.prepareSearch("tekuan").setScroll(new TimeValue(60000)).setQuery(qBuilder)
				.setSize(100).execute().actionGet();
		long count = scrollResp.getHits().getTotalHits();
		logger.info("该段时间数据总量为:  " + count);
		logger.info(TimeUtils.transToCommonDateStr(last).toString() + " " + TimeUtils.transToCommonDateStr(current));

		while (true) {

			List<String> ids = new ArrayList<String>();
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				String id = hit.getId();
				ids.add(id);
			}
			if (!ids.isEmpty()) {
				countHotKeys(ids, counts);
			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
					.actionGet();
			//Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}
		return counts;
	}

	private Map<String, Integer> getTopNHotKey(Multiset<String> counts, int N) {
		Map<String, Integer> hotKeys = new HashMap<>();
		if (counts.isEmpty()) {
			return hotKeys;
		}
		List<KeyValue<String, Integer>> topN = TopN.topNOnValue(counts, N);
		for (KeyValue<String, Integer> keyValue : topN) {
			hotKeys.put(keyValue.getKey(), keyValue.getValue());
		}
		return hotKeys;
	}

	public void countHotKeys(List<String> ids, Multiset<String> counts) {
		/**
		 * Initialize the MultiTermVectorsRequestBuilder first
		 */
		MultiTermVectorsRequestBuilder multiTermVectorsRequestBuilder = client.prepareMultiTermVectors();
		TermVectorsRequest.FilterSettings filterSettings = new TermVectorsRequest.FilterSettings(null, null, null, null,
				null, 3, null);
		Map<String, String> perFieldAnalyzer = new HashMap<>();
		perFieldAnalyzer.put("content", "ik_smart");
		/**
		 * For every document ID, create a different TermVectorsRequest and
		 * add it to the MultiTermVectorsRequestBuilder created above
		 */
		for (String id : ids) {

			TermVectorsRequest termVectorsRequest = new TermVectorsRequest().index("tekuan").type("record").id(id)
					.offsets(false).positions(false).fieldStatistics(false).selectedFields("content")
					.filterSettings(filterSettings).perFieldAnalyzer(perFieldAnalyzer);
			multiTermVectorsRequestBuilder.add(termVectorsRequest);
		}

		/**
		 * Finally execute the MultiTermVectorsRequestBuilder
		 */
		MultiTermVectorsResponse response = multiTermVectorsRequestBuilder.execute().actionGet();

		List<String> termStrings = new ArrayList<>();
		for (MultiTermVectorsItemResponse res : response) {
			try {

				Fields fields = res.getResponse().getFields();
				Iterator<String> iterator = fields.iterator();
				while (iterator.hasNext()) {
					String field = iterator.next();
					Terms terms = fields.terms(field);
					TermsEnum termsEnum = terms.iterator();
					while (termsEnum.next() != null) {
						BytesRef term = termsEnum.term();
						if (term != null) {
							counts.add(term.utf8ToString());
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public static long getHourTime(long milli) {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(milli);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTimeInMillis();
	}

}
