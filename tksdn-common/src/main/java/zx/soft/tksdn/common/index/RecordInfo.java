package zx.soft.tksdn.common.index;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POST的数据类，注意：id需要md5，时间都是10位的数字时间戳，精确到秒的
 *
 * @author wanggang
 *
 */
public class RecordInfo {

	private String id;
	private String username;
	private String identity_id;
	private String phone_num;
	private String ICCID;
	private Date timestamp;
	private String src_ip;
	private String des_ip;
	private String src_port;
	private String des_port;
	private String protocol_type;
	private String header;
	private String url = "";

	private String flow_type;
	private String resource_type;
	private String domain_name;
	private String size;
	private String content = "";
	private String title = "";

	@Override
	public String toString() {
		return "RecordInfo [username=" + username + ", identity_id=" + identity_id + ", phone_num=" + phone_num
				+ ", ICCID=" + ICCID + ", timestamp=" + timestamp + ", src_ip=" + src_ip + ", des_ip=" + des_ip
				+ ", src_port=" + src_port + ", des_port=" + des_port + ", protocol_type=" + protocol_type + ", header="
				+ header + ", url=" + url + ", flow_type=" + flow_type + ", resource_type=" + resource_type
				+ ", domain_name=" + domain_name + ", size=" + size + ", content=" + content + ", title=" + title + "]";
	}

	public RecordInfo(String username, String identity_id, String phone_num, String iCCID, Date timestamp,
			String src_ip, String des_ip, String src_port, String des_port, String protocol_type, String header,
			String url, String flow_type, String resource_type, String domain_name, String size, String content,
			String title) {
		super();
		this.username = username;
		this.identity_id = identity_id;
		this.phone_num = phone_num;
		this.ICCID = iCCID;
		this.timestamp = timestamp;
		this.src_ip = src_ip;
		this.des_ip = des_ip;
		this.src_port = src_port;
		this.des_port = des_port;
		this.protocol_type = protocol_type;
		this.header = header;
		this.url = url;
		this.flow_type = flow_type;
		this.resource_type = resource_type;
		this.domain_name = domain_name;
		this.size = size;
		this.content = content;
		this.title = title;
	}

	public void setField(Map<String, Object> test) {

		if (test.get("content") != null) {
			setContent(test.get("content").toString());
		}
		if (test.get("keyword") != null) {

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIdentity_id() {
		return identity_id;
	}

	public void setIdentity_id(String identity_id) {
		this.identity_id = identity_id;
	}

	public String getPhone_num() {
		return phone_num;
	}

	public void setPhone_num(String phone_num) {
		this.phone_num = phone_num;
	}

	@JsonProperty("ICCID")
	public String getICCID() {
		return ICCID;
	}

	public void setICCID(String iCCID) {
		ICCID = iCCID;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getSrc_ip() {
		return src_ip;
	}

	public void setSrc_ip(String src_ip) {
		this.src_ip = src_ip;
	}

	public String getDes_ip() {
		return des_ip;
	}

	public void setDes_ip(String des_ip) {
		this.des_ip = des_ip;
	}

	public String getSrc_port() {
		return src_port;
	}

	public void setSrc_port(String src_port) {
		this.src_port = src_port;
	}

	public String getDes_port() {
		return des_port;
	}

	public void setDes_port(String des_port) {
		this.des_port = des_port;
	}

	public String getProtocol_type() {
		return protocol_type;
	}

	public void setProtocol_type(String protocol_type) {
		this.protocol_type = protocol_type;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFlow_type() {
		return flow_type;
	}

	public void setFlow_type(String flow_type) {
		this.flow_type = flow_type;
	}

	public String getResource_type() {
		return resource_type;
	}

	public void setResource_type(String resource_type) {
		this.resource_type = resource_type;
	}

	public String getDomain_name() {
		return domain_name;
	}

	public void setDomain_name(String domain_name) {
		this.domain_name = domain_name;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
