package zx.soft.tksdn.es.index;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import zx.soft.tksdn.common.index.RecordInfo;
import zx.soft.tksdn.es.query.ESTransportClient;
import zx.soft.utils.http.HttpClientDaoImpl;
import zx.soft.utils.log.LogbackUtil;

public class IndexfromSolr {

	private final ESBulkProcessor processor;
	private final Client client;
	private static final String BASE_URL = "http://192.168.32.13:5920/sentiment/search?q=";
	public static Logger logger = LoggerFactory.getLogger(ESBulkProcessor.class);

	public IndexfromSolr() {
		client = ESTransportClient.getClient();
		processor = new ESBulkProcessor(client);
	}

	public static void main(String[] args) throws ParseException {
		IndexfromSolr indexFromSolr = new IndexfromSolr();
		indexFromSolr.run();
	}

	private void run() throws ParseException {

		int count = 0;
		for (int i = 0; i < 100000000; i += 100) {
			StringBuilder sBuilder = new StringBuilder();
			count = i + 100;
			sBuilder.append(BASE_URL + "*:*&start=" + i + "&rows=" + count + "&fq=platform:3");
			String response = new HttpClientDaoImpl().doGet(sBuilder.toString());

			JSONObject obj = JSONObject.fromObject(response);
			JSONArray jsonArray = obj.getJSONArray("results");
			List<RecordInfo> result = new ArrayList<>();
			for (int j = 0; j < jsonArray.size(); j++) {
				RecordInfo recordInfo = new RecordInfo();
				JSONObject info = jsonArray.getJSONObject(j);
				logger.info("result " + j + " :" + jsonArray.get(j));
				try {
					recordInfo.setId(info.getString("id") == null ? "" : info.getString("id"));
					recordInfo.setIp_addr(info.getString("ip") == null ? "" : info.getString("ip"));
					recordInfo.setUrl(info.getString("url") == null ? "" : info.getString("url"));
					recordInfo.setTimestamp(info.getString("timestamp") == null ? "" : info.getString("timestamp"));
					recordInfo.setLasttime(info.getString("lasttime") == null ? "" : info.getString("lasttime"));
					recordInfo.setContent(info.getString("content") == null ? "" : info.getString("content"));
					recordInfo.setTitle("test");
					recordInfo.setType("test");
					recordInfo.setKeyword("test");
					processor.doIndex("tk", "record", recordInfo);
					//					result.add(recordInfo);

				} catch (Exception e) {
					logger.error("Exception:{}", LogbackUtil.expection2Str(e));
				}
			}
			//			processor.doIndex("test", "record", result);
			//			processor.closeESBulkProcessor();
		}
	}

}
