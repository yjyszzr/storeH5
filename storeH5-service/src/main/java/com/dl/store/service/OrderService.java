package com.dl.store.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.enums.MatchBetTypeEnum;
import com.dl.base.enums.MatchPlayTypeEnum;
import com.dl.base.enums.MatchResultCrsEnum;
import com.dl.base.enums.MatchResultHadEnum;
import com.dl.base.enums.MatchResultHafuEnum;
import com.dl.base.enums.RespStatusEnum;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.exception.ServiceException;
import com.dl.base.model.UserDeviceInfo;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.SNGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.lottery.api.ILotteryMatchService;
import com.dl.lottery.dto.DLBetMatchCellDTO;
import com.dl.lottery.dto.DLZQBetInfoDTO;
import com.dl.lottery.dto.DLZQOrderLotteryBetInfoDTO;
import com.dl.lottery.param.DlPlayCodeParam;
import com.dl.lottery.param.GetBetInfoByOrderSn;
import com.dl.member.param.SysConfigParam;
import com.dl.order.dto.OrderDetailDTO;
import com.dl.order.dto.OrderDetailDTO.Cathectic;
import com.dl.order.dto.OrderDetailDTO.CathecticResult;
import com.dl.order.dto.OrderDetailDTO.MatchInfo;
import com.dl.order.param.OrderDataParam;
import com.dl.order.param.OrderDetailByOrderSnPara;
import com.dl.order.param.OrderInfoListParam;
import com.dl.order.param.TicketSchemeParam;
import com.dl.store.core.ProjectConstant;
import com.dl.store.dao.DlPrintLotteryMapper;
import com.dl.store.dao.OrderWithUserMapper;
import com.dl.store.dao2.DlLeagueMatchResultMapper;
import com.dl.store.dao3.OrderDetailMapper;
import com.dl.store.dao.OrderMapper;
import com.dl.store.dto.LotteryPrintDTO;
import com.dl.store.dto.OrderAppendInfoDTO;
import com.dl.store.dto.OrderDTO;
import com.dl.store.dto.OrderDetailDataDTO;
import com.dl.store.dto.OrderInfoAndDetailDTO;
import com.dl.store.dto.OrderInfoDTO;
import com.dl.store.dto.OrderInfoListDTO;
import com.dl.store.dto.OrderWithUserDTO;
import com.dl.store.dto.SysConfigDTO;
import com.dl.store.dto.TicketSchemeDTO;
import com.dl.store.dto.TicketSchemeDTO.TicketSchemeDetailDTO;
import com.dl.store.dto.UserIdAndRewardDTO;
import com.dl.store.enums.OrderExceptionEnum;
import com.dl.store.exception.SubmitOrderException;
import com.dl.store.exception.UserAccountException;
import com.dl.store.model.DlLeagueMatchResult;
import com.dl.store.model.DlPrintLottery;
import com.dl.store.model.DlUserAuths;
import com.dl.store.model.LotteryClassifyTemp;
import com.dl.store.model.LotteryPlayClassifyTemp;
import com.dl.store.model.Order;
import com.dl.store.model.OrderDetail;
import com.dl.store.model.PlayTypeName;
import com.dl.store.param.OrderDetailParam;
import com.dl.store.param.SubmitOrderParam;
import com.dl.store.param.SubmitOrderParam.TicketDetail;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional("transactionManager1")
public class OrderService extends AbstractService<Order> {
	
	@Value("${dl.img.file.pre.url}")
	private String imgFilePreUrl;
	

	@Resource
	private DlUserAuthsService dlUserAuthsService;
	
	@Resource
	private UserAccountService userAccountService;
	
	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderDetailMapper orderDetailMapper;
	
    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private  ILotteryMatchService lotteryMatchService;
 
	@Resource
	private DlPrintLotteryService dlPrintLotteryService;
    
	@Resource
	private DlPrintLotteryMapper dlPrintLotteryMapper;

	@Resource
	private DlLeagueMatchResultMapper dlLeagueMatchResultMapper;
	
	@Resource
	private OrderWithUserMapper orderWithUserMapper;
 

    /**
     * 更新订单为已支付
     * @param orderSn
     * @return
     */
	@Transactional("transactionManager1")
    public boolean updatePayStatus(String orderSn) {
    	boolean succ = false;
    	Order order = new Order();
    	order.setOrderSn(orderSn);
    	order.setOrderStatus(3);
    	order.setPayStatus(1);
    	order.setPayCode("store");
    	order.setPayTime(DateUtil.getCurrentTimeLong());
    	int cnt = orderMapper.updatePayStatusByOrderSn(order);
    	if(cnt > 0) {
    		succ = true;
    	}
    	return succ;
    }
    
    /**
     * 订单是否已支付
     * @param orderSn
     * @return
     */
    @Transactional("transactionManager1")
    public boolean isOrderPayed(String orderSn) {
    	boolean isPaid = false;
    	Order order = orderMapper.getOrderInfoByOrderSn(orderSn);
    	if(order != null && order.getPayStatus() != null && order.getPayStatus() == 1) {
    		isPaid = true;
    	}
    	return isPaid;
    }
    
	/**
	 * 创建订单
	 * @param param
	 * @return
	 */
	public BaseResult<OrderDTO> createOrder(SubmitOrderParam param) {
		log.info("创建订单,param={}", param);
		BaseResult<OrderDTO> result = validateAvailability(param);
		if (!result.isSuccess()) {
			return result;
		}
		// 生成订单号
		String sn = SNGenerator.nextSN(SNBusinessCodeEnum.ORDER_SN.getCode());
		boolean isSuccessed = true;
		Order order = null;
		Integer userId = SessionUtil.getUserId();
		OrderDTO orderDTO = new OrderDTO();
		try {
			// 保存订单及商品
			order = saveOrder(param, userId, sn);
			BeanUtils.copyProperties(orderDTO, order);
		} catch (UserAccountException e) {
			log.error("创建订单失败，异常信息：{}", e.getMessage());
			isSuccessed = false;
			e.printStackTrace();
			throw new SubmitOrderException(e.getCode(), e.getMsg());
		} catch (ServiceException e) {
			log.error("创建订单失败，异常信息：{}", e.getMessage());
			isSuccessed = false;
			e.printStackTrace();
			throw new SubmitOrderException(e.getCode(), e.getMsg());
		} catch (Exception e) {
			log.error("创建订单失败，异常信息：{}", e.getMessage());
			isSuccessed = false;
			e.printStackTrace();
			throw new ServiceException(OrderExceptionEnum.SUBMIT_ERROR.getCode(), OrderExceptionEnum.SUBMIT_ERROR.getMsg());
		} finally {
			if(!isSuccessed){
				int updateRowNum = orderMapper.deleteOrderByOrderSn(order.getOrderSn());
				log.error("下单失败，设置delete，订单号orderSn={},updateRow={}",order.getOrderSn(),updateRowNum);
			}
			log.info("下单流程，是否成功：{},参数：{}", isSuccessed, JSON.toJSONString(order));
		}
		return ResultGenerator.genSuccessResult("订单创建成功", orderDTO);
	}


	/***
	 * 根据订单号获取订单信息
	 * @param orderSn
	 * @return
	 */
	@Transactional("transactionManager1")
	public Order queryOrderByOrderSn(String orderSn) {
		Order order = orderMapper.getOrderInfoByOrderSn(orderSn);
		return order;
	}
	
	/**
	 * 校验参数及是否可以创建订单
	 * 
	 * @param param
	 */
	private BaseResult<OrderDTO> validateAvailability(SubmitOrderParam param) {
		List<TicketDetail> ticketDetailDTOs = param.getTicketDetails();
		if (CollectionUtils.isEmpty(ticketDetailDTOs)) {
			return ResultGenerator.genFailResult("创建订单失败，请稍后重试");
		}
		return ResultGenerator.genSuccessResult();
	}

	/**
	 * 保存订单及商品
	 * 
	 * @param param
	 * @param sn
	 * @return
	 */
	private Order saveOrder(SubmitOrderParam param, Integer userId, String sn) {
		Order order = getOrderData(param, userId, sn);
		UserDeviceInfo userDevice = SessionUtil.getUserDevice();
		if (userDevice != null) {
			String channel = userDevice.getChannel();
			order.setDeviceChannel(channel);
		}
		// 保存订单
		orderMapper.insertOrder(order);
		// 保存订单商品
		List<OrderDetail> orderDetails = getOrderDetailData(param, order.getOrderId(), order.getOrderSn(), userId);
		orderDetailMapper.insertList(orderDetails);

		return order;
	}
 

	/**
	 * 组装订单详情数据
	 * 
	 * @param param
	 * @return
	 */
	private List<OrderDetail> getOrderDetailData(SubmitOrderParam param, Integer orderId, String orderSn, Integer userId) {
		List<OrderDetail> orderDetails = new ArrayList<OrderDetail>();
		for (TicketDetail td : param.getTicketDetails()) {
			OrderDetail orderDetail = new OrderDetail();
			orderDetail.setGiveIntegral(0);
			orderDetail.setIsGuess(0);
			orderDetail.setOrderId(orderId);
			orderDetail.setOrderSn(orderSn);
			orderDetail.setTicketData(td.getTicketData());
			orderDetail.setMatchId(td.getMatch_id());
			orderDetail.setChangci(td.getChangci());
			orderDetail.setIssue(td.getIssue());
			orderDetail.setMatchTeam(td.getMatchTeam());
			orderDetail.setMatchResult(td.getMatchResult());
			orderDetail.setTicketStatus(ProjectConstant.TICKET_STATUS_NOT);
			orderDetail.setUserId(userId);
			orderDetail.setLotteryClassifyId(td.getLotteryClassifyId());
			orderDetail.setLotteryPlayClassifyId(td.getLotteryPlayClassifyId());
			orderDetail.setIsDan(td.getIsDan());
			orderDetail.setMatchTime(td.getMatchTime());
			orderDetail.setAddTime(DateUtil.getCurrentTimeLong());
			orderDetail.setFixedodds(td.getFixedodds());
			orderDetail.setBetType(td.getBetType());
			orderDetails.add(orderDetail);
		}
		return orderDetails;
	}
	

	/**
	 * 组装订单数据
	 * 
	 * @param param
	 * @return
	 */
	private Order getOrderData(SubmitOrderParam param, Integer userId, String orderSn) {
		Order order = new Order();
		order.setGiveIntegral(0);
		order.setIsDelete(0);
		order.setMoneyPaid(param.getMoneyPaid());
		order.setOrderFrom(param.getOrderFrom());
		order.setOrderSn(orderSn);
		order.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT_PAY);
		order.setOrderType(0);
		order.setParentSn("");
		order.setPayCode("");
		order.setPayId(0);
		order.setPayName(StringUtils.isBlank(param.getPayName()) ? "" : param.getPayName());
		order.setPaySn("");
		order.setPayStatus(ProjectConstant.PAY_STATUS_STAY);
		order.setPayTime(0);
		order.setSurplus(param.getSurplus());
		order.setTicketAmount(param.getTicketAmount());
		order.setUserBonusId(param.getUserBonusId());
		order.setBonus(param.getBonusAmount());
		order.setUserId(userId);
		order.setUserSurplus(new BigDecimal("0.00"));
		order.setUserSurplusLimit(new BigDecimal("0.00"));
		order.setThirdPartyPaid(param.getThirdPartyPaid());
		order.setAddTime(DateUtil.getCurrentTimeLong());
		order.setLotteryClassifyId(param.getLotteryClassifyId());
		order.setLotteryPlayClassifyId(param.getLotteryPlayClassifyId());
		order.setMatchTime(param.getMatchTime());
		order.setWinningMoney(new BigDecimal("0.00"));
		order.setPassType(param.getPassType());
		order.setPlayType(param.getPlayType());
		order.setCathectic(param.getCathectic());
		order.setBetNum(param.getBetNum());
		order.setAcceptTime(0);
		order.setTicketTime(0);
		order.setForecastMoney(param.getForecastMoney());
		order.setIssue(param.getIssue());
		order.setTicketNum(param.getTicketNum());
		order.setStoreId(Integer.parseInt(param.getStoreId()));
		return order;
	}

	public PageInfo<OrderInfoListDTO> getOrderInfoList(OrderInfoListParam param) {

		List<Integer> statusList = new ArrayList<Integer>();
		if ("-1".equals(param.getOrderStatus())) {
			statusList.add(ProjectConstant.ORDER_STATUS_NOT_PAY);
			statusList.add(ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY);
			statusList.add(ProjectConstant.ORDER_STATUS_FAIL_LOTTERY);
			statusList.add(ProjectConstant.ORDER_STATUS_STAY);
			statusList.add(ProjectConstant.ORDER_STATUS_NOT);
			statusList.add(ProjectConstant.ORDER_STATUS_ALREADY);
			statusList.add(ProjectConstant.ORDER_STATUS_REWARDING);
			statusList.add(ProjectConstant.ORDER_STATUS_REWARDED);
			statusList.add(ProjectConstant.ORDER_STATUS_AWARD_SENDED);
		} else if (ProjectConstant.ORDER_STATUS_ALREADY.equals(Integer.valueOf(param.getOrderStatus()))) {
			statusList.add(ProjectConstant.ORDER_STATUS_ALREADY);
			statusList.add(ProjectConstant.ORDER_STATUS_REWARDING);
			statusList.add(ProjectConstant.ORDER_STATUS_REWARDED);
		} else {
			statusList.add(Integer.valueOf(param.getOrderStatus()));
		}
		if (null == param.getPageNum())
			param.setPageNum("1");
		if (null == param.getPageSize())
			param.setPageSize("20");
		PageHelper.startPage(Integer.valueOf(param.getPageNum()), Integer.valueOf(param.getPageSize()));
		
		log.info("获取订单列表所用的用户Id==========={}", SessionUtil.getUserId()); 
		DlUserAuths userAuths =dlUserAuthsService.queryBindsUser(SessionUtil.getUserId());
		 List<Integer>  userIdList =new ArrayList<Integer>();
		userIdList.add(SessionUtil.getUserId());
		if (null!=userAuths) {
			userIdList.add(userAuths.getThirdUserId());
			log.info("获取订单列表所用的===第三方===用户Id==========={}", userAuths.getThirdUserId()); 
		}
		List<Integer>  storeIdList =new ArrayList<Integer>();
		storeIdList.add(Integer.parseInt(param.getStoreId()));
		storeIdList.add(-1);
		log.info("statusList状态列表======================================================={}", statusList); 
		log.info("userIdList用户列表======================================================={}", userIdList); 
		log.info("storeIdList店铺列表======================================================={}", storeIdList); 
		List<OrderInfoListDTO> orderInfoListDTOs = orderMapper.getOrderInfoList(statusList, userIdList,storeIdList );
		orderInfoListDTOs.forEach(item->{if(item.getLotteryName()==null) {item.setLotteryName("竞彩足球");}});
		if (CollectionUtils.isNotEmpty(orderInfoListDTOs)) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (OrderInfoListDTO orderInfoListDTO : orderInfoListDTOs) {
				if (0 == orderInfoListDTO.getLotteryClassifyId()) {
					orderInfoListDTO.setLotteryClassifyId(1);
				}
				Date matchTime = null;
				try {
					matchTime = sdf.parse(orderInfoListDTO.getMatchTime());
					orderInfoListDTO.setMatchTime(sdf.format(matchTime));
					if (!"0".equals(orderInfoListDTO.getPayTime())&&org.apache.commons.lang3.StringUtils.isNotBlank(orderInfoListDTO.getPayTime())) {
						long parseLong = Long.parseLong(orderInfoListDTO.getPayTime());
						if (parseLong > 0) {
							orderInfoListDTO.setPayTime(DateUtil.getCurrentTimeString(parseLong, DateUtil.datetimeFormat));
						}
					}else{
						long parseLong = Long.parseLong(orderInfoListDTO.getAddTime());
						orderInfoListDTO.setPayTime(DateUtil.getCurrentTimeString(parseLong, DateUtil.datetimeFormat));
					}
				} catch (ParseException e) {
					log.error("根据订单状态查询订单列表，时间转换异常");
					throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单列表查询失败");
				}
				Integer payStatus = orderInfoListDTO.getOrderPayStatus();
				Calendar calendar = Calendar.getInstance();
				if (ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					orderInfoListDTO.setOrderStatusInfo("资金已退回");
					orderInfoListDTO.setOrderStatusDesc("出票失败");
				} else if (ProjectConstant.ORDER_STATUS_STAY.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					long from = new Date().getTime();
					long to = matchTime.getTime();
					int days = (int) ((to - from) / (1000 * 60 * 60 * 24));
					if (days < 0) {
						orderInfoListDTO.setOrderStatusInfo("开奖失败");
						if (8 == orderInfoListDTO.getLotteryPlayClassifyId()) {
							orderInfoListDTO.setOrderStatusInfo("即将开奖");
						}
					} else if (days == 0) {
						orderInfoListDTO.setOrderStatusInfo("即将开奖");
					} else {
						orderInfoListDTO.setOrderStatusInfo(days + "天后开奖");
					}
					orderInfoListDTO.setOrderStatusDesc("待开奖");
				} else if (ProjectConstant.ORDER_STATUS_NOT.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					calendar.setTime(matchTime);
					calendar.add(Calendar.HOUR_OF_DAY, 2);// 开赛2小时后
					orderInfoListDTO.setOrderStatusInfo(calendar.get(Calendar.MONTH) + 1 + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日" + "开奖");
					orderInfoListDTO.setOrderStatusDesc("未中奖");
				} else if (ProjectConstant.ORDER_STATUS_ALREADY.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					orderInfoListDTO.setOrderStatusInfo("中奖金额：" + orderInfoListDTO.getWinningMoney());
					orderInfoListDTO.setOrderStatusDesc("已中奖");
				} else if (ProjectConstant.ORDER_STATUS_REWARDING.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus())) || ProjectConstant.ORDER_STATUS_REWARDED.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					orderInfoListDTO.setOrderStatusInfo("中奖金额：" + orderInfoListDTO.getWinningMoney());
					orderInfoListDTO.setOrderStatusDesc("派奖中");
				} else if (ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					orderInfoListDTO.setOrderStatusInfo("出票中");
					orderInfoListDTO.setOrderStatusDesc("待出票");
				} else if (ProjectConstant.ORDER_STATUS_NOT_PAY.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					orderInfoListDTO.setOrderStatusInfo("支付中");
					orderInfoListDTO.setOrderStatusDesc("支付中");
					if(payStatus.equals(ProjectConstant.PAY_STATUS_ALREADY)){
						orderInfoListDTO.setOrderStatusInfo("出票中");
						orderInfoListDTO.setOrderStatusDesc("待出票");
					}
				}else if(ProjectConstant.ORDER_STATUS_AWARD_SENDED.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))){
					orderInfoListDTO.setOrderStatusInfo("已派奖");
					orderInfoListDTO.setOrderStatusDesc("已派奖");
				}
			}
		}
		PageInfo<OrderInfoListDTO> pageInfo = new PageInfo<OrderInfoListDTO>(orderInfoListDTOs);
		return pageInfo;
	}

	public OrderDetailDTO getOrderDetail(OrderDetailParam param) {
		log.error("订单编号==============================================：" + param.getOrderSn() );
		if (StringUtils.isBlank(param.getOrderSn())) {
			log.error("订单编号：为空，该订单不存在");
			throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单不能为空");
		}
		OrderDetailDTO orderDetailDTO = new OrderDetailDTO();
		Order order  =	orderMapper.getOrderInfoByOrderSn(param.getOrderSn());
		if ( null ==order) {
			log.error("订单编号：" + param.getOrderSn() + "，该订单不存在");
			throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单不存在");
		}
		Integer lotteryClassifyId = order.getLotteryClassifyId();
		Integer lotteryPlayClassifyId = order.getLotteryPlayClassifyId();
		LotteryClassifyTemp lotteryClassify = orderDetailMapper.lotteryClassify(lotteryClassifyId);
		if (lotteryClassify != null) {
			orderDetailDTO.setLotteryClassifyImg(imgFilePreUrl+lotteryClassify.getLotteryImg());
			orderDetailDTO.setLotteryClassifyName(lotteryClassify.getLotteryName());
		} else {
			orderDetailDTO.setLotteryClassifyImg("");
			orderDetailDTO.setLotteryClassifyName("");
		}
		if (ProjectConstant.ORDER_STATUS_NOT_PAY.equals(order.getOrderStatus())&&!ProjectConstant.PAY_STATUS_ALREADY.equals(order.getPayStatus())) {
			orderDetailDTO.setProcessStatusDesc("支付中");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT_PAY.toString());
			orderDetailDTO.setOrderStatusDesc("支付中");
			orderDetailDTO.setProcessResult("支付中");
			orderDetailDTO.setForecastMoney("预测奖金" + order.getForecastMoney().toString());
		} else if ((ProjectConstant.ORDER_STATUS_NOT_PAY.equals(order.getOrderStatus())&&ProjectConstant.PAY_STATUS_ALREADY.equals(order.getPayStatus()))||
				ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("等待出票");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.toString());
			orderDetailDTO.setOrderStatusDesc("支付成功");
			orderDetailDTO.setProcessResult("支付成功");
			orderDetailDTO.setForecastMoney("预测奖金" + order.getForecastMoney().toString());
		} else if (ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("彩金已退回");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.toString());
			orderDetailDTO.setOrderStatusDesc("出票失败");
			orderDetailDTO.setProcessResult("");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_STAY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_STAY.toString());
			orderDetailDTO.setOrderStatusDesc("出票成功");
			orderDetailDTO.setProcessResult("等待开奖");
			orderDetailDTO.setForecastMoney("预测奖金" + order.getForecastMoney().toString());
		} else if (ProjectConstant.ORDER_STATUS_NOT.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("感谢您助力公益事业");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT.toString());
			orderDetailDTO.setOrderStatusDesc("未中奖");
			orderDetailDTO.setProcessResult("再接再厉");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_ALREADY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc(order.getWinningMoney().toString());
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_ALREADY.toString());
			orderDetailDTO.setOrderStatusDesc("已中奖");
			orderDetailDTO.setProcessResult("恭喜中奖");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_REWARDING.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc(order.getWinningMoney().toString());
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_REWARDING.toString());
			orderDetailDTO.setOrderStatusDesc("派奖中");
			orderDetailDTO.setProcessResult("派奖中");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_REWARDED.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc(order.getWinningMoney().toString());
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_REWARDED.toString());
			orderDetailDTO.setOrderStatusDesc("派奖中");
			orderDetailDTO.setProcessResult("派奖中");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.toString());
			orderDetailDTO.setOrderStatusDesc("待出票");
			orderDetailDTO.setProcessResult("出票中");
			orderDetailDTO.setForecastMoney("");
		}
		BigDecimal moneyPaid = order.getMoneyPaid();
		orderDetailDTO.setMoneyPaid(moneyPaid != null ? moneyPaid.toString() : "");// 2018-05-13前端不变参数的情况下暂时使用原有参数,1.0.3更新为moneyPaid
		BigDecimal ticketAmount = order.getTicketAmount();
		orderDetailDTO.setTicketAmount(ticketAmount != null ? ticketAmount.toString() : "");
		BigDecimal surplus = order.getSurplus();
		orderDetailDTO.setSurplus(surplus != null ? surplus.toString() : "");
		BigDecimal userSurplusLimit = order.getUserSurplusLimit();
		orderDetailDTO.setUserSurplusLimit(userSurplusLimit != null ? userSurplusLimit.toString() : "");
		BigDecimal userSurplus = order.getUserSurplus();
		orderDetailDTO.setUserSurplus(userSurplus != null ? userSurplus.toString() : "");
		BigDecimal thirdPartyPaid = order.getThirdPartyPaid();
		orderDetailDTO.setThirdPartyPaid(thirdPartyPaid != null ? thirdPartyPaid.toString() : "");
		BigDecimal bonus = order.getBonus();
		orderDetailDTO.setBonus(bonus != null ? bonus.toString() : "");

		orderDetailDTO.setBetNum(order.getBetNum());
		orderDetailDTO.setPayName(order.getPayName());
		orderDetailDTO.setPassType(getPassType(order.getPassType()));
		orderDetailDTO.setCathectic(order.getCathectic());
		orderDetailDTO.setPlayType(order.getPlayType().replaceAll("0", ""));
		orderDetailDTO.setLotteryClassifyId(String.valueOf(lotteryClassifyId));
		orderDetailDTO.setLotteryPlayClassifyId(String.valueOf(lotteryPlayClassifyId));
		orderDetailDTO.setProgrammeSn(order.getOrderSn());
		orderDetailDTO.setCreateTime(DateUtil.getCurrentTimeString(order.getAddTime().longValue(), DateUtil.datetimeFormat));
		long acceptTime = order.getAcceptTime().longValue();
		if (acceptTime > 0) {
			orderDetailDTO.setAcceptTime(DateUtil.getCurrentTimeString(acceptTime, DateUtil.datetimeFormat));
		} else {
			orderDetailDTO.setAcceptTime("--");
		}
		long ticketTime = order.getTicketTime().longValue();
		if (ticketTime > 0) {
			orderDetailDTO.setTicketTime(DateUtil.getCurrentTimeString(ticketTime, DateUtil.datetimeFormat));
		} else {
			orderDetailDTO.setTicketTime("--");
		}
		List<PlayTypeName> playTypes = orderDetailMapper.getPlayTypes(lotteryClassifyId);
		Map<Integer, String> playTypeNameMap = new HashMap<Integer, String>();
		if (!Collections.isEmpty(playTypes)) {
			for (PlayTypeName type : playTypes) {
				playTypeNameMap.put(type.getPlayType(), type.getPlayName());
			}
		}
		orderDetailDTO.setDetailType(0);
		boolean isWorlCup = false;
		if (lotteryClassifyId == 1 && lotteryPlayClassifyId == 8) {
			isWorlCup = true;
		}
		String redirectUrl = "";
		LotteryPlayClassifyTemp map = orderDetailMapper.lotteryPlayClassifyStatusAndUrl(lotteryClassifyId, lotteryPlayClassifyId);
		log.info("dddd  " + JSONHelper.bean2json(map));
		if (map != null) {
			int status = map.getStatus();
			if (status == 0) {
				redirectUrl = map.getRedirectUrl();
			}
		}
		orderDetailDTO.setRedirectUrl(redirectUrl);
		List<OrderDetail> orderDetails = orderDetailMapper.queryListByOrderSn(param.getOrderSn());
		log.info("orderDetails========================================================================={}",orderDetails);
		log.info("orderDetails========================================================================={}",orderDetails);
		log.info("orderDetails========================================================================={}",orderDetails);
		if (CollectionUtils.isNotEmpty(orderDetails)) {
			List<MatchInfo> matchInfos = new ArrayList<MatchInfo>();
			for (OrderDetail orderDetail : orderDetails) {
				MatchInfo matchInfo = new MatchInfo();
				matchInfo.setChangci(orderDetail.getChangci());
				String match = orderDetail.getMatchTeam();
				/*
				 * String[] matchs = match.split("VS"); match = matchs[0] + "VS"
				 * + matchs[1];
				 */
				match = match.replaceAll("\r\n", "");
				matchInfo.setMatch(match);
				Integer isDan = orderDetail.getIsDan();
				String isDanStr = isDan == null ? "0" : isDan.toString();
				matchInfo.setIsDan(isDanStr);
				String playType = orderDetail.getPlayType();
				String playName = playTypeNameMap.getOrDefault(Integer.valueOf(playType), playType);
				String fixedodds = orderDetail.getFixedodds();
				if (Integer.valueOf(playType).equals(MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode())) {
					playName = StringUtils.isBlank(fixedodds) ? playName : ("[" + fixedodds + "]" + playName);
				}
				matchInfo.setPlayType(playName);
				String matchResult = orderDetail.getMatchResult();
				if (ProjectConstant.ORDER_MATCH_RESULT_CANCEL.equals(matchResult)) {

				}
				if (!isWorlCup){
					matchInfo.setCathecticResults(this.getCathecticResults(fixedodds, orderDetail.getTicketData(), matchResult, playTypeNameMap));
				}
				matchInfos.add(matchInfo);
			}
			if (isWorlCup) {
				String changci = orderDetails.get(0).getChangci();
				int detailType = 1;
				if ("T57".equals(changci)) {
					detailType = 2;
				}
				orderDetailDTO.setDetailType(detailType);
			}
			orderDetailDTO.setMatchInfos(matchInfos);
		}
		orderDetailDTO.setOrderSn(order.getOrderSn());
		//添加好友图片二维码
//		SysConfigParam sysCfgParams = new SysConfigParam();
//		sysCfgParams.setBusinessId(17);
		 SysConfigDTO baseR = sysConfigService.querySysConfig(17);
		if(baseR != null) {
			String desc = baseR.getDescribtion();
			List<String> picList = JSONObject.parseArray(desc,String.class);
			String url = picList.get(0);
			orderDetailDTO.setAddFriendsQRBarUrl(url);
			log.info("[getOrderDetail]" + " desc url:" + baseR.getDescribtion());
		}

		List<OrderAppendInfoDTO> appendInfoList = new ArrayList<>();
		//是否展示店铺入口
		int showStore = 0;
//		sysCfgParams = new SysConfigParam();
//		sysCfgParams.setBusinessId(21);
		baseR = sysConfigService.querySysConfig(21);
		if(baseR != null) {
			BigDecimal val = baseR.getValue();
			showStore = val.intValue();
			OrderAppendInfoDTO appendDto = new OrderAppendInfoDTO();
			if(showStore == 1){
				appendDto.setType("0");
				appendDto.setImgurl("http://t1.caixiaomi.net:9809/uploadImgs/20180913/money_@2.gif");
				appendDto.setPhone("");
				appendDto.setPushurl("");
				appendDto.setWechat("");
				appendInfoList.add(appendDto);
			}else{
				SysConfigParam scfShop = new SysConfigParam();
//				sysCfgParams.setBusinessId(48);//是否展示店主信息
			 SysConfigDTO  baseShop = sysConfigService.querySysConfig(48);
					SysConfigDTO shopSysDto = baseShop;
					if(shopSysDto.getValue().intValue() == 1){
						appendDto.setType("1");
						appendDto.setImgurl("https://szcq-biz.oss-cn-beijing.aliyuncs.com/20181212184220.png");
						appendDto.setPhone("17718518356");
						appendDto.setPushurl("");
						appendDto.setWechat("xiancaipaidian");
						appendInfoList.add(appendDto);
				}
			}
		}
		
		//xxx
//		orderDetailDTO.setAppendInfoList(appendInfoList);
		orderDetailDTO.setShowStore(showStore);

		return orderDetailDTO;
	}
	/**
	 * 组装通过方式字符串
	 * 
	 * @param passType
	 * @return
	 */
	private String getPassType(String passType) {
		String[] passTypes = passType.split(",");
		String passTypeStr = "";
		for (int i = 0; i < passTypes.length; i++) {
			passTypeStr += MatchBetTypeEnum.getName(passTypes[i]) + ",";
		}
		return passTypeStr.substring(0, passTypeStr.length() - 1);
	}
	
	

	/**
	 * 组装投注、赛果列数据
	 * 
	 * @param ticketData
	 * @param matchResult
	 * @return
	 */
	private List<CathecticResult> getCathecticResults(String fixedodds, String ticketData, String matchResult, Map<Integer, String> types) {
		List<CathecticResult> cathecticResults = new LinkedList<CathecticResult>();
		if (StringUtils.isEmpty(ticketData))
			return cathecticResults;
		List<String> ticketDatas = Arrays.asList(ticketData.split(";"));
		List<String> matchResults = null;
		if (StringUtils.isNotEmpty(matchResult) && !ProjectConstant.ORDER_MATCH_RESULT_CANCEL.equals(matchResult)) {
			matchResults = Arrays.asList(matchResult.split(";"));
		}
		if (CollectionUtils.isNotEmpty(ticketDatas)) {
			for (String temp : ticketDatas) {
				CathecticResult cathecticResult = new CathecticResult();
				List<Cathectic> cathectics = new LinkedList<Cathectic>();
				String matchResultStr = "";
				String playType = temp.substring(0, temp.indexOf("|"));
				String playCode = temp.substring(temp.indexOf("|") + 1, temp.lastIndexOf("|"));
				String betCells = temp.substring(temp.lastIndexOf("|") + 1);
				String[] betCellArr = betCells.split(",");
				for (int i = 0; i < betCellArr.length; i++) {
					Cathectic cathectic = new Cathectic();
					String betCellCode = betCellArr[i].substring(0, betCellArr[i].indexOf("@"));
					String betCellOdds = betCellArr[i].substring(betCellArr[i].indexOf("@") + 1);
					String cathecticStr = getCathecticData(playType, betCellCode);
					cathectic.setCathectic(cathecticStr + "[" + String.format("%.2f", Double.valueOf(betCellOdds)) + "]");
					if (null != matchResults) {
						for (String matchStr : matchResults) {
							String rstPlayType = matchStr.substring(0, matchStr.indexOf("|"));
							String rstPlayCode = matchStr.substring(matchStr.indexOf("|") + 1, matchStr.lastIndexOf("|"));
							String rstPlayCells = matchStr.substring(matchStr.lastIndexOf("|") + 1);
							if (playType.equals(rstPlayType) && playCode.equals(rstPlayCode)) {
								if (rstPlayCells.equals(betCellCode)) {
									cathectic.setIsGuess("1");
								} else {
									cathectic.setIsGuess("0");
								}
								matchResultStr = getCathecticData(playType, rstPlayCells);
							}
						}
					} else {
						cathectic.setIsGuess("0");
					}
					cathectics.add(cathectic);
				}
				if (StringUtils.isBlank(matchResultStr)) {
					matchResultStr = "待定";
				}
				if (ProjectConstant.ORDER_MATCH_RESULT_CANCEL.equals(matchResult)) {
					matchResultStr = "#?";
				}
				String playName = types.getOrDefault(Integer.valueOf(playType), playType);
				if (Integer.valueOf(playType).equals(MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode())) {
					playName = StringUtils.isBlank(fixedodds) ? playName : ("[" + fixedodds + "]" + playName);
				}
				cathecticResult.setPlayType(playName);
				cathecticResult.setCathectics(cathectics);
				cathecticResult.setMatchResult(matchResultStr);
				cathecticResults.add(cathecticResult);
			}
		}
		return cathecticResults;
	}
	
	/**
	 * 通过玩法code与投注内容，进行转换
	 * 
	 * @param playCode
	 * @param cathecticStr
	 * @return
	 */
	private String getCathecticData(String playTypeStr, String cathecticStr) {
		int playType = Integer.parseInt(playTypeStr);
		String cathecticData = "";
		if (MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode() == playType || MatchPlayTypeEnum.PLAY_TYPE_HAD.getcode() == playType) {
			cathecticData = MatchResultHadEnum.getName(Integer.valueOf(cathecticStr));
		} else if (MatchPlayTypeEnum.PLAY_TYPE_CRS.getcode() == playType) {
			cathecticData = MatchResultCrsEnum.getName(cathecticStr);
		} else if (MatchPlayTypeEnum.PLAY_TYPE_TTG.getcode() == playType) {
			cathecticData = cathecticStr + "球";
			if ("7".equals(cathecticStr)) {
				cathecticData = cathecticStr + "+球";
			}
		} else if (MatchPlayTypeEnum.PLAY_TYPE_HAFU.getcode() == playType) {
			cathecticData = MatchResultHafuEnum.getName(cathecticStr);
		}
		return cathecticData;
	}
	

	/**
	 * 查询出票方案
	 * 
	 * @param param
	 * @return
	 */
	public TicketSchemeDTO getTicketScheme(TicketSchemeParam param) {
		TicketSchemeDTO ticketSchemeDTO = new TicketSchemeDTO();
		ticketSchemeDTO.setProgrammeSn(param.getProgrammeSn());
		Order orderInfoByOrderSn = orderMapper.getOrderInfoByOrderSn(param.getOrderSn());
		Integer orderStatus = orderInfoByOrderSn.getOrderStatus();
		Integer times = orderInfoByOrderSn.getCathectic();
		if (orderStatus < 10 && orderStatus > 2) {
			List<TicketSchemeDetailDTO> ticketSchemeDetailDTOs = new ArrayList<TicketSchemeDetailDTO>();
			Integer lotteryClassifyId = orderInfoByOrderSn.getLotteryClassifyId();
			Integer lotteryPlayClassifyId = orderInfoByOrderSn.getLotteryPlayClassifyId();
			if (8 == lotteryPlayClassifyId && 1 == lotteryClassifyId) {
				List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(orderInfoByOrderSn.getOrderId(), orderInfoByOrderSn.getUserId());
				int i = 0;
				for (OrderDetail detail : orderDetails) {
					TicketSchemeDetailDTO ticketSchemeDetailDTO = new TicketSchemeDetailDTO();
					ticketSchemeDetailDTO.setNumber(String.valueOf(++i));
					String playType = "冠军";
					if ("T57".equals(detail.getChangci())) {
						playType = "冠亚军";
					}
					String ticketContent = playType + "(" + detail.getMatchTeam() + "[" + detail.getTicketData().split("@")[1] + "])";
					ticketSchemeDetailDTO.setTickeContent(ticketContent);
					ticketSchemeDetailDTO.setPassType("");
					ticketSchemeDetailDTO.setMultiple(times.toString());
					ticketSchemeDetailDTO.setStatus(1);
					ticketSchemeDetailDTOs.add(ticketSchemeDetailDTO);
				}
			} else {
				GetBetInfoByOrderSn getBetInfoByOrderSn = new GetBetInfoByOrderSn();
				getBetInfoByOrderSn.setOrderSn(param.getOrderSn());
				BaseResult<DLZQBetInfoDTO> result = lotteryMatchService.getBetInfoByOrderSn(getBetInfoByOrderSn);
				if (result.getCode() == 0) {
					DLZQBetInfoDTO dLZQBetInfoDTO = result.getData();
					if (null != dLZQBetInfoDTO) {
						List<DLZQOrderLotteryBetInfoDTO> orderLotteryBetInfos = dLZQBetInfoDTO.getBetCells();
						if (CollectionUtils.isNotEmpty(orderLotteryBetInfos)) {
							orderLotteryBetInfos.forEach(betInfo -> {
								Integer status = betInfo.getStatus();
								List<DLBetMatchCellDTO> dLBetMatchCellDTOs = betInfo.getBetCells();
								if (CollectionUtils.isNotEmpty(orderLotteryBetInfos)) {
									for (int i = 0; i < dLBetMatchCellDTOs.size(); i++) {
										DLBetMatchCellDTO dLBetMatchCellDTO = dLBetMatchCellDTOs.get(i);
										TicketSchemeDetailDTO ticketSchemeDetailDTO = new TicketSchemeDetailDTO();
										ticketSchemeDetailDTO.setNumber(String.valueOf(i + 1));
										ticketSchemeDetailDTO.setTickeContent(dLBetMatchCellDTO.getBetContent());
										ticketSchemeDetailDTO.setPassType(getPassType(dLBetMatchCellDTO.getBetType()));
										ticketSchemeDetailDTO.setMultiple(String.valueOf(dLBetMatchCellDTO.getTimes()));
										ticketSchemeDetailDTO.setStatus(status);
										ticketSchemeDetailDTOs.add(ticketSchemeDetailDTO);
									}
								}
							});
						}
					}
				}
			}
			ticketSchemeDTO.setTicketSchemeDetailDTOs(ticketSchemeDetailDTOs);
		} else if (orderStatus.equals(0)||orderStatus.equals(1)) {
			List<TicketSchemeDetailDTO> ticketSchemeDetailDTOs = new ArrayList<TicketSchemeDetailDTO>(1);
			TicketSchemeDetailDTO dto = new TicketSchemeDetailDTO();
			dto.setNumber("1");
			dto.setTickeContent("-");
			dto.setPassType("-");
			dto.setMultiple("-");
			dto.setStatus(0);
			ticketSchemeDetailDTOs.add(dto);
			ticketSchemeDTO.setTicketSchemeDetailDTOs(ticketSchemeDetailDTOs);
		} else {
			List<TicketSchemeDetailDTO> ticketSchemeDetailDTOs = new ArrayList<TicketSchemeDetailDTO>(1);
			TicketSchemeDetailDTO dto = new TicketSchemeDetailDTO();
			dto.setNumber("1");
			dto.setTickeContent("-");
			dto.setPassType("-");
			dto.setMultiple("-");
			dto.setStatus(2);
			ticketSchemeDetailDTOs.add(dto);
			ticketSchemeDTO.setTicketSchemeDetailDTOs(ticketSchemeDetailDTOs);
		}
		return ticketSchemeDTO;
	}

	public  OrderDetailDTO getOrderDetailByOrderSn(OrderDetailByOrderSnPara param) {
		if (StringUtils.isBlank(param.getOrderSn())) {
			log.error("订单ordersn：为空，该订单不存在");
			throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单不能为空");
		}
		OrderDetailDTO orderDetailDTO = new OrderDetailDTO();
		Order order = new Order();
		Integer userId = SessionUtil.getUserId();
		order.setUserId(userId);
		order.setOrderSn(param.getOrderSn());
		order = orderMapper.selectOne(order);
		if (null == order) {
			log.error("订单 order sn：" + param.getOrderSn() + "，该订单不存在");
			throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单不存在");
		}
		Integer lotteryClassifyId = order.getLotteryClassifyId();
		Integer lotteryPlayClassifyId = order.getLotteryPlayClassifyId();
		LotteryClassifyTemp lotteryClassify = orderDetailMapper.lotteryClassify(lotteryClassifyId);
		if (lotteryClassify != null) {
			orderDetailDTO.setLotteryClassifyImg(imgFilePreUrl+lotteryClassify.getLotteryImg());
			orderDetailDTO.setLotteryClassifyName(lotteryClassify.getLotteryName());
		} else {
			orderDetailDTO.setLotteryClassifyImg("");
			orderDetailDTO.setLotteryClassifyName("");
		}
		if (ProjectConstant.ORDER_STATUS_NOT_PAY.equals(order.getOrderStatus())&&!ProjectConstant.PAY_STATUS_ALREADY.equals(order.getPayStatus())) {
			orderDetailDTO.setProcessStatusDesc("支付中");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT_PAY.toString());
			orderDetailDTO.setOrderStatusDesc("支付中");
			orderDetailDTO.setProcessResult("处理中");
			orderDetailDTO.setForecastMoney("预测奖金" + order.getForecastMoney().toString());
		} else if ((ProjectConstant.ORDER_STATUS_NOT_PAY.equals(order.getOrderStatus())&&ProjectConstant.PAY_STATUS_ALREADY.equals(order.getPayStatus()))||
				ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("等待出票");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.toString());
			orderDetailDTO.setOrderStatusDesc("支付成功");
			orderDetailDTO.setProcessResult("处理中");
			orderDetailDTO.setForecastMoney("预测奖金" + order.getForecastMoney().toString());
		} else if (ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("彩金已退回");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.toString());
			orderDetailDTO.setOrderStatusDesc("出票失败");
			orderDetailDTO.setProcessResult("");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_STAY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_STAY.toString());
			orderDetailDTO.setOrderStatusDesc("出票成功");
			orderDetailDTO.setProcessResult("等待开奖");
			orderDetailDTO.setForecastMoney("预测奖金" + order.getForecastMoney().toString());
		} else if (ProjectConstant.ORDER_STATUS_NOT.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("感谢您助力公益事业");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT.toString());
			orderDetailDTO.setOrderStatusDesc("未中奖");
			orderDetailDTO.setProcessResult("再接再厉");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_ALREADY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc(order.getWinningMoney().toString());
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_ALREADY.toString());
			orderDetailDTO.setOrderStatusDesc("已中奖");
			orderDetailDTO.setProcessResult("恭喜中奖");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_REWARDING.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc(order.getWinningMoney().toString());
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_REWARDING.toString());
			orderDetailDTO.setOrderStatusDesc("派奖中");
			orderDetailDTO.setProcessResult("派奖中");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_REWARDED.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc(order.getWinningMoney().toString());
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_REWARDED.toString());
			orderDetailDTO.setOrderStatusDesc("派奖中");
			orderDetailDTO.setProcessResult("派奖中");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.toString());
			orderDetailDTO.setOrderStatusDesc("待出票");
			orderDetailDTO.setProcessResult("出票中");
			orderDetailDTO.setForecastMoney("");
		}
		BigDecimal moneyPaid = order.getMoneyPaid();
		orderDetailDTO.setMoneyPaid(moneyPaid != null ? moneyPaid.toString() : "");// 2018-05-13前端不变参数的情况下暂时使用原有参数,1.0.3更新为moneyPaid
		BigDecimal ticketAmount = order.getTicketAmount();
		orderDetailDTO.setTicketAmount(ticketAmount != null ? ticketAmount.toString() : "");
		BigDecimal surplus = order.getSurplus();
		orderDetailDTO.setSurplus(surplus != null ? surplus.toString() : "");
		BigDecimal userSurplusLimit = order.getUserSurplusLimit();
		orderDetailDTO.setUserSurplusLimit(userSurplusLimit != null ? userSurplusLimit.toString() : "");
		BigDecimal userSurplus = order.getUserSurplus();
		orderDetailDTO.setUserSurplus(userSurplus != null ? userSurplus.toString() : "");
		BigDecimal thirdPartyPaid = order.getThirdPartyPaid();
		orderDetailDTO.setThirdPartyPaid(thirdPartyPaid != null ? thirdPartyPaid.toString() : "");
		BigDecimal bonus = order.getBonus();
		orderDetailDTO.setBonus(bonus != null ? bonus.toString() : "");

		orderDetailDTO.setBetNum(order.getBetNum());
		orderDetailDTO.setPayName(order.getPayName());
		orderDetailDTO.setPassType(getPassType(order.getPassType()));
		orderDetailDTO.setCathectic(order.getCathectic());
		orderDetailDTO.setPlayType(order.getPlayType().replaceAll("0", ""));
		orderDetailDTO.setLotteryClassifyId(String.valueOf(lotteryClassifyId));
		orderDetailDTO.setLotteryPlayClassifyId(String.valueOf(lotteryPlayClassifyId));
		orderDetailDTO.setProgrammeSn(order.getOrderSn());
		orderDetailDTO.setCreateTime(DateUtil.getCurrentTimeString(order.getAddTime().longValue(), DateUtil.datetimeFormat));
		long acceptTime = order.getAcceptTime().longValue();
		if (acceptTime > 0) {
			orderDetailDTO.setAcceptTime(DateUtil.getCurrentTimeString(acceptTime, DateUtil.datetimeFormat));
		} else {
			orderDetailDTO.setAcceptTime("--");
		}
		long ticketTime = order.getTicketTime().longValue();
		if (ticketTime > 0) {
			orderDetailDTO.setTicketTime(DateUtil.getCurrentTimeString(ticketTime, DateUtil.datetimeFormat));
		} else {
			orderDetailDTO.setTicketTime("--");
		}
		List<PlayTypeName> playTypes = orderDetailMapper.getPlayTypes(lotteryClassifyId);
		Map<Integer, String> playTypeNameMap = new HashMap<Integer, String>();
		if (!Collections.isEmpty(playTypes)) {
			for (PlayTypeName type : playTypes) {
				playTypeNameMap.put(type.getPlayType(), type.getPlayName());
			}
		}
		orderDetailDTO.setDetailType(0);
		boolean isWorlCup = false;
		if (lotteryClassifyId == 1 && lotteryPlayClassifyId == 8) {
			isWorlCup = true;
		}
		String redirectUrl = "";
		LotteryPlayClassifyTemp map = orderDetailMapper.lotteryPlayClassifyStatusAndUrl(lotteryClassifyId, lotteryPlayClassifyId);
		log.info("dddd  " + JSONHelper.bean2json(map));
		if (map != null) {
			int status = map.getStatus();
			if (status == 0) {
				redirectUrl = map.getRedirectUrl();
			}
		}
		orderDetailDTO.setRedirectUrl(redirectUrl);
		List<OrderDetail> orderDetails = orderDetailMapper.queryListByOrderSn(param.getOrderSn());
		if (CollectionUtils.isNotEmpty(orderDetails)) {
			List<MatchInfo> matchInfos = new ArrayList<OrderDetailDTO.MatchInfo>();
			for (OrderDetail orderDetail : orderDetails) {
				OrderDetailDTO.MatchInfo matchInfo = new OrderDetailDTO.MatchInfo();
				matchInfo.setChangci(orderDetail.getChangci());
				String match = orderDetail.getMatchTeam();
				/*
				 * String[] matchs = match.split("VS"); match = matchs[0] + "VS"
				 * + matchs[1];
				 */
				match = match.replaceAll("\r\n", "");
				matchInfo.setMatch(match);
				Integer isDan = orderDetail.getIsDan();
				String isDanStr = isDan == null ? "0" : isDan.toString();
				matchInfo.setIsDan(isDanStr);
				String playType = orderDetail.getPlayType();
				String playName = playTypeNameMap.getOrDefault(Integer.valueOf(playType), playType);
				String fixedodds = orderDetail.getFixedodds();
				if (Integer.valueOf(playType).equals(MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode())) {
					playName = StringUtils.isBlank(fixedodds) ? playName : ("[" + fixedodds + "]" + playName);
				}
				matchInfo.setPlayType(playName);
				String matchResult = orderDetail.getMatchResult();
				if (ProjectConstant.ORDER_MATCH_RESULT_CANCEL.equals(matchResult)) {

				}
				if (isWorlCup) {
				} else {
					matchInfo.setCathecticResults(this.getCathecticResults(fixedodds, orderDetail.getTicketData(), matchResult, playTypeNameMap));
				}
				matchInfos.add(matchInfo);
			}
			if (isWorlCup) {
				String changci = orderDetails.get(0).getChangci();
				int detailType = 1;
				if ("T57".equals(changci)) {
					detailType = 2;
				}
				orderDetailDTO.setDetailType(detailType);
			}
			orderDetailDTO.setMatchInfos(matchInfos);
		}
		orderDetailDTO.setOrderSn(order.getOrderSn());
		return orderDetailDTO;
	}


	//更新订单的比赛结果
	public void updateOrderMatchResult() {
		List<OrderDetail> orderDetails = orderDetailMapper.unMatchResultOrderDetails();
		if (CollectionUtils.isEmpty(orderDetails)) {
			return;
		}
		Set<String> playCodesSet = orderDetails.stream().map(detail -> detail.getIssue()).collect(Collectors.toSet());
		List<String> playCodes = new ArrayList<String>(playCodesSet.size());
		playCodes.addAll(playCodesSet);
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size());
		DlPlayCodeParam playCodesParam  =new DlPlayCodeParam();
		playCodesParam.setPlayCodes(playCodes);
		 List<String>  cancelMatches = lotteryMatchService.getCancelMatches(playCodesParam).getData();
		List<DlLeagueMatchResult> matchResults = dlLeagueMatchResultMapper.queryMatchResultsByPlayCodes(playCodes);
		if (CollectionUtils.isEmpty(matchResults) && CollectionUtils.isEmpty(cancelMatches)) {
			log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 没有获取到相应的赛事结果信息及没有取消赛事");
			return;
		}
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 获取到相应的赛事结果信息数：" + matchResults.size() + " 取消赛事数：" + cancelMatches.size());
		Map<String, List<OrderDetail>> detailMap = new HashMap<String, List<OrderDetail>>();
		List<OrderDetail> cancelList = new ArrayList<OrderDetail>(orderDetails.size());
		for (OrderDetail orderDetail : orderDetails) {
			String playCode = orderDetail.getIssue();
			if (cancelMatches.contains(playCode)) {
				orderDetail.setMatchResult(ProjectConstant.ORDER_MATCH_RESULT_CANCEL);
				cancelList.add(orderDetail);
			} else {
				List<OrderDetail> list = detailMap.get(playCode);
				if (list == null) {
					list = new ArrayList<OrderDetail>();
					detailMap.put(playCode, list);
				}
				list.add(orderDetail);
			}
		}
		log.info("取消赛事对应订单详情数：cancelList。si'ze" + cancelList.size() + "  detailMap.size=" + detailMap.size());
		Map<String, List<DlLeagueMatchResult>> resultMap = new HashMap<String, List<DlLeagueMatchResult>>();
		if (CollectionUtils.isNotEmpty(matchResults)) {
			for (DlLeagueMatchResult dto : matchResults) {
				String playCode = dto.getPlayCode();
				List<DlLeagueMatchResult> list = resultMap.get(playCode);
				if (list == null) {
					list = new ArrayList<DlLeagueMatchResult>(5);
					resultMap.put(playCode, list);
				}
				list.add(dto);
			}
		}
		log.info("resultMap size=" + resultMap.size());
		List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>(orderDetails.size());
		for (String playCode : resultMap.keySet()) {
			List<DlLeagueMatchResult> resultDTOs = resultMap.get(playCode);
			List<OrderDetail> details = detailMap.get(playCode);
			for (OrderDetail orderDetail : details) {
				String ticketDataStr = orderDetail.getTicketData();
				String[] split = ticketDataStr.split(";");
				OrderDetail od = new OrderDetail();
				od.setOrderDetailId(orderDetail.getOrderDetailId());
				StringBuffer sbuf = new StringBuffer();
				int hasResultCount=0;
				for (String ticketData : split) {
					if (StringUtils.isBlank(ticketData) || !ticketData.contains("|")) {
						continue;
					}
					Integer playType = Integer.valueOf(ticketData.substring(0, ticketData.indexOf("|")));
					if (playType.equals(MatchPlayTypeEnum.PLAY_TYPE_TSO.getcode())) {
						String hhadRst = null;
						String hadRst = null;
						for (DlLeagueMatchResult dto : resultDTOs) {
							if (MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode() == dto.getPlayType().intValue()) {
								String cellCode = dto.getCellCode();
								if (cellCode.equals("3")) {
									hhadRst = "32";
								} else if (cellCode.equals("0")) {
									hhadRst = "33";
								} else if (cellCode.equals("0")) {
									hhadRst = "32,33";
								}
							} else if (MatchPlayTypeEnum.PLAY_TYPE_HAD.getcode() == dto.getPlayType().intValue()) {
								String cellCode = dto.getCellCode();
								if (cellCode.equals("3")) {
									hadRst = "31";
								} else if (cellCode.equals("0")) {
									hadRst = "30";
								}
							}
						}
						String cellCode = hhadRst;
						if (hadRst != null) {
							if (cellCode == null) {
								cellCode = hadRst;
							} else if (cellCode != null) {
								cellCode = cellCode + "," + hadRst;
							}
						}
						if (cellCode != null) {
							hasResultCount++;
							sbuf.append("07|").append(playCode).append("|").append(cellCode).append(";");
						}
					} else {
						for (DlLeagueMatchResult dto : resultDTOs) {
							if (playType.equals(dto.getPlayType())) {
								hasResultCount++;
								sbuf.append("0").append(dto.getPlayType()).append("|").append(playCode).append("|").append(dto.getCellCode()).append(";");
							}
						}
					}
				}
				if (sbuf.length() > 0&& hasResultCount==split.length) {
					od.setMatchResult(sbuf.substring(0, sbuf.length() - 1));
					orderDetailList.add(od);
				}
			}
		}
		log.info("updateOrderMatchResult 准备去执行数据库更新操作：size=" + orderDetailList.size());
		for(OrderDetail detail: orderDetailList) {
			orderDetailMapper.updateMatchResult(detail);
		}
		log.info("updateOrderMatchResult 准备去执行数据库更新取消赛事结果操作：size=" + cancelList.size());
		for(OrderDetail detail: cancelList) {
			orderDetailMapper.updateMatchResult(detail);
		}
	}


	/**
	 * 更新待开奖的订单状态及中奖金额
	 * @param issue
	 */
	public String updateOrderAfterOpenReward() {
		//查询订单状态是待开奖的，查询是否每笔订单锁包含的彩票都已经比对完成 
		List<String> orderSnList = orderMapper.queryOrderSnListUnOpenReward();
		
		log.info("待开奖数据： size="+orderSnList.size());
		if(CollectionUtils.isEmpty(orderSnList)) {
			return "待开奖数据： size="+orderSnList.size();
		}
		
		while(orderSnList.size() > 0) {
			int num = orderSnList.size()>20?20:orderSnList.size();
			List<String> subList = orderSnList.subList(0, num);
			List<DlPrintLottery> dlOrderDataDTOs = dlPrintLotteryMapper.getPrintLotteryListByGoOpenRewardOrderSns(subList);
			log.info("获取可开奖彩票信息："+dlOrderDataDTOs.size());
			if(CollectionUtils.isNotEmpty(dlOrderDataDTOs)) {
				Map<String, Double> map = new HashMap<String, Double>();
				Set<String> unOrderSns = new HashSet<String>();
				for(DlPrintLottery dto: dlOrderDataDTOs) {
					String orderSn = dto.getOrderSn();
					String compareStatus = dto.getCompareStatus();
					Integer thirdRewardStatus = dto.getThirdRewardStatus();
					Boolean caiXiaoMiIsNotRewardEnd = StringUtils.isBlank(compareStatus) || !"1".equals(compareStatus);
					String game = dto.getGame();
					if("T01".equals(game)){
						caiXiaoMiIsNotRewardEnd = caiXiaoMiIsNotRewardEnd||!Integer.valueOf(3).equals(thirdRewardStatus);
					}
					if(caiXiaoMiIsNotRewardEnd) {
						unOrderSns.add(orderSn);
					}
					if(unOrderSns.contains(orderSn)) {
						map.remove(orderSn);
						continue;
					}
					Double double1 = map.get(orderSn);
					BigDecimal realRewardMoney = dto.getRealRewardMoney();
					double realReward = 0;
					if(realRewardMoney!=null){
						realReward = realRewardMoney.doubleValue();
					}
					double1 = double1==null?realReward:(double1+realReward);
					map.put(orderSn, double1);
				}
				
				log.info("*********8可开奖订单及资金数："+map.size());
				List<OrderDataParam> dtos = new ArrayList<OrderDataParam>(map.size());
				for(String orderSn: map.keySet()) {
					OrderDataParam dlOrderDataDTO = new OrderDataParam();
					dlOrderDataDTO.setOrderSn(orderSn);
					BigDecimal realReward = BigDecimal.valueOf(map.get(orderSn));
					dlOrderDataDTO.setRealRewardMoney(realReward);
					
					if(realReward.compareTo(BigDecimal.ZERO) == 0) {//未中奖
						dlOrderDataDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT);
					}else if(realReward.compareTo(BigDecimal.ZERO) > 0) {//已中奖
						dlOrderDataDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_ALREADY);
					}
					
					if(realReward.compareTo(BigDecimal.ZERO) < 0) {//中奖金额为负数，过滤掉
						continue;
					}
					
					dtos.add(dlOrderDataDTO);
				}
				log.info("%%%%%%准备执行开奖订单数："+dtos.size());
				if(dtos.size() > 0) {
					int n = 0;
					for (OrderDataParam orderDataParam : dtos) {
						Order updateOrder = new Order();
						updateOrder.setOrderSn(orderDataParam.getOrderSn());
						updateOrder.setWinningMoney(orderDataParam.getRealRewardMoney());
						updateOrder.setOrderStatus(orderDataParam.getOrderStatus());
						updateOrder.setAwardTime(DateUtil.getCurrentTimeLong());
						n += orderMapper.updateWiningMoney(updateOrder);
					}
					log.info("更新订单中奖状态和中奖金额updateOrderInfoByExchangeReward param size="+ n);
				}
			}
			orderSnList.removeAll(subList);
		}
		return "success";
	}

    /**
     * 处理订单超时订单
     */
    public void dealBeyondTimeOrderOut() {
    	log.info("开始执行超时订单任务");
    	SysConfigDTO sysConfigDTO = sysConfigService.querySysConfig(45);
    	log.info("订单超时时间======{}",sysConfigDTO);
    	log.info("当前时间=========={}",DateUtil.getCurrentTimeLong());
    	Integer orderExpireTime = sysConfigDTO.getValue().intValue();
		List<Order> orderList = orderMapper.queryOrderListByOrder20minOut(DateUtil.getCurrentTimeLong(),orderExpireTime);
    	
		log.info("超时订单数："+orderList.size());
    	if(orderList.size() == 0) {
    		log.info("没有超时订单,定时任务结束");
    		return;
    	}
    	List<String> orderSnList = orderList.stream().map(s->s.getOrderSn()).collect(Collectors.toList());
    	orderMapper.batchUpdateOrderStatus0To8(orderSnList);
		log.info("结束执行超时订单任务");
    }

	/**
	 * 查询支付成功订单未进行出票数据
	 * @return
	 */
	public List<Order> getPaySuccessOrdersList() {
		return orderMapper.selectPaySuccessOrdersList();
	}

	public void doPaySuccessOrder(Order order) {
		String orderSn = order.getOrderSn();
		//进行预出票
		List<DlPrintLottery> dlPrints = dlPrintLotteryMapper.printLotterysByOrderSn(orderSn);
		if(CollectionUtils.isEmpty(dlPrints)){
			OrderInfoAndDetailDTO orderDetail = getOrderWithDetailByOrder(order);
			List< LotteryPrintDTO> lotteryPrints = dlPrintLotteryService.getPrintLotteryListByOrderInfo(orderDetail,orderSn);
			if(CollectionUtils.isNotEmpty(lotteryPrints)) {
				log.info("=============进行预出票和生成消息======================");
				dlPrintLotteryService.saveLotteryPrintInfo(lotteryPrints, order.getOrderSn(),order.getStoreId());
		        return;
			}
		}
	}


	/**
	 * 根据订单编号查询订单及订单详情
	 * 
	 * @param param
	 * @return
	 */
	public OrderInfoAndDetailDTO getOrderWithDetailByOrder(Order order) {
		List<OrderDetail> orderDetails = orderDetailMapper.queryListByOrderSn(order.getOrderSn());
		OrderInfoAndDetailDTO orderInfoAndDetailDTO = new OrderInfoAndDetailDTO();
		OrderInfoDTO orderInfoDTO = new OrderInfoDTO();
		orderInfoDTO.setCathectic(order.getCathectic());
		orderInfoDTO.setLotteryClassifyId(order.getLotteryClassifyId());
		orderInfoDTO.setLotteryPlayClassifyId(order.getLotteryPlayClassifyId());
		orderInfoDTO.setPassType(order.getPassType());
		orderInfoDTO.setPlayType(order.getPlayType());
		Date minMatchStartTime = null;
		orderInfoAndDetailDTO.setOrderInfoDTO(orderInfoDTO);
		List<OrderDetailDataDTO> orderDetailDataDTOs = new LinkedList<OrderDetailDataDTO>();
		if (CollectionUtils.isNotEmpty(orderDetails)) {
			for (OrderDetail orderDetail : orderDetails) {
				OrderDetailDataDTO orderDetailDataDTO = new OrderDetailDataDTO();
				orderDetailDataDTO.setChangci(orderDetail.getChangci());
				orderDetailDataDTO.setIsDan(orderDetail.getIsDan());
				orderDetailDataDTO.setLotteryClassifyId(orderDetail.getLotteryClassifyId());
				orderDetailDataDTO.setLotteryPlayClassifyId(orderDetail.getLotteryPlayClassifyId());
				orderDetailDataDTO.setMatchId(orderDetail.getMatchId());
				orderDetailDataDTO.setMatchTeam(orderDetail.getMatchTeam());
				orderDetailDataDTO.setMatchTime(orderDetail.getMatchTime());
				if(minMatchStartTime==null){
					minMatchStartTime = orderDetail.getMatchTime();
				}else{
					if(minMatchStartTime.after(orderDetail.getMatchTime())){
						minMatchStartTime = orderDetail.getMatchTime();
					}
				}
				orderDetailDataDTO.setTicketData(orderDetail.getTicketData());
				orderDetailDataDTO.setIssue(orderDetail.getIssue());
				orderDetailDataDTO.setBetType(orderDetail.getBetType());
				orderDetailDataDTOs.add(orderDetailDataDTO);
			}
		}
		orderInfoDTO.setMinMatchStartTime(minMatchStartTime);
		orderInfoAndDetailDTO.setOrderDetailDataDTOs(orderDetailDataDTOs);
		return orderInfoAndDetailDTO;
	}

	public void addRewardMoneyToUsers() {
		List<OrderWithUserDTO> orderWithUserDTOs = orderWithUserMapper.selectOpenedAllRewardOrderList();
		log.info("派奖已中奖的用户数据：code=" + orderWithUserDTOs.size());
		if (CollectionUtils.isNotEmpty(orderWithUserDTOs)) {
			log.info("需要派奖的数据:" + orderWithUserDTOs.size());
			List<UserIdAndRewardDTO> userIdAndRewardDTOs = new LinkedList<UserIdAndRewardDTO>();
			for (OrderWithUserDTO orderWithUserDTO : orderWithUserDTOs) {
				UserIdAndRewardDTO userIdAndRewardDTO = new UserIdAndRewardDTO();
				userIdAndRewardDTO.setUserId(orderWithUserDTO.getUserId());
				userIdAndRewardDTO.setOrderSn(orderWithUserDTO.getOrderSn());
				userIdAndRewardDTO.setReward(orderWithUserDTO.getRealRewardMoney());
				int betTime = orderWithUserDTO.getBetTime();
				userIdAndRewardDTO.setBetMoney(orderWithUserDTO.getBetMoney());
				userIdAndRewardDTO.setBetTime(DateUtil.getTimeString(betTime, DateUtil.datetimeFormat));
				userIdAndRewardDTO.setLotteryClassifyId(orderWithUserDTO.getLotteryClassifyId());
				userIdAndRewardDTOs.add(userIdAndRewardDTO);
			}
			userAccountService.batchUpdateUserAccount(userIdAndRewardDTOs,ProjectConstant.REWARD_AUTO);
		}
	}
 
	/**
     * 更新订单为已支付
     * @param orderSn
     * @return
     */
	@Transactional("transactionManager1")
    public boolean updatePayStatus(String orderSn,BigDecimal money) {
    	boolean succ = false;
    	Order order = new Order();
    	order.setOrderSn(orderSn);
    	order.setOrderStatus(3);
    	order.setPayStatus(1);
    	order.setPayCode("store");
    	order.setSurplus(money);
    	order.setPayTime(DateUtil.getCurrentTimeLong());
    	int cnt = orderMapper.updatePayStatusByOrderSn(order);
    	if(cnt > 0) {
    		succ = true;
    	}
    	return succ;
    }
	
	@Transactional("transactionManager1")
		public boolean updateOrderRollBack(String orderSn) {
				boolean succ = false;
				Order order = new Order();
		    	order.setOrderSn(orderSn);
		    	order.setOrderStatus(2);
		    	int cnt = orderMapper.updateOrderRollBack(order);
		    	if(cnt > 0) {
		    		succ = true;
		    	}
				return succ;
			}
	
	/**
	 * 修改派奖状态
	 * @param orderSn
	 * @return
	 */
	@Transactional("transactionManager1")
	public boolean updateAwardStatus(String orderSn) {
		boolean succ = false;
		Order order = new Order();
    	order.setOrderSn(orderSn);
    	order.setOrderStatus(9);
    	order.setAwardTime(DateUtil.getCurrentTimeLong());
    	int cnt = orderMapper.updateAwardStatusByOrderSn(order);
    	if(cnt > 0) {
    		succ = true;
    	}
		return succ;
	}
	
}
