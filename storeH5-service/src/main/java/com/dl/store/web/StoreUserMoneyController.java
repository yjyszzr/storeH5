package com.dl.store.web;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.order.param.AwardParam;
import com.dl.order.param.OrderRollBackParam;
import com.dl.store.model.Order;
import com.dl.store.service.OrderService;
import com.dl.store.service.UserAccountService;
import com.dl.store.service.UserStoreMoneyService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/pay")
@Slf4j
public class StoreUserMoneyController {
	@Resource
	private OrderService orderService;
	@Resource
	private UserStoreMoneyService userStoreMoneyService;
	@Resource
	private UserAccountService userAccountService;
	
	@ApiOperation(value = "", notes = "订单回滚")
	@PostMapping("/rollback")
	public BaseResult<?> orderRollBack(@RequestBody OrderRollBackParam param){
		String orderSn = param.getOrderSn();
		log.info("[rollback]" + " orderSn:" + orderSn);
		if(StringUtils.isEmpty(orderSn)) {
			return ResultGenerator.genFailResult("订单号为空");
		}
		Order order = orderService.queryOrderByOrderSn(orderSn);
		if(order == null) {
			return ResultGenerator.genFailResult("查询订单失败");
		}
		Integer userId = order.getUserId();
		Integer storeId = order.getStoreId();
		BigDecimal moneyPaid = order.getMoneyPaid();
		if(userId == null || userId <= 0) {
			return ResultGenerator.genFailResult("UserId为空");
		}
		if(storeId == null || storeId <= 0) {
			return ResultGenerator.genFailResult("StoreId为空");
		}
		if(moneyPaid == null || moneyPaid.doubleValue() <= 0) {
			return ResultGenerator.genFailResult("订单支付金额:" + moneyPaid);
		}
		//订单状态:0-待付款,1-待出票,2-出票失败3-待开奖4-未中将5-已中奖6-派奖中7-审核中8-支付失败9-已派奖10.已退款
		Integer orderStatus = order.getOrderStatus();
		if(orderStatus == 10) {
			return ResultGenerator.genFailResult("订单已回滚退款");
		}
		//更改订单退款状态
		boolean succ = orderService.updateOrderRollBack(orderSn);
		log.info("[orderRollBack]" + "更改订单回滚状态:" + succ);
		//账户余额回滚
		succ = userStoreMoneyService.orderRollBack(userId,storeId,moneyPaid);
		log.info("[orderRollBack]" + "账户回滚结果:" + succ);
		//增加流水记录
		int cnt = userAccountService.insertOrderPayInfo(userId, storeId, orderSn,moneyPaid,6);
		log.info("[orderRollBack]" + "增加账户流水记录:" + cnt);
		return ResultGenerator.genSuccessResult("账户回滚成功");
	}
	
	@ApiOperation(value = "派奖", notes = "派奖")
	@PostMapping("/award")
    public BaseResult<?> orderAward(@RequestBody AwardParam param){
		String orderSn = param.getOrderSn();
		log.info("[orderAward]" + " orderSn:" + orderSn);
		if(StringUtils.isEmpty(orderSn)) {
			return ResultGenerator.genFailResult("订单号为空");
		}
		Order order = orderService.queryOrderByOrderSn(orderSn);
		if(order == null) {
			return ResultGenerator.genFailResult("查询订单失败");
		}
		Integer userId = order.getUserId();
		Integer storeId = order.getStoreId();
		Integer orderStatus = order.getOrderStatus();
		BigDecimal awardMoney = order.getWinningMoney();
		if(userId == null || userId <= 0) {
			return ResultGenerator.genFailResult("UserId为空");
		}
		if(storeId == null || storeId <= 0) {
			return ResultGenerator.genFailResult("StoreId为空");
		}
		if(awardMoney == null || awardMoney.doubleValue() <= 0) {
			return ResultGenerator.genFailResult("awardMoney派奖金额为空");
		}
		if(orderStatus == 9) {	//已派奖
			return ResultGenerator.genFailResult("该订单已派奖");
		}
		if(orderStatus != 5) {	//已中奖才给派奖
			return ResultGenerator.genFailResult("该订单未中奖");
		}
		//操作类型:0-全部 1-奖金 2-充值 3-购彩 4-提现 5-红包 6-账户回滚, 7购券, 8退款，9充值过多（输入错误）
		//修改订单为已派奖
		boolean succ = orderService.updateAwardStatus(orderSn);
		log.info("[orderAward]" + "修改订单已派奖:" + succ);
		//增加钱包增加金额
		succ = userStoreMoneyService.awardMony(userId,storeId,awardMoney);
		log.info("[orderAward]" + "增加钱包金额:" + succ);
		//添加流水记录
		int cnt = userAccountService.insertOrderPayInfo(userId, storeId, orderSn,awardMoney,1);
		log.info("[orderAward]" + "增加账户流水成功:" + cnt);
		return ResultGenerator.genSuccessResult("派奖成功");
	}
}
