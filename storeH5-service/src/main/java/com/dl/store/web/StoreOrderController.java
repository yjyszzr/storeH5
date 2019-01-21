package com.dl.store.web;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.jsoup.helper.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtilNew;
import com.dl.base.util.SessionUtil;
import com.dl.store.dto.DlHallInfoDTO;
import com.dl.store.dto.UserBonusDTO;
import com.dl.store.enums.MemberEnums;
import com.dl.store.enums.OrderEnums;
import com.dl.store.model.Order;
import com.dl.store.model.User;
import com.dl.store.model.UserBonus;
import com.dl.store.model.UserStoreMoney;
import com.dl.store.param.OrderPayParam;
import com.dl.store.service.OrderService;
import com.dl.store.service.UserAccountService;
import com.dl.store.service.UserBonusService;
import com.dl.store.service.UserService;
import com.dl.store.service.UserStoreMoneyService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/order")
@Slf4j
public class StoreOrderController {
	@Resource
	private OrderService orderService;
	@Resource
	private UserStoreMoneyService userStoreMoneyService;
	@Resource
	private UserAccountService userAccountService;
	@Resource
	private UserBonusService userBonusService;
	@Resource
	private UserService userService; 

	@Resource
	private StringRedisTemplate stringRedisTemplate;
	
	@ApiOperation(value = "订单支付", notes = "订单支付")
	@PostMapping("/pay")
    public BaseResult<DlHallInfoDTO> orderPay(@RequestBody OrderPayParam param){
		String orderSn = param.getOrderSn();
		Integer storeId = param.getStoreId();
		Integer userId = SessionUtil.getUserId();
		Integer bonusId = param.getBonusId();
		log.info("[orderPay]" + " orderSn:" + orderSn + " storeId:" + storeId + " userId:" + userId + " bonusId:" + bonusId);

		String uniquePay =  "user_pay_unique_"+userId+"_"+param.getOrderSn();
		Boolean absent = stringRedisTemplate.opsForValue().setIfAbsent(uniquePay, "on");
		log.info("absent:"+absent);
		stringRedisTemplate.expire(uniquePay, 10, TimeUnit.SECONDS);
		if(!absent) {
			return ResultGenerator.genResult(MemberEnums.NOT_VALID_PAY.getcode(), MemberEnums.NOT_VALID_PAY.getMsg());
		}

		if(StringUtils.isEmpty(orderSn)) {
			return ResultGenerator.genResult(OrderEnums.ORDER_SN_EMPTY.getcode(),OrderEnums.ORDER_SN_EMPTY.getMsg());
		}
		if(storeId == null || storeId <= 0) {
			return ResultGenerator.genResult(OrderEnums.STORE_ID_EMPTY.getcode(),OrderEnums.STORE_ID_EMPTY.getMsg());
		}
		if(userId == null || userId <= 0) {
			return ResultGenerator.genResult(OrderEnums.USER_ID_EMPTY.getcode(),OrderEnums.USER_ID_EMPTY.getMsg());
		}
		Order order = orderService.queryOrderByOrderSn(orderSn);
		if(order == null) {
			return ResultGenerator.genResult(OrderEnums.ORDER_EMPTY.getcode(),OrderEnums.ORDER_EMPTY.getMsg());
		}
		Integer orderStatus = order.getOrderStatus();
		if (orderStatus != 0){
			return ResultGenerator.genResult(OrderEnums.ORDER_INVALID.getcode(),OrderEnums.ORDER_INVALID.getMsg());
		}
		BigDecimal ticketAmout = order.getTicketAmount();
		BigDecimal money = null;
		UserStoreMoney userStoreMoney = userStoreMoneyService.queryUserMoneyInfo(userId, storeId);
		if(userStoreMoney != null) {
			money = userStoreMoney.getMoney();
		}
		//该订单是否已支付
		log.info("[orderPay]" + " ticketAmout:" + ticketAmout + " money:" + money + " userId:" + userId + " storeId:" + storeId + " orderSn:" + orderSn);
		boolean isPaid = orderService.isOrderPayed(orderSn);
		if(isPaid) {
			return ResultGenerator.genResult(OrderEnums.ORDER_PAID.getcode(),OrderEnums.ORDER_PAID.getMsg());
		}
		UserBonus userBonuds = null;
		if(bonusId != null && bonusId > 0) {
			UserBonusDTO userBonusDTO = userBonusService.queryUserBonus(bonusId);
			if(userBonusDTO == null) {
				return ResultGenerator.genResult(OrderEnums.USERBONDS_NOT_EXIST.getcode(),OrderEnums.USERBONDS_NOT_EXIST.getMsg());
			}
			BaseResult<UserBonus> bR = userBonusService.queryOneValidBonus(bonusId,orderSn);
			if(!bR.isSuccess()) {
				log.info("[orderPay]" + " msg:" + bR.getMsg() + " code:" + bR.getCode());
				return ResultGenerator.genResult(OrderEnums.USERBONDS_NOTUSED.getcode(),OrderEnums.USERBONDS_NOTUSED.getMsg());
			}
			userBonuds = bR.getData();
		}
		BigDecimal amt = ticketAmout;
		BigDecimal bonudsPrice = null;
		Integer userBoundsId = null;
		//带有优惠券类型
		if(userBonuds != null){
			amt = ticketAmout.subtract(userBonuds.getBonusPrice());
			bonudsPrice = userBonuds.getBonusPrice();
			userBoundsId = userBonuds.getUserBonusId();
		}
		if(money.subtract(amt).doubleValue() < 0) {//余额不够
			return ResultGenerator.genResult(OrderEnums.USER_MONEY_NOTENOUGH.getcode(),OrderEnums.USER_MONEY_NOTENOUGH.getMsg());
		}
//		//优惠券流水记录
//		if(userBonuds != null) {
//			userAccountService.insertOrderPayInfo(userId, storeId, orderSn,userBoundsId,bonudsPrice,3);
//			log.info("[orderPay]" + " 记录优惠券流水");
//		}
		//Integer userId,Integer storeId,String orderSn,BigDecimal ticketAmt,Integer bonusId,BigDecimal bonusAmt,int processType
		//记录钱包流水 操作类型:0-全部 1-奖金 2-充值 3-购彩 4-提现 5-红包 6-账户回滚, 7购券, 8退款，9充值过多（输入错误）
		int cnt = userAccountService.insertOrderPayInfo(userId, storeId, orderSn,amt,userBoundsId,bonudsPrice,3);
		if(cnt > 0) {
			log.info("[orderPay]" + " UserAccountService insertOrderPayInfo succ cnt:" + cnt);
		}
		//扣除优惠券逻辑
		if(userBonuds != null) {
			BaseResult<String> bR = userBonusService.updateUserBonusStatusUsed(bonusId,orderSn);
			log.info("[orderPay]" + " 扣除优惠券为已使用bR.getMsg()" + bR.getMsg());
		}
		//扣除余额 Integer userId,Integer storeId,BigDecimal money
		boolean isSucc = userStoreMoneyService.orderPay(userId,storeId,amt,bonudsPrice);
		log.info("[orderPay]" + " succ:" + isSucc);
		if(!isSucc) {
			return ResultGenerator.genResult(OrderEnums.USER_MONEY_PAY_FAILE.getcode(),OrderEnums.USER_MONEY_PAY_FAILE.getMsg());
		}
		//更改订单状态为已支付
		boolean succ = orderService.updatePayStatus(orderSn,amt,bonudsPrice,userBoundsId);
		log.info("[orderPay]" + " succ:" + succ);
		
		try {
			log.info("[customer] start ================================= ");
			if (succ) { 
//				userId
				String mobile = "";
				String firstPayTime = "";
				
			 
				User user = this.userService.findById(userId);
				mobile = user.getMobile();
				if (mobile!= null) mobile = mobile.trim();
//				firstPayTime = order.getPayTime() + "";
				firstPayTime = order.getAddTime() + "";
				Order _order = orderService.queryOrderByOrderSn(orderSn);
				if(order != null) {
					firstPayTime = order.getPayTime() + "";
				}
				
				log.info("[customer] userId:" + userId); 
				log.info("[customer] mobile:" + mobile);
				log.info("[customer] firstPayTime:" + firstPayTime);
				
				if (null != userId
					&& !StringUtil.isBlank(mobile)
					&& !StringUtil.isBlank(firstPayTime)
				) {
					this.orderService.setFirstPayTime(userId + "", mobile, firstPayTime);
					log.info("[customer] to db");
				}
			
			} 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			log.info("[customer] end ================================= ");
		}
		
		return ResultGenerator.genSuccessResult("支付成功");
	}
	
}
