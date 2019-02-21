package com.dl.store.service;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.util.DateUtil;
import com.dl.store.dao3.UserStoreMoneyMapper;
import com.dl.store.model.UserStoreMoney;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional("transactionManager3")
@Slf4j
public class UserStoreMoneyService {

	@Resource
	private UserStoreMoneyMapper userStoreMoneyMapper;
	
	/***
	 * 订单支付，店铺余额减
	 * @param userId
	 * @param storeId
	 * @param money
	 * @return
	 */
	public synchronized boolean orderPay(Integer userId,Integer storeId,BigDecimal amt,BigDecimal userBonudsPrice){
		boolean succ = false;
		if(amt.doubleValue() > 0) {
			UserStoreMoney params = new UserStoreMoney();
			params.setUserId(userId);
			params.setStoreId(storeId);
			UserStoreMoney userStoreMoney = userStoreMoneyMapper.queryInfo(params);
			log.info("[orderPay]" + " query:" + userStoreMoney.getUserId() + " storeId:" + userStoreMoney.getStoreId() + " money:" + userStoreMoney.getMoney() + " limitMoney:" + userStoreMoney.getMoneyLimit());
			BigDecimal userMoneyLimit = userStoreMoney.getMoney();
			BigDecimal moneyResult = userMoneyLimit.subtract(amt);
			if(moneyResult.floatValue() >= 0) {
				userStoreMoney.setMoneyLimit(moneyResult);
			}else {
				//不可提现余额钱包清空
				userStoreMoney.setMoneyLimit(BigDecimal.ZERO);
				//减少可提现余额钱包
				userStoreMoney.setMoney(userStoreMoney.getMoney().add(moneyResult));
			}
			int cnt = userStoreMoneyMapper.orderPay(userStoreMoney);
			log.info("[orderPay]" + " cnt:" + cnt + " result:" + moneyResult + " orderMoney:" + amt + " userMoney:" + userStoreMoney.getMoney());
			succ = true;
		}
		return succ;
	}
	

	/***
	 * 查询用户该店铺下余额信息
	 * @param userStoreMoney
	 * @return
	 */
	public UserStoreMoney queryUserMoneyInfo(Integer userId,Integer storeId) {
		UserStoreMoney userStoreMoney = new UserStoreMoney();
		userStoreMoney.setStoreId(storeId);
		userStoreMoney.setUserId(userId);
		return userStoreMoneyMapper.queryInfo(userStoreMoney);
	}
	
	/**
	 * 派奖金额
	 * @param userId
	 * @param storeId
	 * @param money
	 * @return
	 */
	public boolean awardMony(Integer userId,Integer storeId,BigDecimal money) {
		boolean succ = false;
		UserStoreMoney userStoreMoney = new UserStoreMoney();
		userStoreMoney.setStoreId(storeId);
		userStoreMoney.setUserId(userId);
		UserStoreMoney userMoneyResult = userStoreMoneyMapper.queryInfo(userStoreMoney);
		if(userMoneyResult == null || userMoneyResult.getId() == null || userMoneyResult.getId() <= 0) {
			userStoreMoney.setMoney(money);
			userStoreMoney.setLastTime(DateUtil.getCurrentTimeLong());
			int cnt = userStoreMoneyMapper.insert(userStoreMoney);
			if(cnt > 0) {
				succ = true;
			}
		}else {
			BigDecimal userMoney = userMoneyResult.getMoney();
			BigDecimal moneyResult = userMoney.add(money);
			userStoreMoney.setMoney(moneyResult);
			int cnt = userStoreMoneyMapper.orderPay(userStoreMoney);
			if(cnt > 0) {
				succ = true;
			}
		}
		return succ;
	}
	
	public boolean orderRollBack(Integer userId,Integer storeId,BigDecimal money) {
		boolean succ = false;
		UserStoreMoney userStoreMoney = new UserStoreMoney();
		userStoreMoney.setStoreId(storeId);
		userStoreMoney.setUserId(userId);
		UserStoreMoney userMoneyResult = userStoreMoneyMapper.queryInfo(userStoreMoney);
		BigDecimal userMoney = userMoneyResult.getMoney();
		BigDecimal moneyResult = userMoney.add(money);
		userStoreMoney.setMoney(moneyResult);
		int cnt = userStoreMoneyMapper.orderPay(userStoreMoney);
		if(cnt > 0) {
			succ = true;
		}
		return succ;
	}
}
