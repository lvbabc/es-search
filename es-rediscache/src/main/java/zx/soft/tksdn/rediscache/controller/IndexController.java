//package zx.soft.tksdn.rediscache.controller;
//
//import javax.inject.Inject;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.ResponseStatus;
//
//import zx.soft.tksdn.common.index.PostData;
//import zx.soft.tksdn.rediscache.domain.ErrorResponse;
//import zx.soft.tksdn.rediscache.service.IndexService;
//
///**
// * 索引控制类
// *
// * @author wanggang
// *
// */
//@Controller
//@RequestMapping("/tksdn/index")
//public class IndexController {
//
//	@Inject
//	private IndexService indexService;
//
//	@RequestMapping(method = RequestMethod.POST)
//	@ResponseStatus(HttpStatus.CREATED)
//	public @ResponseBody ErrorResponse add(@RequestBody PostData postData) {
//		return indexService.addIndexData(postData);
//	}
//
//}
