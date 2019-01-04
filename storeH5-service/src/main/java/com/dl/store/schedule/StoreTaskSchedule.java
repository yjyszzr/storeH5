package com.dl.store.schedule;


import com.dl.store.service.UserBonusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

@Slf4j
@Configuration
@EnableScheduling
public class StoreTaskSchedule {

	@Resource
	private UserBonusService userBonusService;

	/************************** 用户的定时任务 *****************/
	/**
	 * 更新过期的红包
	 */
	@Scheduled(cron = "${store.schedule.bonus.expire}")
	public void updateBonusExpire() {
		log.info("更新过期的红包定时任务开始");
		userBonusService.updateBonusExpire();
		log.info("更新过期的红包的定时任务结束");
	}


//
//	@Resource
//	private OrderService orderService;

	
	
	
	/**
	 * 给中奖用户派奖
	 */
//	@Scheduled(cron = "${task.schedule.member.reward.money}")
//	public void addRewardMoneyToUsers() {
//		log.info("更新中奖用户的账户，派奖开始");
//		orderService.addRewardMoneyToUsers();
//		log.info("更新中奖用户的账户，派奖结束");
//	}
//	
	
//	
//	
//	
//	
//	/**
//	 * 第四步： 更新待开奖的订单状态及中奖金额
//	 * 
//	 */
//	@Scheduled(cron = "${task.schedule.order.open.reward}")
//	public void updateOrderAfterOpenReward() {
//		log.info("更新待开奖的订单开始");
//		orderService.updateOrderAfterOpenReward();
//		log.info("更新待开奖的订单结束");
//	}
//
//	/************************* 订单的定时任务 *****************/
//	/**
//	 * 订单详情赛果 （每5分钟执行一次）
//	 */
//	@Scheduled(cron = "${task.schedule.order.match.result}")
//	public void updateOrderMatchResult() {
//		log.info("开始执行更新订单详情赛果任务");
//		orderService.updateOrderMatchResult();
//		log.info("结束执行更新订单详情赛果任务");
//	}
// 
//	/**************** 超时订单处理 **************/
//	@Scheduled(cron = "${task.schedule.payment.time.out}")
//	public void dealBeyondTimeOrderOut() {
//		log.info("开始执行超时订单任务");
//		orderService.dealBeyondTimeOrderOut();
//		log.info("结束执行超时订单任务");
//	}
//
//	/**
//	 * 订单支付成功逻辑处理
//	 */
//	@Scheduled(cron = "${task.schedule.order.pay.success}")
//	public void orderPaySuccessScheduled() {
//		log.info("订单支付完成后的逻辑处理");
//		List<Order> orderList = orderService.getPaySuccessOrdersList();
//		for (Order order : orderList) {
//			log.info("订单信息：================{}",order);
//			try {
//				orderService.doPaySuccessOrder(order);
//			} catch (Exception e) {
//				log.error("处理订单支付成功order_sn={}", order.getOrderSn(), e);
//				log.error("处理订单支付成功",e);
//			}
//		}
//	}
//	
	
}
