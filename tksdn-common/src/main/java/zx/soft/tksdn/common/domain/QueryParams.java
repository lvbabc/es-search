package zx.soft.tksdn.common.domain;

import java.util.Arrays;

/**
 * 查询参数类
 *
 * @author lvbing
 *
 */
public class QueryParams implements Cloneable {

	//索引参数
	private String indexName = "tekuanfirst";
	private String indexType = "record";
	private String preferenceType = "";
	private String searchType = "";
	private String[] includes = null;

	//默认查询,参数中不带字段,字段为默认设置
	private String q = "*";

	//范围查询--rangeQuery参数
	private String rangeFiled = "";
	private String rangeStart = "";
	private String rangeEnd = "";
	private String timeZone = "+08:00";

	//bool 查询 boolQuery --未确定具体形式,待以后更改
	private String bQ = "";

	//聚合参数   termsAgg,dateHistAgg的值为字段名
	//termAgg用于普通数据聚合,dateHistAgg用于日期数据聚合,dateInterval后要加单位,如1d(1天),3M(3个月)
	private String termsAgg = "";
	private String dateHistAgg = "";
	private String dateInterval = null;

	private String term = "";
	private String value = "";

	//高亮
	private String hlfl = null;;

	private String sort = "";

	//分页
	private int from = 0;
	private int size = 10;

	public QueryParams() {
		//
	}

	@Override
	public QueryParams clone() {
		QueryParams params = null;
		try {
			params = (QueryParams) super.clone();
			//			params.facetDate = (HashMap<String, String>)this.facetDate.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return params;
	}

	@Override
	public String toString() {
		return "QueryParams [indexName=" + indexName + ", indexType=" + indexType + ", preferenceType=" + preferenceType
				+ ", searchType=" + searchType + ", includes=" + Arrays.toString(includes) + ", q=" + q + ", termsAgg="
				+ termsAgg + ", dateHistAgg=" + dateHistAgg + ", term=" + term + ", value=" + value + ", hlfl=" + hlfl
				+ ", rangeFiled=" + rangeFiled + ", rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd + ", timeZone="
				+ timeZone + ", sort=" + sort + ", from=" + from + ", size=" + size + "]";
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	public String getPreferenceType() {
		return preferenceType;
	}

	public void setPreferenceType(String preferenceType) {
		this.preferenceType = preferenceType;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	public String[] getIncludes() {
		return includes;
	}

	public void setIncludes(String[] includes) {
		this.includes = includes;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String getRangeFiled() {
		return rangeFiled;
	}

	public void setRangeFiled(String rangeFiled) {
		this.rangeFiled = rangeFiled;
	}

	public String getRangeStart() {
		return rangeStart;
	}

	public void setRangeStart(String rangeStart) {
		this.rangeStart = rangeStart;
	}

	public String getRangeEnd() {
		return rangeEnd;
	}

	public void setRangeEnd(String rangeEnd) {
		this.rangeEnd = rangeEnd;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getHlfl() {
		return hlfl;
	}

	public void setHlfl(String hlfl) {
		this.hlfl = hlfl;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTermsAgg() {
		return termsAgg;
	}

	public void setTermsAgg(String termsAgg) {
		this.termsAgg = termsAgg;
	}

	public String getDateHistAgg() {
		return dateHistAgg;
	}

	public void setDateHistAgg(String dateHistAgg) {
		this.dateHistAgg = dateHistAgg;
	}

	public String getDateInterval() {
		return dateInterval;
	}

	public void setDateInterval(String dateInterval) {
		this.dateInterval = dateInterval;
	}

	public String getbQ() {
		return bQ;
	}

	public void setbQ(String bQ) {
		this.bQ = bQ;
	}

}
