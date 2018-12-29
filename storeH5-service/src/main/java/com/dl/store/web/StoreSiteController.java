package com.dl.store.web;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.store.dto.StoreSiteDTO;
import com.dl.store.param.StoreSiteParam;
import com.dl.store.service.StoreService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/***
 * 店铺相关信息
 * @author Administrator
 */
@RestController
@RequestMapping("/store")
@Slf4j
public class StoreSiteController {
	@Resource
	private StoreService storeService;
	
	@ApiOperation(value = "店铺站点页面", notes = "店铺站点页面")
	@PostMapping("/site")
    public BaseResult<StoreSiteDTO> siteInfo(@RequestBody StoreSiteParam param) {
		log.info("[siteInfo]" + "site...");
		Integer id = param.getStoreId();
		if(id == null || id <= 0) {
			return ResultGenerator.genFailResult("id不能为空~");
		}
		return storeService.getStoreSite(id);
    }
}
