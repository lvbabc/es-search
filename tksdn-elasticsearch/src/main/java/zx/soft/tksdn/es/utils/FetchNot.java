package zx.soft.tksdn.es.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.log.LogbackUtil;

/**
 * @author lvbing
 * 用于提取NOT后关键词
 */
public class FetchNot {
	private static Logger logger = LoggerFactory.getLogger(FetchNot.class);

	//	public static void main(String args[]) {
	//		FetchNot fNot = new FetchNot();
	//		fNot.fetch("(演唱会  NOT  汪峰 AND) OR ");
	//	}

	public String fetch(String selfEdit) {
		selfEdit = selfEdit.replaceAll("AND|OR|\\(|\\)|（|）", " ");
		logger.info(selfEdit);
		int index = 0;
		int lastIndex = 0;
		int size = selfEdit.length();
		String not = null;
		try {
			if (selfEdit.indexOf("NOT") != -1) {
				index = selfEdit.indexOf("NOT");

				if (selfEdit.indexOf(" ", index + 5) != -1) {
					not = selfEdit.substring(index + 3, selfEdit.indexOf(" ", index + 5)).trim();
					return not;
				}
				lastIndex = size;
				not = selfEdit.substring(index + 3, lastIndex).trim();
				return not;
			}
		} catch (Exception e) {
			logger.error(LogbackUtil.expection2Str(e));
		}
		return null;
	}
}
