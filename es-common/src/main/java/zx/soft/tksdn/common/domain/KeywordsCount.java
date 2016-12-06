package zx.soft.tksdn.common.domain;

public class KeywordsCount implements Comparable<KeywordsCount> {
	private String keyword;
	private Long count;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "KeywordsCount [keyword=" + keyword + ", count=" + count + "]";
	}

	@Override
	public int compareTo(KeywordsCount o) {
		return  o.getCount().compareTo(this.getCount());

	}

}
