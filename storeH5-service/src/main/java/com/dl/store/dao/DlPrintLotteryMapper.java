package com.dl.store.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.DlPrintLottery;

public interface DlPrintLotteryMapper extends Mapper<DlPrintLottery> {
 
	//通过订单获取对应的彩票 
	public List<DlPrintLottery> getPrintLotteryListByGoOpenRewardOrderSns(@Param("orderSns")List<String> orderSns);
	
	//获取订单下的所有标
	public List<DlPrintLottery> printLotterysByOrderSn(@Param("orderSn")String orderSn);
	
	public void batchInsertDlPrintLottery(@Param("dlPrintLotterys") List<DlPrintLottery> dlPrintLotterys);
}