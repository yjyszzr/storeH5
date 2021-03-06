package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.UserBonus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserBonusMapper extends Mapper<UserBonus> {
	
	 int insertBatchUserBonus(List<UserBonus> list);
	 
	 int insertBatchUserBonusForRecharge(List<UserBonus> list);
	
	 List<UserBonus> queryUserBonusList(@Param("userBonusIds") Integer[] userBonusIds, @Param("userId") Integer userId, @Param("curTime") Integer curTime);

	 List<UserBonus> queryUserBonusBySelective(UserBonus userBonus);

	 List<UserBonus> queryUserBonusForPay(UserBonus userBonus);

	 Integer queryUserBonusSize(@Param("userId") Integer userId,@Param("storeId") Integer storeId);

	 List<UserBonus> queryUserBonusForUsed(UserBonus userBonus);

	 List<Integer> queryUserBonusIdsExpire(@Param("now") Integer now);

//	 List<Integer> queryUserBonusIdsSoonExpire();

	 int updateBatchUserBonusExpire(@Param("list") List<Integer> userBonusIdList);

	int getUnReadBonusNum(@Param("userId") Integer userId);

	int updateUnReadBonus(@Param("userId") Integer userId);

	UserBonus queryOneBonus(@Param("userBonusId") Integer userBonusId,@Param("userId") Integer userId);

	List<UserBonus> queryPUshBonusList(@Param("start") Integer start, @Param("end") Integer end);
}