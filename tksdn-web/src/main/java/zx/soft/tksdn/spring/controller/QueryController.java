package zx.soft.tksdn.spring.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.spring.service.QueryService;

/**
 *
 * @author lvbing
 *
 */
@Controller
@RequestMapping("/es")
public class QueryController {
	Logger logger = LoggerFactory.getLogger(QueryController.class);

	@Inject
	private QueryService queryService;

	@RequestMapping(value = "/keyword", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Object queryData(HttpServletRequest request) {
		QueryParams queryParams = new QueryParams();
		queryParams.setQ(request.getParameter("q") == null ? "*" : request.getParameter("q"));
		logger.info(queryParams.toString());
		return queryService.queryStringQuery(queryParams);
	}
}
