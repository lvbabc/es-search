package zx.soft.tksdn.es.domain;

import java.util.HashMap;

/**
 * 简单的分片信息
 * 
 * @author wanggang
 *
 */
public class SimpleAggInfo {

	private String name;
	private HashMap<String, Long> values = new HashMap<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, Long> getValues() {
		return values;
	}

	public void setValues(HashMap<String, Long> values) {
		this.values = values;
	}

}
