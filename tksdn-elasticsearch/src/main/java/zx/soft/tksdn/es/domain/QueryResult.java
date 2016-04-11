package zx.soft.tksdn.es.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询结果类
 *
 * @author lvbing
 *
 */
public class QueryResult {

	// 多线程的标志 added by donglei
	private String tag;

	private long numFound;
	private long QTime;
	private List<Map<String, Object>> searchHit = new ArrayList<>();
	private final Map<String, Map<String, List<String>>> highlighting = null;


	public List<Map<String, Object>> getSearchHit() {
		return searchHit;
	}

	public void setSearchHit(List<Map<String, Object>> searchHit) {
		this.searchHit = searchHit;
	}

	public long getQTime() {
		return QTime;
	}

	public void setQTime(long qTime) {
		this.QTime = qTime;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getNumFound() {
		return numFound;
	}

	public void setNumFound(long numFound) {
		this.numFound = numFound;
	}

}
