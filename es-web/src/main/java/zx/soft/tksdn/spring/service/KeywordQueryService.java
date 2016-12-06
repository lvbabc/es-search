package zx.soft.tksdn.spring.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import zx.soft.tksdn.common.domain.KeywordsCount;
import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.query.ESQueryCore;
import zx.soft.utils.time.TimeUtils;

/**
 * @author lvbing
 */
@Service
public class KeywordQueryService {
	private static Logger logger = LoggerFactory.getLogger(KeywordQueryService.class);

	public List<KeywordsCount> queryStringQuery(QueryParams queryParams) {

		String keyword = queryParams.getQ().replaceAll("，", ",");
		List<String> keywords = new ArrayList<>();
		for (String key : keyword.split(",")) {
			keywords.add(key.trim());
		}
		queryParams.setRangeFiled("timestamp");
		if (queryParams.getRangeStart() == "") {
			long now = TimeUtils.getZeroHourTime(System.currentTimeMillis());
			long endTime = TimeUtils.transCurrentTime(now, 0, 0, 0, 0);
			long startTime = TimeUtils.transCurrentTime(endTime, 0, -1, 0, 0);
			queryParams.setRangeStart(TimeUtils.transToCommonDateStr(startTime));
			queryParams.setRangeEnd(TimeUtils.transToCommonDateStr(endTime));
		}

		logger.info(keywords.toString());
		return ESQueryCore.getInstance().queryKeywords(keywords,queryParams);
	}

	public QueryResult querySingle(QueryParams queryParams) {

		return ESQueryCore.getInstance().querySingle(queryParams);
	}

}
