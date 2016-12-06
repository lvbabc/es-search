package zx.soft.tksdn.es.hotkey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.util.FilterModifWord;

import zx.soft.tksdn.common.index.RecordInfo;
import zx.soft.tksdn.dao.insight.RiakInsight;
import zx.soft.utils.json.JsonUtils;

public class HotKeyDemo {

	public static void main(String[] args) {
		RiakInsight insight = new RiakInsight();
		//		String docs = insight.selectHotkeys("0", "2015-11-10,10");

		String docs = "{\"id\": \"AVVNHPIhpvss-ZtBG2Zy\",\"username\": \"丁一珂\",\"identity_id\": \"15085671864\",\"phone_num\": "
				+ "\"19068052109\",\"timestamp\": \"2016-06-16T07:45:06\",\"src_ip\": \"192.168.63.4\",\"des_ip\": \"192.168.67.12\","
				+ "\"src_port\": \"4731\",\"des_port\": \"1653\",\"protocol_type\": \"TCP/IP\",\"header\": \"test\","
				+ "\"url\": \"http//:weibo.com/1995437205/DuTlB2wEe\",\"flow_type\": \"下行\",\"resource_type\": \"图片\","
				+ "\"domain_name\": \"twitter.com\""
				+ ",\"size\": \"48\",\"content\": \"[拍照]\",\"title\": \"test\",\"ICCID\": \"4349\"}";

		List<RecordInfo> solrDocs = JsonUtils.parseJsonArray(docs, RecordInfo.class);
		String strs = "";
		for (RecordInfo solrDoc : solrDocs)

		{
			//			strs += solrDoc.get("content");
		}
		strs = "Somerset House的溜冰場真的是美翻！室外溜冰，提早過聖誕！哈哈哈哈！我跟爸爸又來、跟朋友又來、幾乎天天都來！明天看看錄不錄得了視頻給你們，嘿～";

		KeyWordComputer kwc = new KeyWordComputer(20);
		Collection<Keyword> keywords = kwc.computeArticleTfidf(strs);
		// [调查, 中国, 我们, 公司, 官员, 侦探, 他们, 人员, 基因, 问题, 朔州, 广西, 忻州, 死亡, 肖文荪, 私家, 合法, 进行, 一些, 迁徙]
		// [工资, 劳动, 一个, 记者, 没有, 我们, 於华平, 政府, 问题, 农民, 公司, 奖金, 人民, 合肥, 工作, 安徽, 社会, 单位, 他们, 自己]
		//		List<String> keywords = HanLP.extractKeyword(strs, 20);
		// [调查, 中国, 广西, 国家, 迁徙, 基因, 进入, 人员, 问题, 官员, 今天, 进行, 祖先, 忻州, 提供, 朔州, 死亡, 工作, 测试, 公司]
		// [劳动, 没有, 农民, 安徽, 政府, 东风, 汽车, 合肥, 记者, 问题, 崇祯, 人民, 饿死, 工资, 当时, 已经, 工作, 今年, 中国, 社会]
		System.out.println(keywords);

		List<String> words = new ArrayList<String>();
		for (Keyword keyword : keywords)

		{
			words.add(keyword.getName());
		}

		try (

				BufferedReader read = new BufferedReader(
						new InputStreamReader(HotKeyDemo.class.getClassLoader().getResourceAsStream("stopwords_zh.txt"),
								Charset.forName("UTF-8")));)

		{
			String line = null;
			while ((line = read.readLine()) != null) {
				if (!line.isEmpty()) {
					FilterModifWord.insertStopWord(line);
				}
			}
		} catch (

		IOException e)

		{
			e.printStackTrace();
		}

		FilterModifWord.insertStopNatures("m");
		FilterModifWord.insertStopNatures("r");
		FilterModifWord.insertStopNatures("o");
		FilterModifWord.insertStopNatures("d");

		List<Term> recognition = NatureRecognition.recognition(words, 0);
		System.out.println(recognition);
		recognition = FilterModifWord.modifResult(recognition);

		System.out.println(recognition);

		Object q = null;
		String a = (String) q;
		if (a == null)

		{
			System.out.println("a== null");
		}

		System.exit(0);

	}
}
