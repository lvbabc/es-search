package zx.soft.tksdn.es.driver;

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
			//			pgd.addClass("oracleToRedis", OracleToRedis.class, "将站点数据定时导入Redis中（默认是每小时）");
			pgd.driver(args);
			// Success
			exitCode = 0;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		System.exit(exitCode);
	}
}
