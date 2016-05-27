package zx.soft.tksdn.common.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import zx.soft.utils.json.JsonUtils;

/**
 * POST的索引数据
 *
 * @author lvbing
 *
 */
public class PostData implements Serializable {

	private static final long serialVersionUID = 3183580989697121542L;

	private int num;
	private List<RecordInfo> records;

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public List<RecordInfo> getRecords() {
		return records;
	}

	public void setRecords(List<RecordInfo> records) {
		this.records = records;
	}

	public static void main(String[] args) {
		//  2014-12-28 16:33:47    1419755627695
		RecordInfo recordInfo = new RecordInfo();
//		recordInfo.setId("sentiment");
		List<RecordInfo> records = new ArrayList<>();
		records.add(recordInfo);
		PostData data = new PostData();
		data.setNum(1);
		data.setRecords(records);
		System.out.println(JsonUtils.toJsonWithoutPretty(data));
	}

}
