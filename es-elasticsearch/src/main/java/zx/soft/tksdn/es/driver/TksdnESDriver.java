package zx.soft.tksdn.es.driver;

import zx.soft.tksdn.es.delete.RemoveData;
import zx.soft.tksdn.es.hotkey.HotKey;
import zx.soft.tksdn.es.index.IndexfromSolrNow;
import zx.soft.tksdn.es.index.IndexfromSolrPast;
import zx.soft.utils.driver.ProgramDriver;

/**
 * 驱动类
 *
 * @author lvbing
 *
 */
public class TksdnESDriver {

	/**
	 * 主函数
	 */
	public static void main(String[] args) {

		int exitCode = -1;
		ProgramDriver pgd = new ProgramDriver();
		try {
			pgd.addClass("indexfromSolrPast", IndexfromSolrPast.class, "将solr数据导入ES（默认是每小时）");
			pgd.addClass("indexfromSolrNow", IndexfromSolrNow.class, "将solr数据导入ES（默认是每小时）");
			pgd.addClass("hotkey", HotKey.class, "分时段计算热门关键词（默认是每半小时）");
			pgd.addClass("removedata", RemoveData.class, "删除数据");
			pgd.driver(args);
			// Success
			exitCode = 0;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		System.exit(exitCode);
	}
}
