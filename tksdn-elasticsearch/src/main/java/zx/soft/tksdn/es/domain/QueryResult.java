package zx.soft.tksdn.es.domain;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket;

import zx.soft.tksdn.common.index.RecordInfo;

/**
 * 查询结果类
 *
 * @author lvbing
 *
 */
public class QueryResult {

	//多线程标志
	private String tag;
	private long numFound;
	private long QTime;
	private List<RecordInfo> searchHit = new ArrayList<>();

	private List<SimpleAggInfo> agg = null;
	private List<Bucket> dateAgg = null;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public List<RecordInfo> getSearchHit() {
		return searchHit;
	}

	public void setSearchHit(List<RecordInfo> searchHit) {
		this.searchHit = searchHit;
	}

	public long getQTime() {
		return QTime;
	}

	public void setQTime(long qTime) {
		this.QTime = qTime;
	}

	public long getNumFound() {
		return numFound;
	}

	public void setNumFound(long numFound) {
		this.numFound = numFound;
	}

	public List<SimpleAggInfo> getAgg() {
		return agg;
	}

	public void setAgg(List<SimpleAggInfo> agg) {
		this.agg = agg;
	}

	public List<Bucket> getDateAgg() {
		return dateAgg;
	}

	public void setDateAgg(List<Bucket> dateAgg) {
		this.dateAgg = dateAgg;
	}

}
