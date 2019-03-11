package com.dl.store.web;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.store.dto.DlHallInfoDTO;
import com.dl.store.dto.DlPlayClassifyDetailDTO;
import com.dl.store.dto.UserDTO;
import com.dl.store.model.UserStoreMoney;
import com.dl.store.param.HallParam;
import com.dl.store.param.UserIdParam;
import com.dl.store.service.LotteryHallService;
import com.dl.store.service.UserBonusService;
import com.dl.store.service.UserService;
import com.dl.store.service.UserStoreMoneyService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/hall")
@Slf4j
public class LotteryHallController {
	@Resource
	private LotteryHallService lotteryHallService;
	@Resource
	private UserStoreMoneyService userStoreMoneyService;
	@Resource 
	private UserService userService;

	@Resource
	private UserBonusService userBonusService;
	
	
	@ApiOperation(value = "首页大厅", notes = "首页大厅")	
	@PostMapping("/info")
    public BaseResult<DlHallInfoDTO> hallInfo(@RequestBody HallParam param){
		log.info("[hallInfo]" + "...");
		List<DlPlayClassifyDetailDTO> rList = lotteryHallService.listAllClassify();
		DlHallInfoDTO hallInfo = new DlHallInfoDTO();
		hallInfo.setList(rList);
		BigDecimal money = null;
		BigDecimal moneyLimit = null;
		Integer userId = SessionUtil.getUserId();
		Integer storeId = param.getStoreId();
		log.info("[hallInfo]" + " userId:" + userId + " storeId:" + storeId);
		if(userId != null && storeId != null) {
			UserStoreMoney userStoreMoney = userStoreMoneyService.queryUserMoneyInfo(userId,param.getStoreId());
			if(userStoreMoney != null) {
				money = userStoreMoney.getMoney();
				moneyLimit = userStoreMoney.getMoneyLimit();
			}
		}
		String isSuperWhite = null;
		if(userId != null) {
			UserIdParam userIdParams = new UserIdParam();
			userIdParams.setUserId(userId);
			UserDTO userDTO = userService.queryUserInfo(userIdParams);
			if(money != null) {
				hallInfo.setMoney(money.add(moneyLimit)+"");
			}else {
				hallInfo.setMoney(BigDecimal.ZERO.toString());
			}
			if(moneyLimit != null) {
				hallInfo.setMoneyLimit(moneyLimit+"");
			}else {
				hallInfo.setMoneyLimit(BigDecimal.ZERO.toString());
			}
			if(userDTO != null) {
				isSuperWhite = userDTO.getIsSuperWhite();
			}

			Integer bonusSize = userBonusService.validBonusSize(userId,param.getStoreId());
			hallInfo.setMyBonusNum(String.valueOf(bonusSize));

			Boolean rst = userService.queryStoreUserIsSuperWhite(userId);
			if(rst){
				hallInfo.setIsShowAccount("1");
				hallInfo.setIsShowBonus("1");
			}
		}

		hallInfo.setIsSuperWhite(isSuperWhite);
		log.info("[hallInfo]" + " money:" + money + " isSuperWhite:" + isSuperWhite);
		return ResultGenerator.genSuccessResult("succ",hallInfo);
	}
	
}

