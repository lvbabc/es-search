package zx.soft.tksdn.spring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.query.ESQueryCore;

/**
 * @author lvbing
 */
@Service
public class QueryService {
	private static Logger logger = LoggerFactory.getLogger(QueryService.class);


	public QueryResult queryStringQuery(QueryParams queryParams) {

		//		String q = queryParams.getQ().toUpperCase().replaceAll("(AND|OR|NOT)", " " + "$0" + " ")
		//				.replaceAll("(\\+|-|\\|)", " " + "$0");
		//		System.out.println(q);

		return ESQueryCore.getInstance().queryData(queryParams, true);

	}

}
