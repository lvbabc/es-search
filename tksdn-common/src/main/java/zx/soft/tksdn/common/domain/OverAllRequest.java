package zx.soft.tksdn.common.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import zx.soft.utils.json.JsonUtils;

/**
 * 综合检索请求
 * @author lvbing
 */
public class OverAllRequest {

	private String key = "";
	private String timestampstart = "";
	private String timestampend = "";
	private List<String> protocol_type = new ArrayList<String>();
	private String flow_type = "";
	private List<String> resource_type = new ArrayList<String>();
	private String phone_num = "";
	//  单页显示条数
	private int size = 10;
	//  分页起始
	private int from = 0;

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(OverAllRequest.class).add("key", key).add("timestampstart", timestampstart)
				.add("timestampend", timestampend).add("protocol_type", protocol_type).add("flow_type", flow_type)
				.add("resource_type", resource_type).add("phone_num", phone_num).toString();
	}

	public static void main(String[] args) {
		OverAllRequest request = new OverAllRequest();
		request.setSize(100);
		String str = JsonUtils.toJsonWithoutPretty(request);
		System.out.println(str);
		str = "{\"key\":\"张三\",\"timestampstart\":\"342425188545650213\",\"timestampend\":\"15456784512\","
				+ "\"resource_type\":[\"sds\"],\"phone_num\":\"12345647895\"}";
		OverAllRequest request2 = JsonUtils.getObject(str, OverAllRequest.class);
		System.out.println(JsonUtils.toJsonWithoutPretty(request2));
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTimestampstart() {
		return timestampstart;
	}

	public void setTimestampstart(String timestampstart) {
		this.timestampstart = timestampstart;
	}

	public String getTimestampend() {
		return timestampend;
	}

	public void setTimestampend(String timestampend) {
		this.timestampend = timestampend;
	}

	public String getPhone_num() {
		return phone_num;
	}

	public void setPhone_num(String phone_num) {
		this.phone_num = phone_num;
	}

	public List<String> getProtocol_type() {
		return protocol_type;
	}

	public void setProtocol_type(List<String> protocol_type) {
		this.protocol_type = protocol_type;
	}

	public String getFlow_type() {
		return flow_type;
	}

	public void setFlow_type(String flow_type) {
		this.flow_type = flow_type;
	}

	public List<String> getResource_type() {
		return resource_type;
	}

	public void setResource_type(List<String> resource_type) {
		this.resource_type = resource_type;
	}

	@JsonProperty
	public int getSize() {
		return size;
	}

	@JsonIgnore
	public void setSize(int size) {
		this.size = size;
	}

	@JsonProperty
	public int getFrom() {
		return from;
	}

	@JsonIgnore
	public void setFrom(int from) {
		this.from = from;
	}
}
