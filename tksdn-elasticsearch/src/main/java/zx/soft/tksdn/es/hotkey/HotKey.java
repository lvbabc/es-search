package zx.soft.tksdn.es.hotkey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.util.FilterModifWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.common.index.RecordInfo;
import zx.soft.tksdn.dao.insight.RiakInsight;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.query.ESQueryCore;
import zx.soft.utils.algo.TopN;
import zx.soft.utils.algo.TopN.KeyValue;
import zx.soft.utils.json.JsonUtils;
import zx.soft.utils.log.LogbackUtil;
import zx.soft.utils.time.TimeUtils;

/**
 * 分时段统计热们关键词：
 *
 * 运行目录：/home/elasticsearch/run-work/timer/insight
 * 运行命令：./insighthotkey.sh &
 * @author lvbing
 *
 */
public class HotKey {

	private static Logger logger = LoggerFactory.getLogger(HotKey.class);

	private final static int NUM_EACH_POST = 40;

	private static KeyWordComputer kwc = new KeyWordComputer(NUM_EACH_POST);

	private RiakInsight insight = null;

	public HotKey() {
		insight = new RiakInsight();
	}

	/**
	 * 主函数
	 */
	public static void main(String[] args) {
		HotKey firstPageRun = new HotKey();
		try (BufferedReader read = new BufferedReader(
				new InputStreamReader(HotKeyDemo.class.getClassLoader().getResourceAsStream("stopwords_zh.txt"),
						Charset.forName("UTF-8")));) {
			String line = null;
			while ((line = read.readLine()) != null) {
				if (!line.isEmpty()) {
					FilterModifWord.insertStopWord(line.trim());
				}
			}
		} catch (IOException e) {
			logger.error(LogbackUtil.expection2Str(e));
		}

		FilterModifWord.insertStopNatures("m");
		FilterModifWord.insertStopNatures("r");
		FilterModifWord.insertStopNatures("o");
		FilterModifWord.insertStopNatures("d");

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
		current = TimeUtils.transCurrentTime(current, 0, 0, 0, 1);

		for (int i = 0; i < 180; i++) {
			long hours = TimeUtils.transCurrentTime(current, 0, 0, 0, -i);
			Multiset<String> counts = getOneHourHotKeys(hours);
			Map<String, Integer> hotKeys = getTopNHotKey(counts, 20);
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
		QueryParams params = new QueryParams();
		params.setQ("*");
		long current = milliTime;
		long last = TimeUtils.transCurrentTime(current, 0, 0, 0, -1);

		params.setRangeFiled("timestamp");
		params.setRangeStart(TimeUtils.transToCommonDateStr(last));
		params.setRangeEnd(TimeUtils.transToCommonDateStr(current));
		QueryResult result = ESQueryCore.getInstance().hotkey(params);
		params.setSize(1000);
		countHotKeys(result, counts);
		long numFound = result.getNumFound();
		for (int i = 1000; i < numFound; i += 1000) {
			params.setFrom(i);
			params.setSize(1000);
			result = ESQueryCore.getInstance().hotkey(params);
			countHotKeys(result, counts);
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

	public void countHotKeys(QueryResult result, Multiset<String> counts) {
		for (RecordInfo doc : result.getSearchHit()) {
			String content = doc.getContent();
			if (content != null) {
				content = content.replaceAll("[http|https]+[://]+[0-9A-Za-z:/[-]_#[?][=][.][&]]*", "");
				Collection<Keyword> keywords = kwc.computeArticleTfidf(content);
				List<String> words = new ArrayList<String>();
				for (Keyword keyword : keywords) {
					words.add(keyword.getName());
				}
				List<Term> recognition = NatureRecognition.recognition(words, 0);
				recognition = FilterModifWord.modifResult(recognition);
				for (Term term : recognition) {
					counts.add(term.getName());
				}
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
