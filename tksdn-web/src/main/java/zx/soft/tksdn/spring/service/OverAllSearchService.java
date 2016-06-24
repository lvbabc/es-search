package zx.soft.tksdn.spring.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import zx.soft.tksdn.common.domain.OverAllRequest;
import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.es.domain.QueryResult;
import zx.soft.tksdn.es.query.ESQueryCore;
import zx.soft.utils.json.JsonUtils;

/**
 * @author lvbing
 */
@Service
public class OverAllSearchService {
	Logger logger = LoggerFactory.getLogger(OverAllSearchService.class);

	public QueryResult get(OverAllRequest request) {

		List<String> protocol_type = request.getProtocol_type();
		List<String> resource_type = request.getResource_type();

		for (int i = 0; i < protocol_type.size(); i++) {
			protocol_type.set(i, protocol_type.get(i).toUpperCase());
		}
		for (int i = 0; i < resource_type.size(); i++) {
			resource_type.set(i, resource_type.get(i).toUpperCase());
		}

		QueryParams queryParams = new QueryParams();
		request.setProtocol_type(protocol_type);
		request.setResource_type(resource_type);
		queryParams.setFrom(request.getFrom());
		queryParams.setSize(request.getSize());
		queryParams.setRequest(request);

		logger.info(JsonUtils.toJsonWithoutPretty(queryParams));
		return ESQueryCore.getInstance().getOverAll(queryParams);
	}
}
