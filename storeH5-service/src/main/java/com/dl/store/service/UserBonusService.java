package com.dl.store.service;

import com.dl.base.exception.ServiceException;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SessionUtil;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.shop.payment.api.IpaymentService;
import com.dl.store.core.MemberConstant;
import com.dl.store.dao3.UserBonusMapper;
import com.dl.store.dao3.UserMapper;
import com.dl.store.dto.UserBonusDTO;
import com.dl.store.enums.MemberEnums;
import com.dl.store.model.UserBonus;
import com.dl.store.param.BonusLimitConditionParam;
import com.dl.store.param.OrderSnParam;
import com.dl.store.param.UserBonusParam;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example.Criteria;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional("transactionManager3")
@Slf4j
public class UserBonusService extends AbstractService<UserBonus> {
	@Resource
	private UserBonusMapper userBonusMapper;

	@Resource
	private UserBonusShowDescService userBonusShowDescService;

	@Resource
	private UserMapper userMapper;
	
	@Resource
	private IpaymentService payMentService;
	
	@Resource
	private IOrderService iOrderService;
	
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	

	/**
	 * 下单时的账户变动：目前仅红包置为已使用
	 * 
	 * @param userBonusParam
	 * @return
	 */
	public BaseResult<String> changeUserAccountByCreateOrder(UserBonusParam userBonusParam) {
		this.updateUserBonusStatusUsed(userBonusParam.getUserBonusId(), userBonusParam.getOrderSn());
		return ResultGenerator.genSuccessResult("更新红包状态成功");
	}


	/**
	 * 更新红包状态为已使用
	 *
	 * @param userBonusParam
	 * @return
	 */
	public BaseResult<String> updateUserBonusStatusUsed(Integer userBonusId, String orderSn) {
		if (userBonusId == null || StringUtils.isEmpty(orderSn)) {
		    return ResultGenerator.genResult(MemberEnums.PARAMS_NOT_NULL.getcode(), MemberEnums.PARAMS_NOT_NULL.getMsg());
		}

		UserBonus userBonus = this.findById(userBonusId);
		if (userBonus == null) {
            return ResultGenerator.genResult(MemberEnums.BONUS_UNEXITS.getcode(), "用户红包编号为" + userBonusId + MemberEnums.BONUS_UNEXITS.getMsg());
		}

		Condition cUsed = new Condition(UserBonus.class);
		Criteria criteria = cUsed.createCriteria();
		criteria.andCondition("user_bonus_id =", userBonusId);
		criteria.andCondition("bonus_status =", MemberConstant.BONUS_STATUS_USED);
		criteria.andCondition("is_delete =" + MemberConstant.NOT_DELETE);
		List<UserBonus> userBonusList = this.findByCondition(cUsed);
		if (userBonusList.size() > 0) {
			return ResultGenerator.genResult(MemberEnums.BONUS_USED.getcode(), "用户红包编号为" + userBonusId + MemberEnums.BONUS_USED.getMsg());
		}

		UserBonus usedUserBonus = new UserBonus();
        usedUserBonus.setUserBonusId(userBonus.getUserBonusId());
        usedUserBonus.setUsedTime(DateUtil.getCurrentTimeLong());
        usedUserBonus.setOrderSn(orderSn);
        usedUserBonus.setUserId(SessionUtil.getUserId());
        usedUserBonus.setBonusStatus(MemberConstant.BONUS_STATUS_USED);
        this.update(usedUserBonus);
		return ResultGenerator.genSuccessResult("success");
	}

    /**
     * 查询一个红包是否支付的时候可用
     */
    public BaseResult<UserBonus> queryOneValidBonus(Integer userBonusId, String orderSn){
        Integer userId = SessionUtil.getUserId();

        UserBonus userBonus = userBonusMapper.queryOneBonus(userBonusId,userId);
        if(userBonus == null){
            return ResultGenerator.genResult(MemberEnums.DBDATA_IS_NULL.getcode(),MemberEnums.DBDATA_IS_NULL.getMsg());
        }

        //根据订单号查询支付所用 余额
        com.dl.order.param.OrderSnParam snParam = new com.dl.order.param.OrderSnParam();
        snParam.setOrderSn(orderSn);
        BaseResult<OrderDTO> orderDTOBaseResult = iOrderService.getOrderInfoByOrderSn(snParam);
        if(!orderDTOBaseResult.isSuccess()){
            return ResultGenerator.genResult(MemberEnums.DBDATA_IS_NULL.getcode(),MemberEnums.DBDATA_IS_NULL.getMsg());
        }
        OrderDTO orderDTO = orderDTOBaseResult.getData();
        BigDecimal moneyPaid = orderDTO.getMoneyPaid();
        if(userBonus.getMinGoodsAmount().compareTo(moneyPaid) > 0){
            return ResultGenerator.genResult(MemberEnums.CANNOT_USE.getcode(),MemberEnums.CANNOT_USE.getMsg());
        }

        return ResultGenerator.genSuccessResult("success",userBonus);
    }


	/**
	 * 更新红包状态为未使用
	 *
	 * @param userBonusParam
	 * @return
	 */
	public void updateUserBonusStatusUnused(Integer userBonusId) {
		UserBonus userBonus = this.findById(userBonusId);
		if (userBonus == null) {
			throw new ServiceException(MemberEnums.BONUS_UNEXITS.getcode(), "用户红包编号为" + userBonusId + MemberEnums.BONUS_UNEXITS.getMsg());
		}
		Condition cUsed = new Condition(UserBonus.class);
		Criteria criteria = cUsed.createCriteria();
		criteria.andCondition("user_bonus_id =", userBonusId);
		criteria.andCondition("bonus_status =", MemberConstant.BONUS_STATUS_UNUSED);
		criteria.andCondition("is_delete =", MemberConstant.NOT_DELETE);
		List<UserBonus> userBonusList = this.findByCondition(cUsed);
		if (userBonusList.size() > 0) {
			throw new ServiceException(MemberEnums.BONUS_UNUSED.getcode(), "用户红包编号为" + userBonusId + MemberEnums.BONUS_UNUSED.getMsg());
		}

		UserBonus usedUserBonus = new UserBonus();
		usedUserBonus.setUserBonusId(userBonusId);
		usedUserBonus.setUsedTime(DateUtil.getCurrentTimeLong());
		usedUserBonus.setOrderSn("");
		usedUserBonus.setUserId(SessionUtil.getUserId());
		usedUserBonus.setBonusStatus(MemberConstant.BONUS_STATUS_UNUSED);
		this.update(usedUserBonus);
	}

	/**
	 * 给支付提供查询用户可用的红包列表
	 * 
	 * @return
	 */
	public List<UserBonusDTO> queryValidBonusListForPay(BonusLimitConditionParam bonusLimitConditionParam) {
		Integer userId = SessionUtil.getUserId();
		UserBonus userBonus = new UserBonus();
		userBonus.setUserId(userId);
		userBonus.setIsDelete(MemberConstant.NOT_DELETE);
		userBonus.setBonusStatus(MemberConstant.BONUS_STATUS_UNUSED);
		userBonus.setStartTime(DateUtil.getCurrentTimeLong());
		userBonus.setEndTime(DateUtil.getCurrentTimeLong());
		userBonus.setMinGoodsAmount(bonusLimitConditionParam.getOrderMoneyPaid());
		List<UserBonus> userBonusList = userBonusMapper.queryUserBonusForPay(userBonus);
		List<UserBonusDTO> userBonusDTOList = new ArrayList<UserBonusDTO>();
		if (CollectionUtils.isEmpty(userBonusList)) {
			return userBonusDTOList;
		}

		userBonusList.forEach(s -> {
			UserBonusDTO userBonusDTO = this.createReturnUserBonusDTO(s);
			userBonusDTOList.add(userBonusDTO);
		});
		return userBonusDTOList;

	}


	/**
	 * 给支付提供查询用户可用的红包列表
	 *
	 * @return
	 */
	public Integer queryValidBonusListSize(BonusLimitConditionParam bonusLimitConditionParam) {
		Integer userId = bonusLimitConditionParam.getUserId();
		UserBonus userBonus = new UserBonus();
		userBonus.setUserId(userId);
		userBonus.setIsDelete(MemberConstant.NOT_DELETE);
		userBonus.setBonusStatus(MemberConstant.BONUS_STATUS_UNUSED);
		userBonus.setStartTime(DateUtil.getCurrentTimeLong());
		userBonus.setEndTime(DateUtil.getCurrentTimeLong());
		userBonus.setMinGoodsAmount(bonusLimitConditionParam.getOrderMoneyPaid());
		List<UserBonus> userBonusList = userBonusMapper.queryUserBonusForPay(userBonus);
		return userBonusList.size();
	}


	/**
	 * 查询用户可用的红包数量
	 *
	 * @return
	 */
	public Integer validBonusSize(Integer userId,Integer storeId) {
		return userBonusMapper.queryUserBonusSize(userId,storeId);
	}



	/**
	 * 给支付提供查询用户可用的红包列表
	 *
	 * @return
	 */
	public List<UserBonusDTO> queryValidBonusListForPayV2(OrderSnParam orderSnParam,Integer userId) {
		List<UserBonusDTO> userBonusDTOList = new ArrayList<UserBonusDTO>();
		//根据订单号查询支付所用 余额
		com.dl.order.param.OrderSnParam snParam = new com.dl.order.param.OrderSnParam();
		snParam.setOrderSn(orderSnParam.getOrderSn());
		BaseResult<OrderDTO> orderDTOBaseResult = iOrderService.getOrderInfoByOrderSn(snParam);
		if(!orderDTOBaseResult.isSuccess()){
			return userBonusDTOList;
		}

		BigDecimal moneyPaid= orderDTOBaseResult.getData().getMoneyPaid();
		UserBonus userBonus = new UserBonus();
		userBonus.setUserId(userId);
		userBonus.setIsDelete(MemberConstant.NOT_DELETE);
		userBonus.setBonusStatus(Integer.valueOf(orderSnParam.getStatus()));
		userBonus.setStoreId(orderSnParam.getStoreId());
		List<UserBonus> userBonusList = new ArrayList<>();
		if("0".equals(orderSnParam.getStatus())){
			userBonus.setStartTime(DateUtil.getCurrentTimeLong());
			userBonus.setEndTime(DateUtil.getCurrentTimeLong());
			userBonusList = userBonusMapper.queryUserBonusForPay(userBonus);
		}else{
			userBonusList = userBonusMapper.queryUserBonusBySelective(userBonus);
		}

		if (CollectionUtils.isEmpty(userBonusList)) {
			return userBonusDTOList;
		}

		userBonusList.forEach(s -> {
			UserBonusDTO userBonusDTO = this.createReturnUserBonusDTO(s);
			if(s.getMinGoodsAmount().compareTo(moneyPaid) > 0){ userBonusDTO.setBonusStatus("3");}
			userBonusDTOList.add(userBonusDTO);
		});
		return userBonusDTOList;

	}


	/**
	 * 根据状态查询有效的红包集合 ""-全部 0-未使用 1-已使用 2-已过期
	 * 
	 * @param status
	 * @return
	 */
	public List<UserBonusDTO> queryBonusListByStatus(String status,Integer storeId) {
		Integer userId = SessionUtil.getUserId();
		UserBonus userBonus = new UserBonus();
		userBonus.setUserId(userId);
		userBonus.setStoreId(storeId);
		userBonus.setIsDelete(MemberConstant.NOT_DELETE);
		if (!StringUtils.isEmpty(status)) {
			userBonus.setBonusStatus(Integer.valueOf(status));
		}

		List<UserBonus> userBonusList = new ArrayList<>();
		if (!status.equals("1")) {
			userBonusList = userBonusMapper.queryUserBonusBySelective(userBonus);
		} else {
			userBonusList = userBonusMapper.queryUserBonusForUsed(userBonus);
		}

		PageInfo<UserBonus> pageInfo = new PageInfo<UserBonus>(userBonusList);
		List<UserBonusDTO> userBonusDTOList = new ArrayList<UserBonusDTO>();
		userBonusList.forEach(s -> {
			UserBonusDTO userBonusDTO = this.createReturnUserBonusDTO(s);
			userBonusDTOList.add(userBonusDTO);
		});

		return userBonusDTOList;
	}

	/**
	 * 统一构造返回前台红包列表的数据结构
	 * 
	 * @param shopBonusDTOList
	 * @param userBonusList
	 * @return
	 */
	public UserBonusDTO createReturnUserBonusDTO(UserBonus userBonus) {
		UserBonusDTO userBonusDTO = new UserBonusDTO();
		try {
			BeanUtils.copyProperties(userBonus, userBonusDTO);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

//		if (MemberConstant.BONUS_STATUS_UNUSED == userBonus.getBonusStatus()) {
//			Integer currentTime = DateUtil.getCurrentTimeLong();
//			userBonusDTO.setSoonExprireBz(this.createSoonExprireBz(currentTime, userBonus));
//			userBonusDTO.setLeaveTime(this.createLeaveTime(currentTime, userBonus));
//		} else {
//			userBonusDTO.setSoonExprireBz("");
//			userBonusDTO.setLeaveTime("");
//		}
		userBonusDTO.setBonusName(userBonus.getBonusPrice()+"元代金券");
		userBonusDTO.setShortDesc("去使用");
		userBonusDTO.setBonusPriceStr(userBonus.getBonusPrice()+"元");
		userBonusDTO.setUseRange(userBonusShowDescService.getUseRange(userBonus.getUseRange()));
		userBonusDTO.setBonusStatus(String.valueOf(userBonus.getBonusStatus()));
		userBonusDTO.setBonusPrice(userBonus.getBonusPrice());
		userBonusDTO.setLimitTime(userBonusShowDescService.getLimitTimeDesc(userBonus.getStartTime(), userBonus.getEndTime()));
		userBonusDTO.setBonusEndTime(DateUtil.getCurrentTimeString(Long.valueOf(userBonus.getEndTime()), DateUtil.date_sdf));
		userBonusDTO.setMinGoodsAmount(userBonusShowDescService.getLimitOrderAmountDesc(userBonus.getMinGoodsAmount(), userBonus.getBonusPrice()));
		return userBonusDTO;
	}

	/**
	 * 快过期和未生效标签
	 * 
	 * @param currentTime
	 * @param userBonus
	 * @return
	 */
	public static String createSoonExprireBz(Integer currentTime, UserBonus userBonus) {
		if (userBonus.getEndTime() - currentTime <= MemberConstant.OneDaySecond && userBonus.getEndTime() - currentTime > 0) {
			return MemberConstant.BONUS_SOONEXPIREBZ_NOTHIDE;
		}
		if (currentTime - userBonus.getStartTime() < 0) {// 未生效
			return MemberConstant.BONUS_NOWORK;
		}
		return "";
	}

	/**
	 * 未使用且已生效的才展示leaveTime
	 * 
	 * @param currentTime
	 * @param bonusEndTime
	 * @return
	 */
	public static String createLeaveTime(Integer currentTime, UserBonus userBonus) {
		if (currentTime > userBonus.getStartTime() && currentTime < userBonus.getEndTime()) {
			Integer leaveTime = userBonus.getEndTime() - currentTime;
			Integer leaveDays = leaveTime / MemberConstant.OneDaySecond;
			Integer yu = leaveTime % MemberConstant.OneDaySecond;
			if (leaveDays >= 1) {
				if (yu > 0) {
					leaveDays = leaveDays + 1;
				}
				return "剩余" + leaveDays + "天过期";
			} else {
				Integer leaveHours = leaveTime / 3600;
				return "剩余" + leaveHours + "小时过期";
			}
		}
		return "";
	}

	/**
	 * 查询单个红包的数据
	 *
	 * @param userBonusIds
	 * @return
	 */
	public UserBonusDTO queryUserBonus(Integer userBonusId) {
		Integer curTime = DateUtil.getCurrentTimeLong();
		Integer userId = SessionUtil.getUserId();
		Integer[] userBonusIdArr = new Integer[] { userBonusId };

		List<UserBonus> userBonusList = userBonusMapper.queryUserBonusList(userBonusIdArr, userId, curTime);
		if (userBonusList.size() == 0) {
			return null;
		}

		UserBonus userBonus = userBonusList.get(0);
		UserBonusDTO userBonusDTO = new UserBonusDTO();
		userBonusDTO.setUserBonusId(userBonus.getUserBonusId());
		userBonusDTO.setBonusPrice(userBonus.getBonusPrice());
		return userBonusDTO;
	}


	/**
     * 更新红包为已过期
     *
     */
    public void updateBonusExpire() {
        Integer now = DateUtil.getCurrentTimeLong();
        List<Integer> userBonusIdList = userBonusMapper.queryUserBonusIdsExpire(now);
        if (CollectionUtils.isEmpty(userBonusIdList)) {
            return;
        }

        int rst = userBonusMapper.updateBatchUserBonusExpire(userBonusIdList);
        log.info("本次更新过期的红包" + rst + "个");
    }



}
