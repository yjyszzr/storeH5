//package com.dl.store.api;
//
//import com.dl.base.result.BaseResult;
//import com.dl.store.dto.YesOrNoDTO;
//import com.dl.store.param.UserIdParam;
//import org.springframework.cloud.netflix.feign.FeignClient;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//
//@FeignClient(value = "store-service")
//public interface IUserAuthService {
//
//	@RequestMapping(path = "/user/auths/queryBinds", method = RequestMethod.POST)
//	public BaseResult<YesOrNoDTO> queryBinds(@RequestBody UserIdParam param);
//
//}
