package zx.soft.tksdn.spring.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.primitives.Ints;

import zx.soft.utils.string.StringUtils;

public class TrendResult {
	private List<Map.Entry<String, Integer>> sortedhotKeys = new ArrayList<>();
	@JsonIgnore
	private final Multiset<String> hotkeys = HashMultiset.create();

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(TrendResult.class).add("sortedhotKeys", sortedhotKeys)
				.toString();
	}

	public List<Map.Entry<String, Integer>> getSortedhotKeys() {
		return sortedhotKeys;
	}

	public void setSortedhotKeys(List<Map.Entry<String, Integer>> sortedhotKeys) {
		this.sortedhotKeys = sortedhotKeys;
	}

	public void countHotWords(String key, int count) {
		if (!StringUtils.isEmpty(key)) {
			hotkeys.add(key, count);
		}
	}

	public void sortHotKeys() {
		Map<String, Integer> counts = Maps.newHashMap();

		for (Entry<String> entry : hotkeys.entrySet()) {
			counts.put(entry.getElement(), entry.getCount());
		}
		for (Map.Entry<String, Integer> keyvalue : counts.entrySet()) {
			sortedhotKeys.add(keyvalue);
		}
		Collections.sort(sortedhotKeys, new Comparator<Map.Entry<String, Integer>>() {

			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return -Ints.compare(o1.getValue(), o2.getValue());
			}
		});
		if (sortedhotKeys.size() > 20) {
			this.sortedhotKeys = sortedhotKeys.subList(0, 20);
		}
	}


}
