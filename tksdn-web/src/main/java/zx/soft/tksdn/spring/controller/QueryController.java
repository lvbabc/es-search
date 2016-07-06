package zx.soft.tksdn.spring.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import zx.soft.tksdn.common.domain.OverAllRequest;
import zx.soft.tksdn.common.domain.QueryParams;
import zx.soft.tksdn.spring.service.HotKeyService;
import zx.soft.tksdn.spring.service.KeywordQueryService;
import zx.soft.tksdn.spring.service.OverAllSearchService;
import zx.soft.tksdn.spring.service.TrendService;
import zx.soft.utils.json.JsonUtils;

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
	private KeywordQueryService queryService;
	@Inject
	private OverAllSearchService searchService;
	@Inject
	private TrendService trendService;
	@Inject
	private HotKeyService hotKeyService;

	/**
	 * 获取关键词数量
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/keyword", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Object queryData(HttpServletRequest request) {
		QueryParams queryParams = new QueryParams();
		queryParams.setQ(request.getParameter("q") == null ? "*" : request.getParameter("q"));
		queryParams.setRangeStart(request.getParameter("rangestart") == null ? "*" : request.getParameter("rangestart"));
		queryParams.setRangeEnd(request.getParameter("rangeend") == null ? "*" : request.getParameter("rangeend"));

//		logger.info(queryParams.toString());
		return queryService.queryStringQuery(queryParams);
	}

	/**
	 * 返回包含关键词的所有信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/data", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Object querySingle(HttpServletRequest request) {
		QueryParams queryParams = new QueryParams();
		queryParams.setQ(request.getParameter("q") == null ? "*" : request.getParameter("q"));
		queryParams.setFrom(request.getParameter("from") == null ? 0 : Integer.parseInt(request.getParameter("from")));
		queryParams.setSize(request.getParameter("size") == null ? 10 : Integer.parseInt(request.getParameter("size")));
		queryParams.setHlfl(request.getParameter("hlfl") == null ? "" : request.getParameter("hlfl"));
		queryParams.setSort(request.getParameter("sort") == null ? "" : request.getParameter("sort"));
		queryParams.setId(request.getParameter("id") == null ? "" : request.getParameter("id"));
		queryParams.setTermsAgg(request.getParameter("termsAgg") == null ? "" : request.getParameter("termsAgg"));
		queryParams.setRangeStart(request.getParameter("timestampstart") == null ? "" : request.getParameter("timestampstart"));
		queryParams.setRangeEnd(request.getParameter("timestampend") == null ? "" : request.getParameter("timestampend"));
//		logger.info(queryParams.toString());
		return queryService.querySingle(queryParams);
	}

	/**
	 * 综合搜索
	 * 模糊查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/oversearch", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Object overAllSearch(@RequestBody OverAllRequest request) {

		logger.info(JsonUtils.toJsonWithoutPretty(request));
		return searchService.get(request);
	}

	/**
	 * 获取关键词数量
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/hotkey", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Object getHotKey(HttpServletRequest request) {
		QueryParams queryParams = new QueryParams();
		queryParams.setCount(request.getParameter("count") == null ? 20 : Integer.parseInt(request.getParameter("count")));
		queryParams.setRangeStart(request.getParameter("rangeStart") == null ? "" : request.getParameter("rangeStart"));
		queryParams.setRangeEnd(request.getParameter("rangeEnd") == null ? "" : request.getParameter("rangeEnd"));
//		logger.info(queryParams.toString());
		return hotKeyService.getHotKey(queryParams);//trendService.getTrendInfos(queryParams);
	}
}
