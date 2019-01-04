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
import com.dl.base.util.SessionUtil;
import com.dl.store.dto.DlHallInfoDTO;
import com.dl.store.enums.OrderEnums;
import com.dl.store.model.Order;
import com.dl.store.model.UserStoreMoney;
import com.dl.store.param.OrderPayParam;
import com.dl.store.service.OrderService;
import com.dl.store.service.UserAccountService;
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
	
	@ApiOperation(value = "订单支付", notes = "订单支付")
	@PostMapping("/pay")
    public BaseResult<DlHallInfoDTO> orderPay(@RequestBody OrderPayParam param){
		String orderSn = param.getOrderSn();
		Integer storeId = param.getStoreId();
		Integer userId = SessionUtil.getUserId();
		Integer bonusId = param.getBonusId();
		log.info("[orderPay]" + " orderSn:" + orderSn + " storeId:" + storeId + " userId:" + userId + " bonusId:" + bonusId);
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
		if(money.subtract(ticketAmout).doubleValue() < 0) {//余额不够
			return ResultGenerator.genResult(OrderEnums.USER_MONEY_NOTENOUGH.getcode(),OrderEnums.USER_MONEY_NOTENOUGH.getMsg());
		}
		if(bonusId != null && bonusId > 0) {
			boolean isBondsAvailable = false;
		}
		//记录流水 操作类型:0-全部 1-奖金 2-充值 3-购彩 4-提现 5-红包 6-账户回滚, 7购券, 8退款，9充值过多（输入错误）
		int cnt = userAccountService.insertOrderPayInfo(userId, storeId, orderSn,ticketAmout,3);
		if(cnt > 0) {
			log.info("[orderPay]" + " UserAccountService insertOrderPayInfo succ cnt:" + cnt);
		}
		//扣除余额 Integer userId,Integer storeId,BigDecimal money
		boolean isSucc = userStoreMoneyService.orderPay(userId,storeId,ticketAmout);
		log.info("[orderPay]" + " succ:" + isSucc);
		if(!isSucc) {
			return ResultGenerator.genResult(OrderEnums.USER_MONEY_PAY_FAILE.getcode(),OrderEnums.USER_MONEY_PAY_FAILE.getMsg());
		}
		//更改订单状态为已支付
		boolean succ = orderService.updatePayStatus(orderSn,ticketAmout);
		log.info("[orderPay]" + " succ:" + succ);
		return ResultGenerator.genSuccessResult("支付成功");
	}
}
