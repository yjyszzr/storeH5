package com.dl.store.dao;

import com.dl.base.mapper.Mapper;
import com.dl.store.dto.OrderInfoListDTO;
import com.dl.store.dto.OrderWithUserDTO;
import com.dl.store.model.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface OrderMapper extends Mapper<Order> {

	public int setFirstPayTime(Map parameter);
 
	
	/**
	 * 保存订单数据
	 * 
	 * @param order
	 */
	public void insertOrder(Order order); 
 

	public int deleteOrderByOrderSn(@Param("orderSn") String orderSn);


	public List<OrderInfoListDTO> getOrderInfoList(@Param("statusList") List<Integer> statusList, @Param("userIdList")List<Integer> userIdList, @Param("storeIdList")List<Integer> storeIdList );


	public Order getOrderInfoByOrderSn(@Param("orderSn")String orderSn);

	public Order getOrderInfoByOrderId(@Param("orderId")String orderId);
	
	public int updateWiningMoney(Order updateOrder);


	public List<String> queryOrderSnListUnOpenReward();


	public List<Order> queryOrderListByOrder20minOut(@Param("nowTime") Integer nowTime,@Param("expireTime") Integer expireTime);


	public void batchUpdateOrderStatus0To8(@Param("orderSnList")List<String> orderSnList );


	public List<Order> selectPaySuccessOrdersList();
 

	/**
	 * 支付成功后更改订单状态
	 * @param order
	 * @return
	 */
	public int updatePayStatusByOrderSn(Order order);

	/***
	 * 更改订单派奖状态
	 * @param order
	 * @return
	 */
	public int updateAwardStatusByOrderSn(Order order);
	
	/**
	 * 更改订单回滚状态
	 * @param order
	 * @return
	 */
	public int updateOrderRollBack(Order order);
	
	public List<OrderWithUserDTO> selectOpenedAllRewardOrderList();

	public int updateOrderStatus6To5(@Param("orderSn")String orderSn);


	public int updateOrderStatus6To7(@Param("orderSn")String orderSn);
}