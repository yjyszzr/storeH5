package com.dl.store.web;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.lottery.api.ILotteryMatchService;
import com.dl.lottery.dto.DLZQBetInfoDTO;
import com.dl.lottery.dto.DlJcZqMatchListDTO;
import com.dl.lottery.dto.LeagueInfoDTO;
import com.dl.lottery.dto.OrderIdDTO;
import com.dl.lottery.param.DlJcZqMatchBetParam;
import com.dl.lottery.param.DlJcZqMatchListParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.dto.OrderDetailDTO;
import com.dl.order.dto.OrderInfoListDTO;
import com.dl.order.dto.StoreUserInfoDTO;
import com.dl.order.dto.TicketSchemeDTO;
import com.dl.order.param.OrderDetailParam;
import com.dl.order.param.OrderInfoListParam;
import com.dl.order.param.OrderSnParam;
import com.dl.order.param.TicketSchemeParam;
import com.dl.store.dto.UserBonusDTO;
import com.dl.store.dto.UserDTO;
import com.dl.store.enums.OrderEnums;
import com.dl.store.model.UserStoreMoney;
import com.dl.store.param.UserIdParam;
import com.dl.store.service.UserBonusService;
import com.dl.store.service.UserService;
import com.dl.store.service.UserStoreMoneyService;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
* Created by CodeGenerator on 2018/03/21.
*/
@Slf4j
@RestController
@RequestMapping("/store/match")
public class StoreMatchController {
	
	private final static Logger logger = Logger.getLogger(StoreMatchController.class);
    @Resource
    private  ILotteryMatchService lotteryMatchService;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private IOrderService iOrderService;
    
//    @Resource
//    private OrderService orderService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserStoreMoneyService userStoreMoneyService;
    
    @Resource
    private UserBonusService userBonusService;
    
    @ApiOperation(value = "获取筛选条件列表-足球", notes = "获取筛选条件列表-足球")
    @PostMapping("/filterConditions")
    public BaseResult<List<LeagueInfoDTO>> getFilterConditions(@Valid @RequestBody EmptyParam emptyParam) {
    	return lotteryMatchService.getMatchByConditions(emptyParam);
    }
    
    
	@ApiOperation(value = "店铺获取足球赛事列表", notes = "店铺获取足球赛事列表")
    @PostMapping("/getMatchList")
    public BaseResult<DlJcZqMatchListDTO> getMatchList(@Valid @RequestBody DlJcZqMatchListParam param) {
//		logger.info("获取足球赛事列表========+++++++++++=========="+lotteryMatchService.getMatchList(param).getData());
		return lotteryMatchService.getMatchList(param);
    }

	@ApiOperation(value = "计算投注信息", notes = "计算投注信息,times默认值为1，betType默认值为11")
	@PostMapping("/getBetInfo")
	public BaseResult<DLZQBetInfoDTO> getBetInfo(@Valid @RequestBody DlJcZqMatchBetParam param) {
		BaseResult<DLZQBetInfoDTO > DLZQBetInfo =lotteryMatchService.getBetInfo(param);
		return ResultGenerator.genSuccessResult("", DLZQBetInfo.getData());
	}
	
	
	@ApiOperation(value = "订单列表", notes = "订单列表")
    @PostMapping("/getOrderInfoList")
    public BaseResult<PageInfo<OrderInfoListDTO>> getOrderInfoList(@Valid @RequestBody OrderInfoListParam param) {
		PageInfo<OrderInfoListDTO> orderInfoListDTOs = iOrderService.getOrderInfoListForStoreProject(param).getData();
		return ResultGenerator.genSuccessResult("订单列表查询成功", orderInfoListDTOs);
    }
	
	@ApiOperation(value = "查询出票方案", notes = "查询出票方案")
    @PostMapping("/getTicketScheme")
    public BaseResult<TicketSchemeDTO> getTicketScheme(@Valid @RequestBody TicketSchemeParam param) {
		TicketSchemeDTO ticketSchemeDTO = iOrderService.getTicketScheme(param).getData();
		return ResultGenerator.genSuccessResult("出票方案查询成功", ticketSchemeDTO);
    }
	
	
	@ApiOperation(value = "查询订单详情", notes = "查询订单详情")
    @PostMapping("/getOrderDetail")
    public BaseResult<OrderDetailDTO> getOrderDetail(@Valid @RequestBody OrderDetailParam param) { 
		Integer userId = SessionUtil.getUserId();
		Integer storeId = param.getStoreId();
		String orderId = param.getOrderId();
		Integer bonusId = param.getBonusId();
		log.info("接口为[getOrderDetail]" + " userId:" + userId + ",storeId:" + storeId + ",orderId:"+orderId + " bonusId:" + bonusId);
		if(storeId == null || storeId <= 0) {
			return ResultGenerator.genResult(OrderEnums.STORE_ID_EMPTY.getcode(),OrderEnums.STORE_ID_EMPTY.getMsg());
		}
		UserStoreMoney userStoreMoney = null;
		UserIdParam userIdParam = new UserIdParam();
		userIdParam.setUserId(userId);
		UserDTO user = userService.queryUserInfo(userIdParam);
		StoreUserInfoDTO storeUserDTO = null;
		OrderDetailDTO orderDetailDTO = iOrderService.getOrderDetail(param).getData();
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
		orderDetailDTO.setUserInfo(storeUserDTO);
		OrderSnParam snParam =new OrderSnParam();
		 OrderDTO   orderDTO    = 	iOrderService.getOrderInfoByOrderSn(snParam).getData();
		orderDetailDTO.setStoreId(orderDTO.getStoreId());
		return ResultGenerator.genSuccessResult("订单详情查询成功", orderDetailDTO);
    }

	@ApiOperation(value = "根据orderId查询订单详情", notes = "根据orderId查询订单详情")
	@PostMapping("/getOrderDetailByOrderId")
	public BaseResult<OrderDetailDTO> getOrderDetailByOrderSn(@Valid @RequestBody OrderDetailParam param) {
		 OrderDetailDTO orderDetailDTO = iOrderService.getOrderDetail(param).getData();
		return ResultGenerator.genSuccessResult("订单详情查询成功", orderDetailDTO);
	}
	
	@ApiOperation(value = "生成订单", notes = "生成订单")
	@PostMapping("/createOrder")
	public BaseResult<OrderIdDTO> createOrder(@Valid @RequestBody DlJcZqMatchBetParam param){
		Integer userId = SessionUtil.getUserId();
		log.info("[createOrder]" + " userId:" + userId);
		return	lotteryMatchService.createOrderForStoreProject(param);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	@ApiOperation(value = "根据ordersn查询订单详情(已废弃)", notes = "根据ordersn查询订单详情(已废弃)")
	@PostMapping("/getOrderDetailByOrderSnScrap")
	public BaseResult<OrderDetailDTO> getOrderDetailByOrderSnScrap(@Valid @RequestBody OrderDetailByOrderSnPara param) {
//		OrderDetailDTO orderDetailDTO = orderService.getOrderDetailByOrderSn(param);
		return ResultGenerator.genSuccessResult("订单详情查询成功", new OrderDetailDTO());
	}
	@ApiOperation(value = "生成模拟订单(已废弃)", notes = "生成模拟订单(已废弃)")
	@PostMapping("/createOrderBySimulate")
	public BaseResult<OrderIdDTO> createOrderBySimulate(@Valid @RequestBody DlJcZqMatchBetParam param){
		log.info("DlJcZqMatchBetParam订单参数====================================={}",param); 
		BaseResult<String> rst = this.nSaveBetInfo(param);
		if(rst.getCode()!=0) {
			return ResultGenerator.genResult(rst.getCode(), rst.getMsg());
		}
		
		String payToken = rst.getData();
		if (StringUtils.isBlank(payToken)) {
			logger.info("payToken值为空！");
			return ResultGenerator.genResult(PayEnums.PAY_TOKEN_EMPTY.getcode(), PayEnums.PAY_TOKEN_EMPTY.getMsg());
		}
		// 校验payToken的有效性
		String jsonData = stringRedisTemplate.opsForValue().get(payToken);
		if (StringUtils.isBlank(jsonData)) {
			logger.info( "支付信息获取为空！");
			return ResultGenerator.genResult(PayEnums.PAY_TOKEN_EXPRIED.getcode(), PayEnums.PAY_TOKEN_EXPRIED.getMsg());
		}
		// 清除payToken
		stringRedisTemplate.delete(payToken);

		UserBetPayInfoDTO dto = null;
		try {
			dto = JSONHelper.getSingleBean(jsonData, UserBetPayInfoDTO.class);
		} catch (Exception e1) {
			logger.error("支付信息转DIZQUserBetInfoDTO对象失败！", e1);
			return ResultGenerator.genFailResult("模拟支付信息异常，模拟支付失败！");
		}
		if (null == dto) {
			return ResultGenerator.genFailResult("模拟支付信息异常，模拟支付失败！");
		}

		Integer userId = dto.getUserId();
		Integer currentId = SessionUtil.getUserId();
		if (!userId.equals(currentId)) {
			logger.info("支付信息不是当前用户的待支付彩票！");
			return ResultGenerator.genFailResult("模拟支付信息异常，支付失败！");
		}
		Double orderMoney = dto.getOrderMoney();
		Integer userBonusId = StringUtils.isBlank(dto.getBonusId()) ? 0 : Integer.valueOf(dto.getBonusId());// form paytoken
		BigDecimal ticketAmount = BigDecimal.valueOf(orderMoney);// from paytoken
		BigDecimal bonusAmount = BigDecimal.ZERO;//BigDecimal.valueOf(dto.getBonusAmount());// from  paytoken
		BigDecimal moneyPaid = BigDecimal.valueOf(orderMoney);// from paytoken
		BigDecimal surplus =  BigDecimal.ZERO;//BigDecimal.valueOf(dto.getSurplus());// from paytoken
		BigDecimal thirdPartyPaid =  BigDecimal.ZERO;//BigDecimal.valueOf(dto.getThirdPartyPaid());
		List<UserBetDetailInfoDTO> userBetCellInfos = dto.getBetDetailInfos();
		List<TicketDetail> ticketDetails = userBetCellInfos.stream().map(betCell -> {
			TicketDetail ticketDetail = new TicketDetail();
			ticketDetail.setMatch_id(betCell.getMatchId());
			ticketDetail.setChangci(betCell.getChangci());
			int matchTime = betCell.getMatchTime();
			if (matchTime > 0) {
				ticketDetail.setMatchTime(Date.from(Instant.ofEpochSecond(matchTime)));
			}
			ticketDetail.setMatchTeam(betCell.getMatchTeam());
			ticketDetail.setLotteryClassifyId(betCell.getLotteryClassifyId());
			ticketDetail.setLotteryPlayClassifyId(betCell.getLotteryPlayClassifyId());
			ticketDetail.setTicketData(betCell.getTicketData());
			ticketDetail.setIsDan(betCell.getIsDan());
			ticketDetail.setIssue(betCell.getPlayCode());
			ticketDetail.setFixedodds(betCell.getFixedodds());
			ticketDetail.setBetType(betCell.getBetType());
			return ticketDetail;
		}).collect(Collectors.toList());

		// order生成
		SubmitOrderParam submitOrderParam = new SubmitOrderParam();
		submitOrderParam.setTicketNum(dto.getTicketNum());
		submitOrderParam.setMoneyPaid(moneyPaid);
		submitOrderParam.setTicketAmount(ticketAmount);
		submitOrderParam.setSurplus(surplus);
		submitOrderParam.setThirdPartyPaid(thirdPartyPaid);
		submitOrderParam.setPayName("");
		submitOrderParam.setUserBonusId(userBonusId);
		submitOrderParam.setBonusAmount(bonusAmount);
		submitOrderParam.setOrderFrom(dto.getRequestFrom());
		int lotteryClassifyId = dto.getLotteryClassifyId();
		submitOrderParam.setLotteryClassifyId(lotteryClassifyId);
		int lotteryPlayClassifyId = dto.getLotteryPlayClassifyId();
		submitOrderParam.setLotteryPlayClassifyId(lotteryPlayClassifyId);
		submitOrderParam.setPassType(dto.getBetType());
		submitOrderParam.setPlayType("0" + dto.getPlayType());
		submitOrderParam.setBetNum(dto.getBetNum());
		submitOrderParam.setCathectic(dto.getTimes());
		submitOrderParam.setStoreId(param.getStoreId());
		if (lotteryPlayClassifyId != 8 && lotteryClassifyId == 1) {
			if (ticketDetails.size() > 1) {
				Optional<TicketDetail> max = ticketDetails.stream().max((detail1, detail2) -> detail1.getMatchTime().compareTo(detail2.getMatchTime()));
				submitOrderParam.setMatchTime(max.get().getMatchTime());
			} else {
				submitOrderParam.setMatchTime(ticketDetails.get(0).getMatchTime());
			}
		}
		submitOrderParam.setForecastMoney(dto.getForecastMoney());
		submitOrderParam.setIssue(dto.getIssue());
		submitOrderParam.setTicketDetails(ticketDetails);
		BaseResult<OrderDTO> createOrder = orderService.createOrder(submitOrderParam);
		if (createOrder.getCode() != 0) {
			logger.info("订单创建失败！");
			return ResultGenerator.genFailResult("模拟支付失败！");
		}
		String orderId = createOrder.getData().getOrderId().toString();
		String orderSn = createOrder.getData().getOrderSn();
		OrderIdDTO orderDto = new OrderIdDTO();
		orderDto.setOrderId(orderId);
		orderDto.setOrderSn(orderSn);
		return ResultGenerator.genSuccessResult("success", orderDto);
	}
	
	
	
	public BaseResult<String> nSaveBetInfo(@Valid @RequestBody DlJcZqMatchBetParam param) {
		EmptyParam emptyParam =new EmptyParam();
		if(lotteryMatchService.isShutDownBet(emptyParam).getData()) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_MATCH_STOP.getCode(), LotteryResultEnum.BET_MATCH_STOP.getMsg());
		}
		List<MatchBetPlayDTO> matchBetPlays = param.getMatchBetPlays();
		if(matchBetPlays == null || matchBetPlays.size() < 1) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_CELL_EMPTY.getCode(), LotteryResultEnum.BET_CELL_EMPTY.getMsg());
		}
		//设置投注倍数
		Integer times = param.getTimes();
		if(null == times || times < 1) {
			param.setTimes(1);
		}
		if(param.getTimes() >= 99999) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_TIMES_LIMIT.getCode(), LotteryResultEnum.BET_TIMES_LIMIT.getMsg());
		}
		String playType = param.getPlayType();
		if(StringUtils.isBlank(playType)) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_PLAY_ENABLE.getCode(), LotteryResultEnum.BET_PLAY_ENABLE.getMsg());
		}
		try {
			int parseInt = Integer.parseInt(playType);
			if(parseInt < 1 || parseInt > 7) {
				return ResultGenerator.genResult(LotteryResultEnum.BET_PLAY_ENABLE.getCode(), LotteryResultEnum.BET_PLAY_ENABLE.getMsg());
			}
		} catch (NumberFormatException e) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_PLAY_ENABLE.getCode(), LotteryResultEnum.BET_PLAY_ENABLE.getMsg());
		}
		//2 1
		if(Integer.valueOf(playType).equals(MatchPlayTypeEnum.PLAY_TYPE_TSO.getcode())) {
			return ResultGenerator.genFailResult("暂不支持该玩法！", null);
		}
		//校验赛事投注时间
		MatchBetPlayDTO min = matchBetPlays.get(0);
		if(matchBetPlays.size() > 1) {
			min = matchBetPlays.stream().min((cell1,cell2)->cell1.getMatchTime()-cell2.getMatchTime()).get();
		}
		log.info("比赛时间getMatchTime========++++++++++++++=========="+min.getMatchTime());
//		int betEndTime = min.getMatchTime() - ProjectConstant.BET_PRESET_TIME;
		MatchTimePream matchTimePream =new MatchTimePream();
		matchTimePream.setMatchTime(min.getMatchTime());
		Integer betEndTime = lotteryMatchService.getBetEndTime(matchTimePream).getData();
		log.info("投注结束时间betEndTime========+++++++++++=========="+betEndTime);
		Date now = new Date();
		log.info("当前时间now========+++++++++++=========="+now);
		int nowTime = Long.valueOf(now.toInstant().getEpochSecond()).intValue();
		if(nowTime - betEndTime > 0) {
			log.info("当前时间减去投注结束时间========+++++++++++=========="+(nowTime - betEndTime));
			return ResultGenerator.genResult(LotteryResultEnum.BET_TIME_LIMIT.getCode(), LotteryResultEnum.BET_TIME_LIMIT.getMsg());
		}
		IsHideParam isHideParam =new IsHideParam();
		isHideParam.setBetEndTime(betEndTime);
		isHideParam.setMatchTime(min.getMatchTime());
		boolean hideMatch = lotteryMatchService.isHideMatch(isHideParam).getData();
		if(hideMatch) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_TIME_LIMIT.getCode(), LotteryResultEnum.BET_TIME_LIMIT.getMsg());
		}
		//校验串关
		String betTypeStr = param.getBetType();
		if(StringUtils.isBlank(betTypeStr)) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_PLAY_TYPE_ENABLE.getCode(), LotteryResultEnum.BET_PLAY_TYPE_ENABLE.getMsg());
		}
		
		boolean isCellError = false;
		boolean isAllSingle = true;
		for(MatchBetPlayDTO betPlay : matchBetPlays){
			List<MatchBetCellDTO> matchBetCells = betPlay.getMatchBetCells();
			if(CollectionUtils.isEmpty(matchBetCells)) {
				isCellError = true;
				break;
			}
			for(MatchBetCellDTO betCell: matchBetCells){
				List<DlJcZqMatchCellDTO> betCells = betCell.getBetCells();
				if(CollectionUtils.isEmpty(betCells)) {
					isCellError = true;
					break;
				}
				for(DlJcZqMatchCellDTO dto: betCells) {
					String cellCode = dto.getCellCode();
					String cellName = dto.getCellName();
					String cellOdds = dto.getCellOdds();
					if(StringUtils.isBlank(cellCode) || StringUtils.isBlank(cellName) || StringUtils.isBlank(cellOdds)) {
						isCellError = true;
						break;
					}
				}
				Integer single = betCell.getSingle();
				if(single == null || single.equals(0)) {
					isAllSingle = false;
				}
				if(isCellError) {
					break;
				}
			}
			if(isCellError) {
				break;
			}
		}
		//校验投注选项
		if(isCellError) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_CELL_HAS_NULL.getCode(), LotteryResultEnum.BET_CELL_HAS_NULL.getMsg());
		}
		if(betTypeStr.contains("11")) {
			if(!isAllSingle) {
				return ResultGenerator.genResult(LotteryResultEnum.BET_CELL_NO_SINGLE.getCode(), LotteryResultEnum.BET_CELL_NO_SINGLE.getMsg());
			}
		}
		String[] betTypes = betTypeStr.split(",");
		boolean isCheckedBetType = true;
		int minBetNum = 9;
		try {
			int maxBetNum = 1;
			for(String betType: betTypes) {
				char[] charArray = betType.toCharArray();
				if(charArray.length == 2 && charArray[1] == '1') {
					int num = Integer.valueOf(String.valueOf(charArray[0]));
					if(num > maxBetNum) {
						maxBetNum = num;
					}
					if(minBetNum > num) {
						minBetNum = num;
					}
					if(num < 1 || num > 8) {
						isCheckedBetType = false;
					}
				}
			}
			if(maxBetNum > matchBetPlays.size()) {
				isCheckedBetType = false;
			}
		} catch (NumberFormatException e) {
		}
		if(!isCheckedBetType) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_PLAY_TYPE_ENABLE.getCode(), LotteryResultEnum.BET_PLAY_TYPE_ENABLE.getMsg());
		}
		//校验胆的个数设置
		int danEnableNum = minBetNum -1;
		if(minBetNum == matchBetPlays.size()) {
			danEnableNum = 0;
		}
		long danNum = matchBetPlays.stream().filter(dto->dto.getIsDan() == 1).count();
		if(danNum > danEnableNum) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_CELL_DAN_ERR.getCode(), LotteryResultEnum.BET_CELL_DAN_ERR.getMsg());
		}
		//投注计算
		DLZQBetInfoDTO betInfo = lotteryMatchService.getBetInfo1(param).getData();
		if(Double.valueOf(betInfo.getMaxLotteryMoney()) >= 20000) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_MONEY_LIMIT.getCode(), LotteryResultEnum.BET_MONEY_LIMIT.getMsg());
		}
		int betNum = betInfo.getBetNum();
		if(betNum >= 10000 || betNum < 0) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_NUMBER_LIMIT.getCode(), LotteryResultEnum.BET_NUMBER_LIMIT.getMsg());
		}
		String betMoney = betInfo.getMoney();
		Double orderMoney = Double.valueOf(betMoney);
		Double minBetMoney = lotteryMatchService.getMinBetMoney(emptyParam).getData();
		if(orderMoney < minBetMoney) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_MATCH_WC.getCode(), "最低投注"+minBetMoney.intValue()+"元!");
		}
		int canBetMoney = lotteryMatchService.canBetMoney(emptyParam).getData();
		if(orderMoney > canBetMoney) {
			return ResultGenerator.genResult(LotteryResultEnum.BET_MATCH_STOP.getCode(), LotteryResultEnum.BET_MATCH_STOP.getMsg());
		}
		
		//缓存订单支付信息
		UserBetPayInfoDTO dto = new UserBetPayInfoDTO();
		List<UserBetDetailInfoDTO> betDetailInfos = new ArrayList<UserBetDetailInfoDTO>(matchBetPlays.size());
		for(MatchBetPlayDTO matchCell: matchBetPlays) {
			UserBetDetailInfoDTO dizqUserBetCellInfoDTO = new UserBetDetailInfoDTO();
			dizqUserBetCellInfoDTO.setMatchId(matchCell.getMatchId());
			dizqUserBetCellInfoDTO.setChangci(matchCell.getChangci());
			dizqUserBetCellInfoDTO.setIsDan(matchCell.getIsDan());
			dizqUserBetCellInfoDTO.setLotteryClassifyId(matchCell.getLotteryClassifyId());
			dizqUserBetCellInfoDTO.setLotteryPlayClassifyId(matchCell.getLotteryPlayClassifyId());
			dizqUserBetCellInfoDTO.setMatchTeam(matchCell.getMatchTeam());
			dizqUserBetCellInfoDTO.setMatchTime(matchCell.getMatchTime());
			String playCode = matchCell.getPlayCode();
			dizqUserBetCellInfoDTO.setPlayCode(playCode);
			List<MatchBetCellDTO> matchBetCells = matchCell.getMatchBetCells();
			String ticketData = matchBetCells.stream().map(betCell->{
				String ticketData1 = "0" + betCell.getPlayType() + "|" + playCode + "|";
				return ticketData1 + betCell.getBetCells().stream().map(cell->cell.getCellCode()+"@"+cell.getCellOdds())
						.collect(Collectors.joining(","));
			}).collect(Collectors.joining(";"));
			dizqUserBetCellInfoDTO.setTicketData(ticketData);;
			Optional<MatchBetCellDTO> findFirst = matchCell.getMatchBetCells().stream().filter(item->Integer.valueOf(item.getPlayType()).equals(MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode())).findFirst();
			if(findFirst.isPresent()) {
				String fixOdds = findFirst.get().getFixedOdds();
				logger.info("**************************fixOdds="+fixOdds);
				dizqUserBetCellInfoDTO.setFixedodds(fixOdds);
			}
			betDetailInfos.add(dizqUserBetCellInfoDTO);
		}
		dto.setTimes(param.getTimes());
		dto.setBetType(param.getBetType());
		dto.setPlayType(param.getPlayType());
		dto.setLotteryClassifyId(param.getLotteryClassifyId());
		dto.setLotteryPlayClassifyId(param.getLotteryPlayClassifyId());
		dto.setBetDetailInfos(betDetailInfos);
		dto.setBetNum(betNum);
		dto.setTicketNum(betInfo.getTicketNum());
		dto.setOrderMoney(orderMoney);
//		dto.setBonusAmount(bonusAmount);
//		dto.setBonusId(bonusId);
//		dto.setSurplus(surplus);
		String forecastMoney = betInfo.getMinBonus() + "~" + betInfo.getMaxBonus();
		dto.setForecastMoney(forecastMoney);
//		dto.setThirdPartyPaid(thirdPartyPaid);
		String requestFrom = "0";
		UserDeviceInfo userDevice = SessionUtil.getUserDevice();
		if(userDevice != null) {
			requestFrom = userDevice.getPlat();
		}
		dto.setRequestFrom(requestFrom);
		dto.setUserId(SessionUtil.getUserId());
		dto.setIssue(betInfo.getIssue());
		String dtoJson = JSONHelper.bean2json(dto);
		String keyStr = "bet_info_" + SessionUtil.getUserId() +"_"+ System.currentTimeMillis();
		String payToken = MD5.crypt(keyStr);
		stringRedisTemplate.opsForValue().set(payToken, dtoJson, ProjectConstant.BET_INFO_EXPIRE_TIME, TimeUnit.MINUTES);
		return ResultGenerator.genSuccessResult("success", payToken);
	}
	*/
}
 