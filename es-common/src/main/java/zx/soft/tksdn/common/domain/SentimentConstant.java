package zx.soft.tksdn.common.domain;

/**
 * 與情信息常量值
 *
 * @author wanggang
 *
 */
public class SentimentConstant {

	/**
	 * 與情平台类型
	 */
	public static final String[] PLATFORM_ARRAY = { "其他类", "资讯类", "论坛类", "微博类", "博客类", "QQ类", "搜索类", "回复类", "邮件类",
		"图片识别类", "微信类" };

	// 专题模块站点信息键名，用于多个站点Md5-站点列表存储
	public static final String SITE_GROUPS = "sent:site:groups";

	// 所有站点id和站点名称对应的列表名称
	public static final String SITE_MAP = "sent:site:map";

	// 與情数据消息队列
	public static final String TK_CACHE_KEY = "tk.cache.records";

	// CDH5 缓存数据队列
	public static final String CDH5_CACHE_RECORDS = "cdh5.cache.records";

	// 存储记录的id，主要用于数据库写入去重
	public static final String TK_KEY_INSERTED = "tk:key:inserted";

}
