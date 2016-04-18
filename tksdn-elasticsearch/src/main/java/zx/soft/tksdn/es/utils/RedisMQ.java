package zx.soft.tksdn.es.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import zx.soft.tksdn.common.domain.SentimentConstant;
import zx.soft.tksdn.common.index.RecordInfo;
import zx.soft.utils.config.ConfigUtil;
import zx.soft.utils.json.JsonUtils;
import zx.soft.utils.log.LogbackUtil;

public class RedisMQ {

	private static Logger logger = LoggerFactory.getLogger(RedisMQ.class);

	private static JedisPool pool;

	private static final ObjectMapper OBJECT_MAPPER = JsonUtils.getObjectMapper();

	public RedisMQ() {
		init();
	}

	private void init() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(256);
		poolConfig.setMinIdle(64);
		poolConfig.setMaxWaitMillis(10_000);
		poolConfig.setMaxTotal(1024);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTimeBetweenEvictionRunsMillis(30000);
		Properties props = ConfigUtil.getProps("cache-config.properties");
		pool = new JedisPool(poolConfig, props.getProperty("redis.mq.server"), Integer.parseInt(props
				.getProperty("redis.mq.port")), 30_000, props.getProperty("redis.password"));
	}

	public synchronized static Jedis getJedis() {
		try {
			if (pool != null) {
				return pool.getResource();
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
			return null;
		}
	}

	/**
	 * 添加数据，members不能为空
	 */
	public synchronized void addRecord(String... members) {
		Jedis jedis = getJedis();
		if (jedis == null) {
			return;
		}
		try {
			jedis.sadd(SentimentConstant.SENTIMENT_CACHE_KEY, members);
		} catch (Exception e) {
			logger.error("Exception:{},Records'size={}.", LogbackUtil.expection2Str(e), members.length);
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
				jedis = null;
			}
		} finally {
			// 这里很重要，一旦拿到的jedis实例使用完毕，必须要返还给池中
			if (jedis != null && jedis.isConnected()) {
				pool.returnResource(jedis);
			}
		}
	}

	/**
	 * 添加数据，members不能为空
	 */
	public synchronized void addRecord(String key, String... members) {
		Jedis jedis = getJedis();
		if (jedis == null) {
			return;
		}
		try {
			jedis.sadd(key, members);
		} catch (Exception e) {
			logger.error("Exception:{},Records'size={}.", LogbackUtil.expection2Str(e), members.length);
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
				jedis = null;
			}
		} finally {
			// 这里很重要，一旦拿到的jedis实例使用完毕，必须要返还给池中
			if (jedis != null && jedis.isConnected()) {
				pool.returnResource(jedis);
			}
		}
	}

	/**
	 * 获取集合大小
	 */
	public synchronized long getSetSize() {
		long result = 0L;
		Jedis jedis = getJedis();
		if (jedis == null) {
			return result;
		}
		try {
			// 在事务和管道中不支持同步查询
			result = jedis.scard(SentimentConstant.SENTIMENT_CACHE_KEY).longValue();
		} catch (Exception e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
				jedis = null;
			}
		} finally {
			if (jedis != null && jedis.isConnected()) {
				pool.returnResource(jedis);
			}
		}
		return result;
	}

	/**
	 * 获取数据
	 */
	public synchronized List<String> getRecords() {
		List<String> records = new ArrayList<>();
		Jedis jedis = getJedis();
		if (jedis == null) {
			return records;
		}
		try {
			String value = jedis.spop(SentimentConstant.SENTIMENT_CACHE_KEY);
			//			while (value != null) {
			for (int i = 0; (i < 10000) && (value != null); i++) {
				records.add(value);
				value = jedis.spop(SentimentConstant.SENTIMENT_CACHE_KEY);
			}
			logger.info("Records'size = {}", records.size());
		} catch (Exception e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
				jedis = null;
			}
		} finally {
			if (jedis != null && jedis.isConnected()) {
				pool.returnResource(jedis);
			}
		}
		return records;
	}

	/**
	 * 获取数据
	 */
	public synchronized List<String> getRecords(String key) {
		List<String> records = new ArrayList<>();
		Jedis jedis = getJedis();
		if (jedis == null) {
			return records;
		}
		try {
			String value = jedis.spop(key);
			//			while (value != null) {
			for (int i = 0; (i < 10000) && (value != null); i++) {
				records.add(value);
				value = jedis.spop(key);
			}
			logger.info("Records'size = {}", records.size());
		} catch (Exception e) {
			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
				jedis = null;
			}
		} finally {
			if (jedis != null && jedis.isConnected()) {
				pool.returnResource(jedis);
			}
		}
		return records;
	}

	/**
	 * 将数据从String映射到Object
	 */
	public List<RecordInfo> mapper(List<String> records) {
		List<RecordInfo> recordInfos = new ArrayList<>();
		for (String record : records) {
			try {
				recordInfos.add(OBJECT_MAPPER.readValue(record, RecordInfo.class));
			} catch (Exception e) {
				logger.error("Record:{}", record);
				logger.error("Exception:{}", LogbackUtil.expection2Str(e));
			}
		}
		return recordInfos;
	}

	public void close() {
		// 程序关闭时，需要调用关闭方法
		pool.destroy();
	}

}
