package com.dl.store.web;

import java.math.BigDecimal;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.lottery.api.ILotteryDiscoveryService;
import com.dl.lottery.dto.OrderIdDTO;
import com.dl.lottery.dto.SZCPrizeDTO;
import com.dl.lottery.dto.SZCResultDTO;
import com.dl.lottery.param.DiscoveryPageParam;
import com.dl.lottery.param.SZCQueryParam;
import com.dl.lotto.api.ISuperLottoService;
import com.dl.lotto.dto.LottoChartDataDTO;
import com.dl.lotto.dto.LottoFirstDTO;
import com.dl.lotto.param.ChartSetupParam;
import com.dl.lotto.param.SaveBetInfoParam;
import com.dl.member.api.IUserService;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.LottoOrderDetailDTO;
import com.dl.order.dto.OrderDTO;
import com.dl.order.dto.OrderDetailDTO;
import com.dl.order.dto.StoreUserInfoDTO;
import com.dl.order.param.OrderDetailParam;
import com.dl.order.param.OrderIdParam;
import com.dl.store.dto.UserBonusDTO;
import com.dl.store.dto.UserDTO;
import com.dl.store.enums.OrderEnums;
import com.dl.store.model.UserStoreMoney;
import com.dl.store.param.UserIdParam;
import com.dl.store.service.OrderService;
import com.dl.store.service.UserBonusService;
import com.dl.store.service.UserService;
import com.dl.store.service.UserStoreMoneyService;

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
	@Resource
	private OrderService orderService;
	@Resource
	private UserService userService;
	@Resource
	private UserStoreMoneyService userStoreMoneyService;
	@Resource
	private UserBonusService userBonusService;
	@Resource
	private ILotteryDiscoveryService iLotteryDisService;
	
	
	@ApiOperation(value = "查询每期结果", notes = "查询每期结果")
	@PostMapping("/querySzcOpenPrizesByDate")
    public BaseResult<SZCResultDTO> querySzcOpenPrizesByDate(@RequestBody SZCQueryParam param){
		log.info("[szcDetailList]");
		return iLotteryDisService.querySzcOpenPrizesByDate(param);
	}
	
	@ApiOperation(value = "获取乐透开奖结果", notes = "获取乐透开奖结果")
	@PostMapping("/szcDetailList")
    public BaseResult<SZCPrizeDTO> szcDetailList(@RequestBody DiscoveryPageParam param){
		log.info("[szcDetailList]");
		return iLotteryDisService.szcDetailList(param);
	}
	
	
	@ApiOperation(value = "获取走势图", notes = "走势图")
	@PostMapping("/getChartData")
    public BaseResult<LottoChartDataDTO> getChartDataByStore(@RequestBody ChartSetupParam param){
		log.info("[getChartDataByStore]");
		return iLottoService.getChartDataByStore(param);
	}
	
	@ApiOperation(value = "选号投注页数据", notes = "选号投注页数据")	
	@PostMapping("/getTicketInfo")
    public BaseResult<LottoFirstDTO> getTiketInfo(@RequestBody EmptyParam param){
		log.info("[getTiketInfo]");
		return iLottoService.getTicketInfo(param);
	}
	
	@ApiOperation(value = "生成模拟订单", notes = "生成模拟订单")	
	@PostMapping("/createOrderSimulate")
    public BaseResult<OrderIdDTO> createOrderSimulateByStore(@RequestBody SaveBetInfoParam param){
		Integer storeId = param.getStoreId();
		if(storeId == null || storeId <= 0) {
			return ResultGenerator.genResult(OrderEnums.STORE_ID_EMPTY.getcode(),OrderEnums.STORE_ID_EMPTY.getMsg());
		}
		log.info("[createOrderSimulateByStore]" + " storeId:" + storeId);
		return iLottoService.createOrderSimulateByStore(param);
	}
	
	@ApiOperation(value = "获取乐透详情", notes = "获取乐透详情")
	@PostMapping("/getLottoDetail")
	public BaseResult<LottoOrderDetailDTO> getLottoOrderDetailByStore(@RequestBody OrderDetailParam param){
		Integer userId = SessionUtil.getUserId();
		Integer storeId = param.getStoreId();
		String orderId = param.getOrderId();
		Integer bonusId = param.getBonusId();
		log.info("[getLottoOrderDetailByStore]" + " userId:" + userId + " storeId:" + storeId + " orderId:" + orderId);
		if(storeId == null || storeId <= 0) {
			return ResultGenerator.genResult(OrderEnums.STORE_ID_EMPTY.getcode(),OrderEnums.STORE_ID_EMPTY.getMsg());
		}
		BaseResult<LottoOrderDetailDTO> baseResult = iOrderService.getLottoOrderDetailSimulatByStore(param);
		if(!baseResult.isSuccess()) {
			return ResultGenerator.genResult(baseResult.getCode(),baseResult.getMsg());
		}
		LottoOrderDetailDTO lottoDetailDTO = baseResult.getData();
		StoreUserInfoDTO storeUserDTO = getStoreUserInfoDTO(orderId,userId,storeId,bonusId);
		lottoDetailDTO.setUserInfo(storeUserDTO);
		return ResultGenerator.genSuccessResult("succ", lottoDetailDTO);
	}
	
	
	private StoreUserInfoDTO getStoreUserInfoDTO(String orderId,Integer userId,Integer storeId,Integer bonusId) {
		StoreUserInfoDTO storeUserDTO = null;
		UserStoreMoney userStoreMoney = null;
		UserIdParam userIdParam = new UserIdParam();
		userIdParam.setUserId(userId);
		UserDTO user = userService.queryUserInfo(userIdParam);
		com.dl.store.param.OrderDetailParam orderDetailParam = new com.dl.store.param.OrderDetailParam();
		orderDetailParam.setStoreId(storeId);
		orderDetailParam.setOrderId(orderId);
		OrderDetailDTO orderDetailDTO = orderService.getOrderDetail(orderDetailParam);
		log.info("[orderDetailDTO]" + "->" + orderDetailDTO);
		if("1".equals(user.getIsSuperWhite()) && userId != null && userId > 0) {
			userStoreMoney = userStoreMoneyService.queryUserMoneyInfo(userId,storeId);	
			storeUserDTO = new StoreUserInfoDTO();
			storeUserDTO.setIsSuperWhite("1");
			BigDecimal bigDec = null;
			if(userStoreMoney != null) {
				bigDec = userStoreMoney.getMoney();
			}else {
				bigDec = BigDecimal.ZERO;
			}
			Integer bonusSize = userBonusService.validBonusSize(userId,storeId);
			log.info("[getOrderDetail]" + " money:" + bigDec + " bonusSize:" + bonusSize);
			storeUserDTO.setMoney(bigDec.toString());
			storeUserDTO.setBonusNum(bonusSize);
			if(bonusId != null && bonusId > 0) {
				UserBonusDTO userBonusDTO = userBonusService.queryUserBonus(bonusId);
				log.info("[getOrderDetail]" + " userBonusDTO:" + userBonusDTO);
				if(userBonusDTO != null) {
					BigDecimal amt = BigDecimal.valueOf(Double.valueOf(orderDetailDTO.getTicketAmount())).subtract(userBonusDTO.getBonusPrice());
					log.info("[getOrderDetail]" + "实付金额:" + amt + " 订单金额:" + orderDetailDTO.getTicketAmount() + " 红包金额:" + userBonusDTO.getBonusPrice());
					storeUserDTO.setActualAmount(amt+"");
					storeUserDTO.setBonusName(userBonusDTO.getBonusPrice().toString()+"元代金券");
					storeUserDTO.setBonusPrice(userBonusDTO.getBonusPrice().toString());
				}else {
					storeUserDTO.setActualAmount(orderDetailDTO.getTicketAmount());
				}
			}else {
				storeUserDTO.setActualAmount(orderDetailDTO.getTicketAmount());
			}
		}
		return storeUserDTO;
	}
}
