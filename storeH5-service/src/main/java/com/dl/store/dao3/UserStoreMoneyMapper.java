package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.UserStoreMoney;


public interface UserStoreMoneyMapper extends Mapper<UserStoreMoney>{
	
	public UserStoreMoney queryInfo(UserStoreMoney userMoneyMoney);
	
	public int orderPay(UserStoreMoney resultMoneyPay);
}
