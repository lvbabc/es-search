package zx.soft.elasticsearch.api.core;

import java.util.Date;

/**
 * 上网记录字段及类型定义
 * @author xuwenjaun
 *
 */
public class BrowsingRecord {

	private String src_ip = "";//源ip
	private int src_port;//源端口
	private String des_ip = "";//目的ip
	private int des_port;//目的端口
	private String service = "";//协议
	private Date generate_time;//数据产生时间
	private Date index_time;//数据索引时间
	private String content = "";//内容
	private String keyword = "";//关键词

	public BrowsingRecord(String src_ip, int src_port, String des_ip, int des_port, String service, Date generate_time,
			Date index_time, String content, String keyword) {
		super();
		this.src_ip = src_ip;
		this.src_port = src_port;
		this.des_ip = des_ip;
		this.des_port = des_port;
		this.service = service;
		this.generate_time = generate_time;
		this.index_time = index_time;
		this.content = content;
		this.keyword = keyword;
	}

	public String getSrc_ip() {
		return src_ip;
	}

	public int getSrc_port() {
		return src_port;
	}

	public String getDes_ip() {
		return des_ip;
	}

	public int getDes_port() {
		return des_port;
	}

	public String getService() {
		return service;
	}

	public Date getGenerate_time() {
		return generate_time;
	}

	public Date getIndex_time() {
		return index_time;
	}

	public String getContent() {
		return content;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setSrc_ip(String src_ip) {
		this.src_ip = src_ip;
	}

	public void setSrc_port(int src_port) {
		this.src_port = src_port;
	}

	public void setDes_ip(String des_ip) {
		this.des_ip = des_ip;
	}

	public void setDes_port(int des_port) {
		this.des_port = des_port;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setGenerate_time(Date generate_time) {
		this.generate_time = generate_time;
	}

	public void setIndex_time(Date index_time) {
		this.index_time = index_time;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
