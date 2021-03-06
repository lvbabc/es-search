//package zx.soft.tksdn.rediscache.service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ThreadPoolExecutor;
//
//import javax.inject.Inject;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import zx.soft.tksdn.common.index.PostData;
//import zx.soft.tksdn.common.index.RecordInfo;
//import zx.soft.tksdn.es.utils.RedisMQ;
//import zx.soft.tksdn.rediscache.domain.ErrorResponse;
//import zx.soft.utils.log.LogbackUtil;
//import zx.soft.utils.threads.ApplyThreadPool;
//
///**
// * 索引服务类
// *
// * @author lvbing
// *
// */
//@Service
//public class IndexService {
//
//	private static Logger logger = LoggerFactory.getLogger(IndexService.class);
//
//	//	@Inject
//	//	private PersistCore persistCore;
//
//	@Inject
//	private RedisMQ redisMQ;
//
//	private static ThreadPoolExecutor pool = ApplyThreadPool.getThreadPoolExector();
//
//	static {
//		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//			@Override
//			public void run() {
//				pool.shutdown();
//			}
//		}));
//	}
//
//	public ErrorResponse addIndexData(final PostData postData) {
//		if (postData == null) {
//			logger.info("Records' size=0");
//			return new ErrorResponse.Builder(-1, "no post data.").build();
//		}
//		logger.info("Records' Size:{}", postData.getRecords().size());
//		try {
//			if (postData.getRecords().size() > 0) {
//
//				pool.execute(new Thread(new Runnable() {
//					@Override
//					public void run() {
//						// 去重处理
//						List<RecordInfo> recordsNew = new ArrayList<>();
//						for (RecordInfo record : postData.getRecords()) {
////							if (!redisMQ.sismember(SentimentConstant.TK_KEY_INSERTED, record.getId())) {
////								redisMQ.sadd(SentimentConstant.TK_KEY_INSERTED, record.getId());
////								recordsNew.add(record);
////							}
//						}
//						// 持久化到Redis
//						if (recordsNew.size() > 0) {
//							add2Redis(recordsNew);
//						}
//						// 这里面以及包含了错误日志记录
//						//						persist(recordsNew);
//					}
//				}));
//
//			}
//			return new ErrorResponse.Builder(0, "ok").build();
//		} catch (Exception e) {
//			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
//			return new ErrorResponse.Builder(-1, "persist error!").build();
//		}
//	}
//
//	/**
//	 * 数据持久化到Redis
//	 */
//	private void add2Redis(List<RecordInfo> records) {
////		String[] data = new String[records.size()];
////		for (int i = 0; i < records.size(); i++) {
////			if (records.get(i).getPic_url().length() > 500) {
////				records.get(i).setPic_url(records.get(i).getPic_url().substring(0, 500));
////			}
////			//			logger.info("MQ Record:{}", records.get(i).getId());
////			data[i] = JsonUtils.toJsonWithoutPretty(records.get(i));
////		}
////		try {
////			redisMQ.addRecord(data);
////		} catch (Exception e) {
////			logger.error("Exception:{}", LogbackUtil.expection2Str(e));
////		}
//	}
//
//	/**
//	 * 数据持久化到Mysql
//	 */
//	//	private void persist(List<RecordInfo> records) {
//	//		for (RecordInfo record : records) {
//	//			if (record.getPic_url().length() > 500) {
//	//				record.setPic_url(record.getPic_url().substring(0, 500));
//	//			}
//	//			persistCore.persist(redisMQ, record);
//	//		}
//	//	}
//
//}