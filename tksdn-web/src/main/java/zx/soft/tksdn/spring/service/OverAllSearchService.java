package zx.soft.tksdn.spring.service;

import zx.soft.tksdn.common.domain.OverAllRequest;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.query.ESQueryCore;

public class OverAllSearchService {

	public QueryResult get(OverAllRequest request) {

		return ESQueryCore.getInstance().getOverAll(request);
	}
}
