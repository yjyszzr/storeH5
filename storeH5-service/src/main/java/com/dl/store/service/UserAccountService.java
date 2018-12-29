package com.dl.store.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.dl.base.constant.CommonConstants;
import com.dl.base.enums.AccountEnum;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SNGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.store.core.MemberConstant;
import com.dl.store.core.ProjectConstant;
import com.dl.store.dao3.OrderMapper;
import com.dl.store.dao3.UserAccountMapper;
import com.dl.store.dao3.UserMapper;
import com.dl.store.dao3.UserStoreMoneyMapper;
import com.dl.store.dto.SysConfigDTO;
import com.dl.store.dto.UserAccountDTO;
import com.dl.store.dto.UserIdAndRewardDTO;
import com.dl.store.model.User;
import com.dl.store.model.UserAccount;
import com.dl.store.model.UserStoreMoney;
import com.dl.store.param.AmountTypeParam;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserAccountService extends AbstractService<UserAccount> {
	@Resource
	private UserAccountMapper userAccountMapper;

	@Resource
	private UserMapper userMapper;

	@Resource
	private UserService userService;

	@Resource
	private UserStoreMoneyMapper userStoreMoneyMapper;
	
	@Resource
	private SysConfigService sysConfigService;

	@Resource
	private OrderMapper orderMapper;
	
	public int insertOrderPayInfo(Integer userId,Integer storeId,String orderSn,BigDecimal ticketAmt,int processType){
		int cnt = 0;
		UserStoreMoney userStoreMoney = new UserStoreMoney();
		userStoreMoney.setUserId(userId);
		userStoreMoney.setStoreId(storeId);
		userStoreMoney = userStoreMoneyMapper.queryInfo(userStoreMoney);
		UserAccount userAccount = new UserAccount();
		userAccount.setUserId(userId);
		String accountSn = SNGenerator.nextSN(SNBusinessCodeEnum.ACCOUNT_SN.getCode());
		userAccount.setAmount(BigDecimal.ZERO.subtract(ticketAmt));
		userAccount.setAccountSn(accountSn);
		userAccount.setProcessType(3);
		userAccount.setThirdPartName("店铺-" + storeId);
		userAccount.setThirdPartPaid(ticketAmt);
		userAccount.setPayId(storeId+"");
		userAccount.setAddTime(DateUtil.getCurrentTimeLong());
		userAccount.setLastTime(DateUtil.getCurrentTimeLong());
		userAccount.setCurBalance(userStoreMoney.getMoney().subtract(ticketAmt));
		userAccount.setStatus(1);
		userAccount.setStoreId(storeId);
		userAccount.setOrderSn(orderSn);
		String note = "";
		if(processType == 3) {
			note = "购彩" + String.format("%.2f",ticketAmt.doubleValue()) + "元";
		}else if(processType == 1) {
			note = "奖金" + String.format("%.2f",ticketAmt.doubleValue()) + "元";
		}else if(processType == 6) {
			note = "退款" + String.format("%.2f",ticketAmt.doubleValue()) + "元";
		}
		userAccount.setNote(note);
		cnt = userAccountMapper.insertUserAccountBySelective(userAccount);
		return cnt;
	}
	
	public PageInfo<UserAccountDTO> queryUserAccountList(AmountTypeParam amountTypeParam) {
		Integer startTime = 0;
		Integer endTime = 0;
		Integer userId = SessionUtil.getUserId();
		Integer processType = Integer.valueOf(amountTypeParam.getAmountType());
		String timeType = amountTypeParam.getTimeType();
		Integer pageNum = Integer.valueOf(amountTypeParam.getPageNum());
		Integer pageSize = Integer.valueOf(amountTypeParam.getPageSize());
		Date todayDate = new Date();
		List<UserAccount> userAccountList = null;
		List<UserAccountDTO> userAccountListDTO = new ArrayList<>();

		UserAccount userAccount = new UserAccount();
		userAccount.setUserId(userId);
		if (0 != processType) {
			userAccount.setProcessType(processType);
		} else {
			processType = null;
		}
		PageHelper.startPage(pageNum, pageSize);
		if (MemberConstant.ALL_TIME.equals(timeType)) {
			userAccountList = userAccountMapper.queryUserAccountBySelective(userAccount);
		} else if (MemberConstant.TODAY.equals(timeType)) {
			startTime = DateUtil.getTimeAfterDays(todayDate, 1, 0, 0, 0);
			endTime = DateUtil.getTimeAfterDays(todayDate, 1, 23, 59, 59);
			userAccountList = userAccountMapper.queryUserAccountByTime(userId, processType, startTime, endTime);
		} else if (MemberConstant.BEFORE_6_DAY.equals(timeType)) {
			startTime = DateUtil.getTimeAfterDays(todayDate, -5, 0, 0, 0);
			endTime = DateUtil.getTimeAfterDays(todayDate, 1, 23, 59, 59);
			userAccountList = userAccountMapper.queryUserAccountByTime(userId, processType, startTime, endTime);
		} else if (MemberConstant.BEFORE_29_DAY.equals(timeType)) {
			startTime = DateUtil.getTimeAfterDays(todayDate, -28, 0, 0, 0);
			endTime = DateUtil.getTimeAfterDays(todayDate, 1, 23, 59, 59);
			userAccountList = userAccountMapper.queryUserAccountByTime(userId, processType, startTime, endTime);
		} else if (MemberConstant.BEFORE_89_DAY.equals(timeType)) {
			startTime = DateUtil.getTimeAfterDays(todayDate, -88, 0, 0, 0);
			endTime = DateUtil.getTimeAfterDays(todayDate, 1, 23, 59, 59);
			userAccountList = userAccountMapper.queryUserAccountByTime(userId, processType, startTime, endTime);
		}

		PageInfo<UserAccount> pageInfo = new PageInfo<UserAccount>(userAccountList);
		for (UserAccount ua : userAccountList) {
			UserAccountDTO userAccountDTO = new UserAccountDTO();
			userAccountDTO.setId(ua.getId());
			userAccountDTO.setPayId(ua.getPayId());
			userAccountDTO.setAddTime(DateUtil.getCurrentTimeString(Long.valueOf(ua.getAddTime()), DateUtil.date_sdf));
			userAccountDTO.setAccountSn(ua.getAccountSn());
			userAccountDTO.setShotTime(DateUtil.getCurrentTimeString(Long.valueOf(ua.getAddTime()), DateUtil.short_time_sdf));
			userAccountDTO.setStatus("");
			userAccountDTO.setProcessType(String.valueOf(ua.getProcessType()));
			userAccountDTO.setProcessTypeChar(AccountEnum.getShortStr(ua.getProcessType()));
			userAccountDTO.setProcessTypeName(AccountEnum.getName(ua.getProcessType()));
			userAccountDTO.setNote("");
			String changeAmount = ua.getAmount().compareTo(BigDecimal.ZERO) == 1 ? "+" + ua.getAmount() + "元" : String.valueOf(ua.getAmount() + "元");
			userAccountDTO.setChangeAmount(changeAmount);
			userAccountListDTO.add(userAccountDTO);
		}

		PageInfo<UserAccountDTO> result = new PageInfo<UserAccountDTO>();
		try {
			BeanUtils.copyProperties(result, pageInfo);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		result.setList(userAccountListDTO);
		return result;
	}


	/**
	 * 中奖后批量更新用户账户的可提现余额,dealType = 1,自动；dealType = 2,手动
	 * 
	 * @param userIdAndRewardList
	 */
	public BaseResult<String> batchUpdateUserAccount(List<UserIdAndRewardDTO> dtos, Integer dealType) {
		List<UserIdAndRewardDTO> userIdAndRewardList = new ArrayList<UserIdAndRewardDTO>(dtos);
		BigDecimal limitValue = BigDecimal.ZERO;
		if (1 == dealType) {
			limitValue = this.queryBusinessLimit(CommonConstants.BUSINESS_ID_REWARD);
			if (limitValue.compareTo(BigDecimal.ZERO) <= 0) {
				limitValue = BigDecimal.ZERO;
			}

			Double limitValueDouble = limitValue.doubleValue();
			this.dealBeyondLimitOrder(userIdAndRewardList, limitValueDouble);
			userIdAndRewardList.removeIf(s -> s.getReward().doubleValue() >= limitValueDouble);
		}
		if (userIdAndRewardList.size() == 0) {
			log.info("没有要自动开奖的订单");
			return ResultGenerator.genSuccessResult("没有要自动开奖的订单");
		}

		log.info("=^_^= =^_^= =^_^= =^_^= 派奖开始,派奖数据包括:" + JSON.toJSONString(userIdAndRewardList));

		// 查询是否已经派发奖金,并过滤掉
		List<String> orderSnList = userIdAndRewardList.stream().map(s -> s.getOrderSn()).collect(Collectors.toList());
		List<String> rewardOrderSnList = userAccountMapper.queryUserAccountRewardByOrdersn(orderSnList);
		if (rewardOrderSnList.size() > 0) {
			log.error("含有已派发过奖金的订单号，已被过滤,订单号包括：" + Joiner.on(",").join(rewardOrderSnList));
			for(String s: rewardOrderSnList){
				orderMapper.updateOrderStatus6To5(s);
			}
			userIdAndRewardList.removeIf(s -> rewardOrderSnList.contains(s.getOrderSn()));
		}

		Integer accountTime = DateUtil.getCurrentTimeLong();
		for (UserIdAndRewardDTO uDTO : userIdAndRewardList) {
			User updateUserMoney = new User();
			updateUserMoney.setUserId(uDTO.getUserId());
			updateUserMoney.setUserMoney(uDTO.getReward());
			userMapper.updateInDBUserMoney(updateUserMoney);
			UserAccount userAccountParam = new UserAccount();
			String accountSn = SNGenerator.nextSN(SNBusinessCodeEnum.ACCOUNT_SN.getCode());
			userAccountParam.setAccountSn(accountSn);
			userAccountParam.setOrderSn(uDTO.getOrderSn());
			userAccountParam.setUserId(uDTO.getUserId());
			userAccountParam.setAmount(uDTO.getReward());
			userAccountParam.setProcessType(ProjectConstant.REWARD);
			userAccountParam.setLastTime(DateUtil.getCurrentTimeLong());
			userAccountParam.setAddTime(accountTime);
			userAccountParam.setStatus(Integer.valueOf(ProjectConstant.FINISH));
            User curUser = userMapper.queryUserByUserId(uDTO.getUserId());
            log.info("派奖 userId={},orderSn={},userMoney={},userMoneyLimit={}",uDTO.getUserId(),uDTO.getOrderSn(),curUser.getUserMoney(),curUser.getUserMoneyLimit());
            BigDecimal curBalance = curUser.getUserMoney().add(curUser.getUserMoneyLimit());
            userAccountParam.setCurBalance(curBalance);
			int insertRst = userAccountMapper.insertUserAccountBySelective(userAccountParam);
			if (1 != insertRst) {
				log.error("中奖订单号为" + uDTO.getOrderSn() + "生成中奖流水失败");
			} else {
                log.info("用户" + uDTO.getUserId() + "中奖订单号为" + uDTO.getOrderSn() + "奖金派发完成");
			}
		}
		log.info("更新用户中奖订单为已派奖开始");
		for(UserIdAndRewardDTO s: userIdAndRewardList){
			orderMapper.updateOrderStatus6To5(s.getOrderSn());
		}
		log.info("更新用户中奖订单为已派奖成功");
		//推送消息
//		saveRewardMessageAsync(userIdAndRewardList, accountTime);

		//记录中奖信息
//		this.updateLotteryWinning(oldUserIdAndRewardDtos);
		log.info("=^_^= =^_^= =^_^= =^_^= " + DateUtil.getCurrentDateTime() + "用户派发奖金完成" + "=^_^= =^_^= =^_^= =^_^= ");

		return ResultGenerator.genSuccessResult("用户派发奖金完成");
	}
	
	/**
	 * 查询业务值得限制：CommonConstants 中9-派奖限制 8-提现限制
	 * 
	 * @return
	 */
	public BigDecimal queryBusinessLimit(Integer businessId) {
		// 检查是否设置了派奖阈值
		SysConfigDTO sysDTO = sysConfigService.querySysConfig(businessId);
		if (sysDTO == null) {
			log.warn("派奖前，请前往后台管理设置派奖的奖金阈值");
			return BigDecimal.ZERO;
		}
		BigDecimal limitValue = sysDTO.getValue();
		return limitValue;
	}

	/**
	 * 更新超过阈值的订单状态为派奖审核中
	 * @param userIdAndRewardList
	 * @param limitValueDouble
	 */
	public void dealBeyondLimitOrder(List<UserIdAndRewardDTO> userIdAndRewardList,Double limitValueDouble) {
		List<UserIdAndRewardDTO> beyondLimitList = userIdAndRewardList.stream().filter(s -> s.getReward().doubleValue() >= limitValueDouble).collect(Collectors.toList());
		if(beyondLimitList.size() == 0) {
			return;
		}
		for(UserIdAndRewardDTO s: beyondLimitList){
			orderMapper.updateOrderStatus6To7(s.getOrderSn());
		}
	}
	
}
