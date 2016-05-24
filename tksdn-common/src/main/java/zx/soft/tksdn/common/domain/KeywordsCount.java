package zx.soft.tksdn.common.domain;

public class KeywordsCount {
	private String keyword;
	private long count;
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	@Override
	public String toString() {
		return "KeywordsCount [keyword=" + keyword + ", count=" + count + "]";
	}

}
