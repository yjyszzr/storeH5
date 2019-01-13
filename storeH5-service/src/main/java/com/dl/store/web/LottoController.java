package com.dl.store.web;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.lotto.api.ISuperLottoService;
import com.dl.lotto.dto.LottoFirstDTO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 大乐透
 * @author Administrator
 * @date 2019.01.13
 */
@RestController
@RequestMapping("/lotto")
@Slf4j
public class LottoController {

	@Resource
	private ISuperLottoService iLottoService;
	
	@ApiOperation(value = "选号投注页数据", notes = "选号投注页数据")	
	@PostMapping("/getTicketInfo")
    public BaseResult<LottoFirstDTO> getTiketInfo(@RequestBody EmptyParam param){
		return iLottoService.getTicketInfo(param);
	}


}
