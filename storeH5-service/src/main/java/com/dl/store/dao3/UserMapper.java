package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends Mapper<User> {

	User queryUserByMobile(@Param("mobile") String mobile);

	int insertWithReturnId(User user);

	User queryUserExceptPass(@Param("userId") Integer userId);

	/**
	 * 更新用户账户资金
	 * 
	 * @param user
	 * @return
	 */
	int updateUserMoneyAndUserMoneyLimit(User user);

	/**
	 * 更新绑定信息
	 * @param userIds
	 * @return
	 */
	int updateUserHasThid(@Param("hasThirdUserId") Integer hasThirdUserId,@Param("userId") Integer userId);


	/**
	 * 在数据库中更新用户账户资金
	 * 
	 * @param user
	 * @return
	 */
	int updateInDBUserMoneyAndUserMoneyLimit(User user);

	/**
	 * 提现，扣除用户可提现余额
	 * 
	 * @param user
	 * @return
	 */
	int reduceUserMoneyInDB(User user);

	/**
	 * 查询多个用户的当前余额
	 */
	List<User> queryUserByUserIds(@Param("userIds") List<Integer> userIds);

	User queryUserByUserId(@Param("userId") Integer userId);


	int updateUserMoneyForCashCoupon(User user);

	List<String> getClientIds(@Param("userIds") List<Integer> userIds);

	Integer updateUserInfo(User user);
	
	int updateIsReal0to1(@Param("userId") Integer userId);

	User selectUserFoUpdateByUserId(@Param("userId") Integer userId);

	String getMobileById(@Param("userId") Integer userId);

	//在数据库中更新用户可提现金额
    int updateInDBUserMoney(User user);

	User queryUserByMobileAndAppCdde(@Param("mobile") String mobile,@Param("appCodeName") String appCodeName);
	
}