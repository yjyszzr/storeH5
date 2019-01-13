package com.dl.store.web;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.lottery.dto.OrderIdDTO;
import com.dl.lotto.api.ISuperLottoService;
import com.dl.lotto.dto.LottoFirstDTO;
import com.dl.lotto.param.SaveBetInfoParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.LottoOrderDetailDTO;
import com.dl.order.param.OrderDetailParam;
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
	@Resource
	private IOrderService iOrderService;
	
	@ApiOperation(value = "选号投注页数据", notes = "选号投注页数据")	
	@PostMapping("/getTicketInfo")
    public BaseResult<LottoFirstDTO> getTiketInfo(@RequestBody EmptyParam param){
		log.info("[getTiketInfo]");
		return iLottoService.getTicketInfo(param);
	}
	
	@ApiOperation(value = "生成模拟订单", notes = "生成模拟订单")	
	@PostMapping("/createOrderSimulate")
    public BaseResult<OrderIdDTO> createOrderSimulateByStore(@RequestBody SaveBetInfoParam param){
		log.info("[createOrderSimulateByStore]");
		return iLottoService.createOrderSimulateByStore(param);
	}
	
	@ApiOperation(value = "获取乐透详情", notes = "获取乐透详情")
	@PostMapping("/getLottoDetail")
	public BaseResult<LottoOrderDetailDTO> getLottoOrderDetailByStore(@RequestBody OrderDetailParam param){
		log.info("[getLottoOrderDetailByStore]");
		return iOrderService.getLottoOrderDetailSimulatByStore(param);
	}
}
