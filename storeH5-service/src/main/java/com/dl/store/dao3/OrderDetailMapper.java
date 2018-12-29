package com.dl.store.dao3;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.LotteryClassifyTemp;
import com.dl.store.model.LotteryPlayClassifyTemp;
import com.dl.store.model.OrderDetail;
import com.dl.store.model.PlayTypeName;

public interface OrderDetailMapper extends Mapper<OrderDetail> {

	LotteryClassifyTemp lotteryClassify( @Param("classifyId")Integer lotteryClassifyId);

	List<PlayTypeName> getPlayTypes(@Param("lotteryClassifyId")Integer lotteryClassifyId);

	LotteryPlayClassifyTemp lotteryPlayClassifyStatusAndUrl(@Param("classifyId") int classifyId, @Param("playClassifyId") int playClassifyId);

	List<OrderDetail> selectByOrderId(@Param("orderId")Integer orderId, @Param("userId")Integer userId);

	public List<OrderDetail> queryListByOrderSn(@Param("orderSn")String orderSn);

	List<OrderDetail> unMatchResultOrderDetails();

	void updateMatchResult(OrderDetail detail);
	
}