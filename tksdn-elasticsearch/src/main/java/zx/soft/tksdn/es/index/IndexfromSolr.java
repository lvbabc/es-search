package zx.soft.tksdn.es.index;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import zx.soft.tksdn.common.index.RecordInfo;
import zx.soft.tksdn.es.query.ESTransportClient;
import zx.soft.utils.http.HttpClientDaoImpl;
import zx.soft.utils.log.LogbackUtil;
import zx.soft.utils.time.TimeUtils;

public class IndexfromSolr {

	private final ESBulkProcessor processor;
	private final Client client;
	private static final String BASE_URL = "http://192.168.3.55:5920/sentiment/search?q=";
	public static Logger logger = LoggerFactory.getLogger(ESBulkProcessor.class);

	private static List<String> ip = new ArrayList<>();
	private static List<String> pro = new ArrayList<>();
	private static List<String> flo = new ArrayList<>();
	private static List<String> res = new ArrayList<>();
	private static List<String> domain = new ArrayList<>();
	private static int timeIncrement = 0;
	private static Random random = new Random();

	static {
		ip.add("192.168.1.2");
		ip.add("192.168.31.25");
		ip.add("192.168.63.4");
		ip.add("192.168.5.65");
		ip.add("192.168.21.54");
		ip.add("192.168.67.12");
		ip.add("192.168.23.76");

		pro.add("HTTP");
		pro.add("FTP");
		pro.add("SMTP");
		pro.add("HTTPS");
		pro.add("POP3");
		pro.add("TCP/IP");
		pro.add("UDP");
		pro.add("P2P");

		flo.add("上行");
		flo.add("下行");

		res.add("HTML");
		res.add("CSS");
		res.add("图片");
		res.add("语音");
		res.add("视频");
		res.add("FLASH");

		domain.add("facebook.com");
		domain.add("yotube.com");
		domain.add("twitter.com");

	}

	public IndexfromSolr() {
		client = ESTransportClient.getClient();
		processor = new ESBulkProcessor(client);
	}

	public static void main(String[] args) throws ParseException, UnsupportedEncodingException {
		IndexfromSolr indexFromSolr = new IndexfromSolr();
		indexFromSolr.run();
	}

	private void run() throws ParseException, UnsupportedEncodingException {

		int count = 0;
		long endTime = TimeUtils.getZeroHourTime(System.currentTimeMillis());
		long startTime = TimeUtils.transCurrentTime(endTime, 0, 0, 0, -1);

		for (int i = 0; i < 1000; i += 100) {
			StringBuilder sBuilder = new StringBuilder();
			count = i + 100;
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

				try {
					recordInfo = new RecordInfo(info.getString("nickname"), createRandomNum(), createRandomNum(),
							createRandomPort(), createRandomTime(), createRandomIP(), createRandomIP(),
							createRandomPort(), createRandomPort(), createRandomProtocol(), "test",
							info.getString("url"), createRandomFlo(), createRandomRes(), createRandomDoma(),
							createRandomPort(), info.getString("content"), "test");
					result.add(recordInfo);

				} catch (Exception e) {
					logger.error("Exception:{}", LogbackUtil.expection2Str(e));
				}
			}
			processor.doIndex("tekuanfirst", "record", result);
		}
	}

	private static String createRandomProtocol() {
		return pro.get(random.nextInt(pro.size()));
	}

	private static String createRandomDoma() {
		return domain.get(random.nextInt(domain.size()));
	}

	private static String createRandomFlo() {
		return flo.get(random.nextInt(flo.size()));
	}

	private static String createRandomRes() {
		return res.get(random.nextInt(res.size()));
	}

	private static String createRandomIP() {
		return ip.get(random.nextInt(ip.size()));
	}

	private static Date createRandomTime() {
		return new Date((System.currentTimeMillis() / 1000 + (timeIncrement++) * 4) * 1000);
	}

	private static String createRandomPort() {
		return Integer.toString(random.nextInt(6000));
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
