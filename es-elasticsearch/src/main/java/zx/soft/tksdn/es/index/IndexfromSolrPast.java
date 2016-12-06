package zx.soft.tksdn.es.index;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import zx.soft.tksdn.common.index.RecordInfo;
import zx.soft.tksdn.es.utils.ESBulkProcessor;
import zx.soft.tksdn.es.utils.ESTransportClient;
import zx.soft.utils.http.HttpClientDaoImpl;
import zx.soft.utils.log.LogbackUtil;
import zx.soft.utils.time.TimeUtils;

public class IndexfromSolrPast {

	private final ESBulkProcessor processor;
	private final Client client;
	private static final String BASE_URL = "http://192.168.3.55:5920/sentiment/search?q=";
	public static Logger logger = LoggerFactory.getLogger(ESBulkProcessor.class);
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static List<String> ip = new ArrayList<>();
	private static Map<String, Integer> pro = new HashMap<String, Integer>();
	private static List<String> flo = new ArrayList<>();
	private static Map<String, Integer> res = new HashMap<String, Integer>();
	private static List<String> domain = new ArrayList<>();
	private static List<String> header = new ArrayList<>();
	private static Random random = new Random();

	static {
		ip.add("192.168.1.2");
		ip.add("192.168.31.25");
		ip.add("192.168.63.4");
		ip.add("192.168.5.65");
		ip.add("192.168.21.54");
		ip.add("192.168.67.12");
		ip.add("192.168.23.76");

		pro.put("HTTP", 8);
		pro.put("FTP", 6);
		pro.put("SMTP", 1);
		pro.put("HTTPS", 7);
		pro.put("POP3", 2);
		pro.put("TCP/IP", 4);
		pro.put("UDP", 3);
		pro.put("P2P", 5);

		flo.add("上行");
		flo.add("下行");

		res.put("HTML", 6);
		res.put("CSS", 5);
		res.put("图片", 4);
		res.put("语音", 1);
		res.put("视频", 3);
		res.put("FLASH", 2);

		domain.add("facebook.com");
		domain.add("yotube.com");
		domain.add("twitter.com");
		domain.add("tumblr.com");
		domain.add("google.com");

		header.add("Content-type:text/html;charset=utf-8");
		header.add("Content-Type: application/json; charset=UTF-8");
	}

	public IndexfromSolrPast() {
		client = ESTransportClient.getClient();
		processor = new ESBulkProcessor(client);
	}

	public static void main(String[] args) throws ParseException, UnsupportedEncodingException {
		IndexfromSolrPast indexFromSolr = new IndexfromSolrPast();
		indexFromSolr.run();
	}

	private void run() throws ParseException, UnsupportedEncodingException {
		long now = TimeUtils.getZeroHourTime(System.currentTimeMillis());
		for (int timeNum = 0; timeNum < 720; timeNum++) {
			int count = 0;
			long endTime = TimeUtils.transCurrentTime(now, 0, 0, 0, -timeNum);
			long startTime = TimeUtils.transCurrentTime(now, 0, 0, 0, -1 - timeNum);

			for (int i = 0; i < 1000; i += 1000) {
				StringBuilder sBuilder = new StringBuilder();
				count = 1000;
				sBuilder.append(BASE_URL + "*:*&start=" + i + "&rows=" + count + "&fq=timestamp:"
						+ URLEncoder.encode("[", "utf-8") + TimeUtils.transToSolrDateStr(startTime)
						+ URLEncoder.encode(" ", "utf-8") + "TO" + URLEncoder.encode(" ", "utf-8")
						+ TimeUtils.transToSolrDateStr(endTime) + URLEncoder.encode("]", "utf-8"));
				logger.info(sBuilder.toString());
				String response = new HttpClientDaoImpl().doGet(sBuilder.toString());

				JSONObject obj = JSONObject.fromObject(response);
				JSONArray jsonArray = obj.getJSONArray("results");
				List<RecordInfo> result = new ArrayList<>();
				for (int j = 0; j < jsonArray.size(); j++) {
					RecordInfo recordInfo = null;
					JSONObject info = jsonArray.getJSONObject(j);
					String title = "";
					if (!info.containsKey("content") || (!info.containsKey("nickname")) || (!info.containsKey("url"))
							|| (!info.containsKey("timestamp"))) {
						continue;
					}
					if (info.getString("content").length() < 20) {
						continue;
					}
					if (info.containsKey("title")) {
						title = info.getString("title");
					} else {
						title = info.getString("content").substring(0, 15).trim();
					}
					try {
						String time = info.getString("timestamp");
						Date date = format.parse(time);
						//						time = TimeUtils.transStrToCommonDateStr(date.toString(), 8);
						//						date = format.parse(time);
						recordInfo = new RecordInfo(info.getString("id"), info.getString("nickname"), createRandomNum(),
								createRandomNum(), createRandomPort(), date, createRandomIP(), createRandomIP(),
								createRandomPort(), createRandomPort(), createRandomProtocol(), createRandomHeader(),
								info.getString("url"), createRandomFlo(), createRandomRes(), createRandomDoma(),
								createRandomPort(), info.getString("content").trim(), title);
						result.add(recordInfo);
					} catch (Exception e) {
						logger.error("Exception:{}", LogbackUtil.expection2Str(e));
					}
				}
				processor.doIndex("tekuan", "record", result);
			}
		}
	}

	private static String createRandomProtocol() {
		Integer sum = 0;
		for (Integer value : pro.values()) {
			sum += value;
		}
		// 从1开始
		Integer rand = new Random().nextInt(sum) + 1;

		for (Map.Entry<String, Integer> entry : pro.entrySet()) {
			rand = rand - entry.getValue();
			// 选中
			if (rand <= 0) {
				String item = entry.getKey();
				return item;
			}
		}
		return null;
	}

	private static String createRandomDoma() {
		return domain.get(random.nextInt(domain.size()));
	}

	private static String createRandomFlo() {
		return flo.get(random.nextInt(flo.size()));
	}

	private static String createRandomRes() {
		Integer sum = 0;
		for (Integer value : res.values()) {
			sum += value;
		}
		// 从1开始
		Integer rand = new Random().nextInt(sum) + 1;

		for (Map.Entry<String, Integer> entry : res.entrySet()) {
			rand = rand - entry.getValue();
			// 选中
			if (rand <= 0) {
				String item = entry.getKey();
				return item;
			}
		}
		return null;
	}

	private static String createRandomIP() {
		return ip.get(random.nextInt(ip.size()));
	}

	private static String createRandomPort() {
		return Integer.toString(random.nextInt(6000));
	}

	private static String createRandomHeader() {
		return header.get(random.nextInt(header.size()));
	}

	private static String createRandomNum() {
		String chars = "0123456789";
		char[] rands = new char[11];
		rands[0] = '1';
		for (int i = 1; i < 11; i++) {
			int rand = (int) (Math.random() * 10);

			rands[i] = chars.charAt(rand);
		}
		String chTOStr = String.valueOf(rands);
		return chTOStr;
	}
}
