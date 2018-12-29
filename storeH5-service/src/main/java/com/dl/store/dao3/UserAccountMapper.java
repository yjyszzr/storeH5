package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.UserAccount;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

public interface UserAccountMapper extends Mapper<UserAccount> {

	List<UserAccount> queryUserAccountBySelective(UserAccount userAccount);

	int insertUserAccount(UserAccount userAccount);

	int updateUserAccountBySelective(UserAccount userAccount);
	
	int countValidUserAccountByUserId(@Param("userId") Integer userId);

	List<UserAccount> queryUserAccountCurMonth(@Param("userId") Integer userId);

	List<String> queryUserAccountRewardByOrdersn(@Param("list") List<String> list);

	int insertUserAccountBySelective(UserAccount userAccount);

	BigDecimal countBackMoneyByProcessTyepByOrderSns(@Param("list") List<String> list, @Param("userId") Integer userId);

	List<UserAccount> findByUserIdsAndType(@Param("list") List<String> userIds, @Param("data") String data, @Param("type") int type);

	List<UserAccount> queryUserAccountByTime(@Param("userId") Integer userId, @Param("processType") Integer processType, @Param("startTime") Integer startTime, @Param("endTime") Integer endTime);

	List<UserAccount> countUserAccountByTime(@Param("startTime") Integer startTime, @Param("endTime") Integer endTime);
}