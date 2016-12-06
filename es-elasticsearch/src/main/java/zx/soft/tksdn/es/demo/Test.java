package zx.soft.tksdn.es.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Test {

	// String 可以为任意类型 也可以自定义类型
	static Map<String, Integer> keyChanceMap = new HashMap<String, Integer>();

	static {
		//        keyChanceMap.put("出现比例为10的", 10);
		keyChanceMap.put("出现比例为40的", 4);
		keyChanceMap.put("出现比例为20的", 2);
		keyChanceMap.put("出现比例为10的", 1);
		keyChanceMap.put("出现比例为30的", 3);
	}

	public static void main(String[] args) {
		Map<String, Integer> count = new HashMap<String, Integer>();
		for (int i = 0; i < 100000; i++) {

			String item = chanceSelect(keyChanceMap);

			if (count.containsKey(item)) {
				count.put(item, count.get(item) + 1);
			} else {
				count.put(item, 1);
			}
		}

		for (String id : count.keySet()) {
			System.out.println(id + "\t出现了 " + count.get(id) + " 次");
		}
	}

	public static String chanceSelect(Map<String, Integer> keyChanceMap) {
		if (keyChanceMap == null || keyChanceMap.size() == 0)
			return null;

		Integer sum = 0;
		for (Integer value : keyChanceMap.values()) {
			sum += value;
		}
		// 从1开始
		Integer rand = new Random().nextInt(sum) + 1;

		for (Map.Entry<String, Integer> entry : keyChanceMap.entrySet()) {
			rand = rand - entry.getValue();
			// 选中
			if (rand <= 0) {
				String item = entry.getKey();
				return item;
			}
		}

		return null;
	}
}