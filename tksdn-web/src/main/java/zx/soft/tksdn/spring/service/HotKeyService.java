package zx.soft.tksdn.spring.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import zx.soft.tksdn.common.domain.KeywordsCount;
import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.dao.insight.RiakInsight;
import zx.soft.tksdn.es.query.ESQueryCore;
import zx.soft.tksdn.spring.domain.TrendResult;
import zx.soft.utils.algo.TopN;
import zx.soft.utils.algo.TopN.KeyValue;
import zx.soft.utils.json.JsonUtils;
import zx.soft.utils.time.TimeUtils;

@Service
public class HotKeyService {

	private static RiakInsight riak = null;

	static {
		riak = new RiakInsight();
	}

	private static Logger logger = LoggerFactory.getLogger(HotKeyService.class);

	public Object getHotKey(QueryParams params) {

		long start = System.currentTimeMillis();
		// 获得热门关键词
		TrendResult hotkeyResult = new TrendResult();
		List<KeyValue<String, Integer>> hotKeys = getHotKeys(params);

		for (KeyValue<String, Integer> hotKey : hotKeys) {
			hotkeyResult.countHotWords(hotKey.getKey(), hotKey.getValue());
		}
		hotkeyResult.sortHotKeys();

		logger.info("获得倾向信息耗时: {}ms", System.currentTimeMillis() - start);

		List<Map.Entry<String, Integer>> result = hotkeyResult.getSortedhotKeys();

		List<String> words = new ArrayList<>();
		for (Entry<String, Integer> entry : result) {
			words.add(entry.getKey());
		}
		return getEqual(words, params);//results;
	}

	private List<KeyValue<String, Integer>> getHotKeys(QueryParams params) {
		Multiset<String> hotKeys = HashMultiset.create();
		long endTime = TimeUtils.getZeroHourTime(System.currentTimeMillis());
		long startTime = TimeUtils.transCurrentTime(endTime, 0, 0, -10, 0);
		if (params.getRangeStart() != "") {
			long lTime = TimeUtils.transTimeLong(params.getRangeStart().trim());
			startTime = TimeUtils.getZeroHourTime(lTime);
			long rTime = TimeUtils.transTimeLong(params.getRangeEnd().trim());
			endTime = TimeUtils.getZeroHourTime(rTime);
		}
		List<Map<String, Integer>> hoursHotKeys = new ArrayList<Map<String, Integer>>();

		while (startTime <= endTime) {
			int hour = Integer.parseInt(TimeUtils.timeStrByHour(startTime).split(",")[1]);
			if (hour % 6 == 0) {
				hoursHotKeys.add(getKeys(startTime));
			}
			startTime = TimeUtils.transCurrentTime(startTime, 0, 0, 0, 1);
		}

		for (Map<String, Integer> hourHotKey : hoursHotKeys) {
			for (Entry<String, Integer> entry : hourHotKey.entrySet()) {

				hotKeys.add(entry.getKey(), entry.getValue());
			}
		}
		return TopN.topNOnValue(hotKeys, 50);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Integer> getKeys(long milliSecond) {
		String hourhotkeys = riak.selectHotkeys("hotkeys", TimeUtils.timeStrByHour(milliSecond));
		if (hourhotkeys != null) {
			Map<String, Integer> hotKeys = JsonUtils.getObject(hourhotkeys, Map.class);
			if (hotKeys != null) {
				return hotKeys;
			}
		}
		return new HashMap<>();
	}

	private List<KeywordsCount> getEqual(List<String> keywords, QueryParams params) {
		params.setRangeFiled("timestamp");
		long endTime = TimeUtils.getZeroHourTime(System.currentTimeMillis());
		long startTime = TimeUtils.transCurrentTime(endTime, 0, -1, 0, 0);
		if (params.getRangeStart() != "") {
			long lTime = TimeUtils.transTimeLong(params.getRangeStart().trim());
			startTime = TimeUtils.getZeroHourTime(lTime);
			long rTime = TimeUtils.transTimeLong(params.getRangeEnd().trim());
			endTime = TimeUtils.getZeroHourTime(rTime);
		}
		params.setRangeStart(TimeUtils.transToCommonDateStr(startTime));
		params.setRangeEnd(TimeUtils.transToCommonDateStr(endTime));
		List<KeywordsCount> kCounts = ESQueryCore.getInstance().queryKeywords(keywords, params);
		Collections.sort(kCounts);
		return kCounts.subList(0, params.getCount());
	}
}
