package zx.soft.tksdn.spring.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.dao.insight.RiakInsight;
import zx.soft.tksdn.spring.domain.HotKeyResult;
import zx.soft.tksdn.spring.domain.TrendResult;
import zx.soft.utils.algo.TopN;
import zx.soft.utils.algo.TopN.KeyValue;
import zx.soft.utils.json.JsonUtils;
import zx.soft.utils.threads.AwesomeThreadPool;
import zx.soft.utils.time.TimeUtils;

/**
 * @author lvbing
 */
@Service
public class TrendService {

	private static RiakInsight riak = null;

	static {
		riak = new RiakInsight();
	}

	private static Logger logger = LoggerFactory.getLogger(TrendService.class);

	public Object getTrendInfos(final QueryParams params) {
		long start = System.currentTimeMillis();
		// 获得热门关键词
		Callable<TrendResult> hotkeyCall = new Callable<TrendResult>() {

			@Override
			public TrendResult call() throws Exception {
				TrendResult hotkeyResult = new TrendResult();
				List<KeyValue<String, Integer>> hotKeys = getHotKeys(params);
				for (KeyValue<String, Integer> hotKey : hotKeys) {
					hotkeyResult.countHotWords(hotKey.getKey(), hotKey.getValue());
				}
				hotkeyResult.sortHotKeys();
				return hotkeyResult;
			}
		};
		FutureTask<TrendResult> hotkeyTask = new FutureTask<>(hotkeyCall);
		new Thread(hotkeyTask).start();

		TrendResult hotkey = new TrendResult();
		try {
			hotkey = hotkeyTask.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage());
		}

		logger.info("获得倾向信息耗时: {}ms", System.currentTimeMillis() - start);

		List<Map.Entry<String, Integer>> result = hotkey.getSortedhotKeys();
		List<HotKeyResult> results = new ArrayList<HotKeyResult>();
		for (Entry<String, Integer> entry : result) {
			HotKeyResult hotKeyResult = new HotKeyResult();
			hotKeyResult.setKeyword(entry.getKey());
			hotKeyResult.setCount(entry.getValue());
			results.add(hotKeyResult);
		}
		return results;
	}

	private List<KeyValue<String, Integer>> getHotKeys(QueryParams params) {
		Multiset<String> hotKeys = HashMultiset.create();
		long endTime = TimeUtils.getZeroHourTime(System.currentTimeMillis());
		long startTime = TimeUtils.transCurrentTime(endTime, 0, -1, 0, 0);
		if (params.getRangeStart() != "") {
			long lTime = TimeUtils.transTimeLong(params.getRangeStart().trim());
			startTime = TimeUtils.getZeroHourTime(lTime);
			long rTime = TimeUtils.transTimeLong(params.getRangeEnd().trim());
			endTime = TimeUtils.getZeroHourTime(rTime);
		}
		List<Callable<Map<String, Integer>>> calls = new ArrayList<Callable<Map<String, Integer>>>();
		while (startTime < endTime) {
			calls.add(new RiakCallable(startTime));
			startTime = TimeUtils.transCurrentTime(startTime, 0, 0, 0, 1);
		}
		List<Map<String, Integer>> hoursHotKeys = AwesomeThreadPool.runCallables(5, calls);
		for (Map<String, Integer> hourHotKey : hoursHotKeys) {
			for (Entry<String, Integer> entry : hourHotKey.entrySet()) {
				hotKeys.add(entry.getKey(), entry.getValue());
			}
		}
		return TopN.topNOnValue(hotKeys, params.getCount());
	}

	public static class RiakCallable implements Callable<Map<String, Integer>> {
		private final long milliSecond;

		public RiakCallable(long milliSecond) {
			this.milliSecond = milliSecond;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Integer> call() throws Exception {
			String hourhotkeys = riak.selectHotkeys("hotkeys", TimeUtils.timeStrByHour(milliSecond));
			if (hourhotkeys != null) {
				Map<String, Integer> hotKeys = JsonUtils.getObject(hourhotkeys, Map.class);
				if (hotKeys != null) {
					return hotKeys;
				}
			}
			return new HashMap<>();
		}

	}
}
