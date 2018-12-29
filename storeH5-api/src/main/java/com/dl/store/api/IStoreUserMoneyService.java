package com.dl.store.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dl.base.result.BaseResult;
import com.dl.store.param.AwardParam;
import com.dl.store.param.OrderRollBackParam;

@FeignClient(value = "store-service")
public interface IStoreUserMoneyService {
	
	@RequestMapping(path = "/pay/award", method = RequestMethod.POST)
	public BaseResult<Object> orderAward(@RequestBody AwardParam param);

	@RequestMapping(path = "/pay/rollback", method = RequestMethod.POST)
	public BaseResult<Object> orderRollBack(@RequestBody OrderRollBackParam param);
}
