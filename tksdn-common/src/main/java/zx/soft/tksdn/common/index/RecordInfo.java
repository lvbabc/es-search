package zx.soft.tksdn.common.index;

import java.util.Map;

/**
 * POST的数据类，注意：id需要md5，时间都是10位的数字时间戳，精确到秒的
 *
 * @author wanggang
 *
 */
public class RecordInfo {

	private String id = "";
	private String url = "";
	private String title = "";
	private String type = "";
	private String content = "";
	private String timestamp;
	private String lasttime;
	private String keyword = "";
	private String ip_addr = "";

	@Override
	public String toString() {
		return "RecordInfo [id=" + id + ", url=" + url + ", title=" + title + ", type=" + type + ", content=" + content
				+ ", timestamp=" + timestamp + ", lasttime=" + lasttime + ", keyword=" + keyword + ", ip_addr=" + ip_addr + "]";
	}

	public void setField(Map<String, Object> test) {

		if (test.get("content") != null) {
			setContent(test.get("content").toString());
		}
		if (test.get("keyword") != null) {
			setKeyword(test.get("keyword").toString());
		}
	}

	public RecordInfo() {
		//
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getLasttime() {
		return lasttime;
	}

	public void setLasttime(String lasttime) {
		this.lasttime = lasttime;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getIp_addr() {
		return ip_addr;
	}

	public void setIp_addr(String ip_addr) {
		this.ip_addr = ip_addr;
	}


}
