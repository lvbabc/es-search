package zx.soft.tksdn.es.domain;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket;

import zx.soft.tksdn.common.index.BrowsingRecord;

/**
 * 查询结果类
 *
 * @author lvbing
 *
 */
public class QueryResult {

	private String tag;

	private long numFound;
	private long QTime;
	private List<BrowsingRecord> searchHit = new ArrayList<>();
	private Object[] sort = null;

	private List<SimpleAggInfo> agg = null;
	private List<Bucket> dateAgg = null;

	public List<BrowsingRecord> getSearchHit() {
		return searchHit;
	}

	public void setSearchHit(List<BrowsingRecord> searchHit) {
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

	public Object[] getSort() {
		return sort;
	}

	public void setSort(Object[] sort) {
		this.sort = sort;
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
