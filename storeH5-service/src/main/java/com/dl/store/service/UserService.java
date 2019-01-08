package com.dl.store.service;

import com.alibaba.fastjson.JSON;
import com.dl.base.enums.RespStatusEnum;
import com.dl.base.exception.ServiceException;
import com.dl.base.model.UserDeviceInfo;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.RandomUtil;
import com.dl.base.util.RegexUtil;
import com.dl.base.util.SessionUtil;
import com.dl.member.api.IMessageService;
import com.dl.member.api.IUserService;
import com.dl.member.dto.UserNoticeDTO;
import com.dl.member.param.UserIdRealParam;
import com.dl.member.param.UserRealParam;
import com.dl.shop.auth.api.IAuthService;
import com.dl.shop.auth.dto.InvalidateTokenDTO;
import com.dl.store.core.MemberConstant;
import com.dl.store.dao3.UserMapper;
import com.dl.store.dto.UserDTO;
import com.dl.store.dto.UserLoginDTO;
import com.dl.store.dto.YesOrNoDTO;
import com.dl.store.enums.MemberEnums;
import com.dl.store.model.DlUserAuths;
import com.dl.store.model.User;
import com.dl.store.param.MobileAndPassParam;
import com.dl.store.param.SetLoginPassParam;
import com.dl.store.param.UserIdParam;
import com.dl.store.param.UserParam;
import com.dl.store.util.Encryption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Condition;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional("transactionManager3")
@Slf4j
public class UserService extends AbstractService<User> {

	@Resource
	private UserMapper userMapper;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private IUserService iUserService;

	@Resource
	private UserLoginService userLoginService;

	@Resource
	private DlUserAuthsService dlUserAuthsService;

	@Resource
	private IAuthService iAuthService;
	
	@Resource
	private IMessageService iMessageService;
	
	public Boolean queryStoreUserIsSuperWhite(Integer userId){
		User user = userMapper.queryUserByUserId(userId);
		if(user.getIsSuperWhite() != null && user.getIsSuperWhite() == 1){
			return true;
		}else{
			return false;
		}
	}

	public User queryUserByUserId(Integer userId){
		return  userMapper.queryUserByUserId(userId);
	}

	/**
	 * 保存用户
	 *
	 * @param uParams
	 */
	public Integer saveUser(UserParam userParam) {
		User user = new User();
		String userName = generateUserName(userParam.getMobile());// 账号
		String nickName = generateNickName(userParam.getMobile());// 昵称
		user.setMobile(userParam.getMobile());
		user.setUserName(userName);
		user.setNickname(nickName);
		user.setHeadImg(MemberConstant.USER_DEFAULT_HEADING_IMG);
		user.setRegTime(DateUtil.getCurrentTimeLong());
		user.setLastTime(DateUtil.getCurrentTimeLong());
		user.setRegIp(userParam.getRegIp());
		user.setRegFrom(userParam.getLoginSource());
		user.setUserStatus(0);// 用户状态：有效
		user.setUserType(false);// 用户类型：个人用户
		user.setMobileSupplier("");
		user.setMobileProvince("");
		user.setMobileCity("");
		user.setLastIp(userParam.getLastIp());
		user.setSex(false);
		user.setMobile(userParam.getMobile());
		String loginsalt = Encryption.salt();
		user.setSalt(loginsalt);
		String paysalt = Encryption.salt();
		user.setPayPwdSalt(paysalt);
		if (!StringUtils.isEmpty(userParam.getPassWord())) {
			user.setPassword(Encryption.encryption(userParam.getPassWord(), loginsalt));
		} else {
			user.setPassword("");
		}
		user.setRankPoint(0);
		user.setPassWrongCount(0);
		user.setIsReal(MemberConstant.USER_IS_NOT_REAL);
		user.setPushKey(userParam.getPushKey());
		UserDeviceInfo userDevice = SessionUtil.getUserDevice();
		if (userDevice != null) {
			String channel = userDevice.getChannel();
			user.setDeviceChannel(channel);
		}
		Integer insertRsult = userMapper.insertWithReturnId(user);
		if (1 != insertRsult) {
			log.error("注册用户失败");
			return null;
		}
		Integer userId = user.getUserId();

		return userId;
	}

	public BaseResult<UserDTO> queryUserByUserIdExceptPass() {
		Integer userId = SessionUtil.getUserId();
		if (null == userId) {
			return ResultGenerator.genNeedLoginResult("请登录");
		}

		User user = userMapper.queryUserByUserId(userId);
		if (user == null) {
			return ResultGenerator.genFailResult("用户不存在");
		}

		UserDTO userDTO = new UserDTO();
		try {
			BeanUtils.copyProperties(userDTO, user);
		} catch (Exception e1) {
			log.error("个人信息接口的DTO转换异常");
			return ResultGenerator.genFailResult("个人信息接口的DTO转换异常");
		}

		userDTO.setHasPass(StringUtils.isBlank(user.getPassword()) ? 0 : 1);
		BigDecimal userMoney = user.getUserMoney();
		String userMoneyStr = userMoney == null ? "0" : userMoney.toString();
		String mobile = user.getMobile();
		String strStar4 = RandomUtil.generateStarString(4);
		String mobileStr = mobile.replace(mobile.substring(3, 7), strStar4);
        userDTO.setHasThirdUserId(user.getHasThirdUserId());
        userDTO.setThirdMobile("");
        if(1 == user.getHasThirdUserId()){
            DlUserAuths userAuths = dlUserAuthsService.queryBindsUser(userId);
            if (null != userAuths) userDTO.setThirdMobile(userAuths.getThirdMobile());
        }
		userDTO.setMobile(mobileStr);
		userDTO.setIsSuperWhite(null == user.getIsSuperWhite()?"0":String.valueOf(user.getIsSuperWhite()));
		userDTO.setUserMoney(userMoneyStr);
		userDTO.setBalance(String.valueOf(userMoney.add(user.getUserMoneyLimit()).subtract(user.getFrozenMoney())));
		userDTO.setTotalMoney(String.valueOf(userMoney.add(user.getUserMoneyLimit()).subtract(user.getFrozenMoney())));
		return ResultGenerator.genSuccessResult("查询用户信息成功", userDTO);
	}


//	com.dl.member.param.TokenParam memToken = new com.dl.member.param.TokenParam();
//		memToken.setUserToken(tokenStr);
//	BaseResult<com.dl.member.dto.UserDTO> memRst =  iUserService.queryUserInfoByToken(memToken);
//		if(memRst.getCode() != 0){
//		log.error("iUserService.queryUserInfoByToken 接口异常");
//		return ResultGenerator.genFailResult("fail",userLoginDTO);
//	}



	public BaseResult<UserLoginDTO> bindsThirdAndReg(String tokenStr) {
		UserLoginDTO userLoginDTO = new UserLoginDTO();

		Integer userId = null;
		InvalidateTokenDTO invalidateTokenDTO = new InvalidateTokenDTO();
		invalidateTokenDTO.setToken(tokenStr);
		BaseResult<Integer> rst = iAuthService.getUserIdByToken(invalidateTokenDTO);
		if(rst.isSuccess()){
			userId = rst.getData();
		}else{
			return ResultGenerator.genResult(rst.getCode(),rst.getMsg());
		}
		log.info("userId+20180109:"+userId);

		com.dl.member.dto.UserDTO userDto = new com.dl.member.dto.UserDTO();
		com.dl.member.param.UserIdParam userIdParam = new com.dl.member.param.UserIdParam();
		userIdParam.setUserId(userId);
		BaseResult<com.dl.member.dto.UserDTO> userDTOBaseResult = iUserService.queryUserInfo(userIdParam);
		if(userDTOBaseResult.isSuccess()){
			userDto = userDTOBaseResult.getData();
		}else{
			return ResultGenerator.genResult(rst.getCode(),rst.getMsg());
		}
		log.info("userDto+20180109:"+userDto.getMobile()+","+userDto.getPassword()+","+userDto.getSalt());

		Integer newUserId = this.saveUser(userDto.getMobile(),userDto.getPassword(),userDto.getSalt(),userDto.getIsSuperWhite());
		if(newUserId != null){
			DlUserAuths dlUserAuths = new DlUserAuths();
			dlUserAuths.setThirdMobile(userDto.getMobile());
			dlUserAuths.setThirdPass(userDto.getPassword());
			dlUserAuths.setThirdSalt(userDto.getSalt());
			dlUserAuths.setThirdUserId(userDto.getUserId());
			dlUserAuths.setUserId(newUserId);
			Integer id = dlUserAuthsService.saveThirdUser(dlUserAuths);
			if(id != null){
				userMapper.updateUserHasThid(1,newUserId);
			}
			userLoginDTO = userLoginService.queryUserLoginDTOByMobile(userDto.getMobile(), "4");
		}
		return ResultGenerator.genSuccessResult("绑定新用户成功", userLoginDTO);
	}

	public BaseResult<String> bindsThird(MobileAndPassParam param) {
		Integer userId = SessionUtil.getUserId();
		if(userId == null){
			return ResultGenerator.genNeedLoginResult("请登录");
		}
		UserLoginDTO userLoginDTO = new UserLoginDTO();
		com.dl.member.param.MobileAndPassParam mobileAndPassParam = new com.dl.member.param.MobileAndPassParam();
		mobileAndPassParam.setMobile(param.getMobile());
		mobileAndPassParam.setPass(param.getPass());
		BaseResult<com.dl.member.dto.UserDTO> memRst = iUserService.queryUserByMobileAndPass(mobileAndPassParam);
		if(memRst.getCode() != 0){
			log.error("iUserService.queryUserByMobileAndPass 接口异常");
			return ResultGenerator.genResult(memRst.getCode(),memRst.getMsg());
		}

		com.dl.member.dto.UserDTO userDto = memRst.getData();

		//手机号得一致
		UserIdRealParam userRealParam = new UserIdRealParam();
		userRealParam.setUserId(userId);
		BaseResult<com.dl.member.dto.UserDTO> userDTOBaseResult = iUserService.queryUserInfoReal(userRealParam);
		if(!userDTOBaseResult.isSuccess()){
			log.error("iUserService.queryUserByMobileAndPass 接口异常");
			return ResultGenerator.genResult(memRst.getCode(),memRst.getMsg());
		}

		if(!userDTOBaseResult.getData().getMobile().equals(param.getMobile())){
			return ResultGenerator.genResult(MemberEnums.NOT_SAME_MOBILE.getcode(),MemberEnums.NOT_SAME_MOBILE.getMsg());
		}

		Boolean  bindRst = dlUserAuthsService.queryBindThird(userDto.getUserId());
		if(bindRst == true){
			return ResultGenerator.genResult(MemberEnums.DATA_ALREADY_EXIT_IN_DB.getcode(),"已经绑定过");
		}

		DlUserAuths dlUserAuths = new DlUserAuths();
		dlUserAuths.setThirdMobile(userDto.getMobile());
		dlUserAuths.setThirdPass(userDto.getPassword());
		dlUserAuths.setThirdSalt(userDto.getSalt());
		dlUserAuths.setThirdUserId(userDto.getUserId());
		dlUserAuths.setUserId(userId);
		Integer id = dlUserAuthsService.saveThirdUser(dlUserAuths);
		if(id != null){
			userMapper.updateUserHasThid(1,userId);
		}

		return ResultGenerator.genSuccessResult("绑定新用户成功");
	}

	public BaseResult<String> unBindsThird(MobileAndPassParam param) {
		Integer userId = SessionUtil.getUserId();
		if(userId ==  null){
			return ResultGenerator.genNeedLoginResult("请先登录");
		}

		UserLoginDTO userLoginDTO = new UserLoginDTO();
		com.dl.member.param.MobileAndPassParam mobileAndPassParam = new com.dl.member.param.MobileAndPassParam();
		mobileAndPassParam.setMobile(param.getMobile());
		mobileAndPassParam.setPass(param.getPass());
		BaseResult<com.dl.member.dto.UserDTO> memRst = iUserService.queryUserByMobileAndPass(mobileAndPassParam);
		if(memRst.getCode() != 0){
			log.error("iUserService.queryUserByMobileAndPass 接口异常");
			return ResultGenerator.genResult(memRst.getCode(),memRst.getMsg());
		}

		dlUserAuthsService.delThirdUser(userId);
		userMapper.updateUserHasThid(0,userId);

		return ResultGenerator.genSuccessResult("解除绑定用户成功");
	}

	/**
	 * 保存用户
	 *
	 * @param uParams
	 */
	public Integer saveUser(String mobile,String pass,String salt,String isSuperWhite) {
		User user = new User();
		String userName = generateUserName(mobile);// 账号
		String nickName = generateNickName(mobile);// 昵称
		user.setMobile(mobile);
		user.setUserName(userName);
		user.setNickname(nickName);
		user.setHeadImg("");
		user.setRegTime(DateUtil.getCurrentTimeLong());
		user.setLastTime(DateUtil.getCurrentTimeLong());
		user.setRegIp("");
		user.setRegFrom("h5");
		user.setUserStatus(0);
		user.setUserType(false);
		user.setMobileSupplier("");
		user.setMobileProvince("");
		user.setMobileCity("");
		user.setLastIp("");
		user.setSex(false);
		user.setSalt(salt);
		user.setPayPwdSalt("");
		user.setPassword(pass);
		user.setRankPoint(0);
		user.setPassWrongCount(0);
		user.setIsReal("0");
		user.setPushKey("");
		user.setDeviceChannel("");
		//查询彩小秘用户是否是超级用户
		log.info("[saveUser]" + " mobile:" + mobile + " isSuperWhite:" + isSuperWhite);
		if("1".equals(isSuperWhite)){
			user.setIsSuperWhite(1);
		}else{
			user.setIsSuperWhite(0);
		}
		Integer insertRsult = userMapper.insertWithReturnId(user);
		if (1 != insertRsult) {
			log.error("注册用户失败");
			return null;
		}
		Integer userId = user.getUserId();
		return userId;
	}

	/**
	 * 校验用户的手机号
	 *
	 * @param mobileNumberParam
	 * @return
	 */
	public BaseResult<String> validateUserMobile(String mobileNumber) {
		if (!RegexUtil.checkMobile(mobileNumber)) {
			return ResultGenerator.genResult(MemberEnums.MOBILE_VALID_ERROR.getcode(), MemberEnums.MOBILE_VALID_ERROR.getMsg());
		}

		User user = this.findBy("mobile", mobileNumber);
		if (null == user) {
			return ResultGenerator.genResult(MemberEnums.NO_REGISTER.getcode(), MemberEnums.NO_REGISTER.getMsg());
		}

		return ResultGenerator.genSuccessResult("用户手机号校验成功");
	}

	/**
	 * 更新用户登录密码
	 *
	 * @param mobileNumberParam
	 */
	public BaseResult<String> updateUserLoginPass(String userLoginPass, String mobileNumber, String smsCode) {
		if (!userLoginPass.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$")) {
			return ResultGenerator.genResult(MemberEnums.PASS_FORMAT_ERROR.getcode(), MemberEnums.PASS_FORMAT_ERROR.getMsg());
		}

		User user = this.findBy("mobile", mobileNumber);
		if (null == user) {
			return ResultGenerator.genResult(MemberEnums.NO_REGISTER.getcode(), MemberEnums.NO_REGISTER.getMsg());
		}

		String cacheSmsCode = stringRedisTemplate.opsForValue().get(MemberConstant.SMS_PREFIX + MemberConstant.SMS_TYPE_RESETPASS + "_" + mobileNumber);
		if (StringUtils.isEmpty(cacheSmsCode) || !cacheSmsCode.equals(smsCode)) {
			return ResultGenerator.genResult(MemberEnums.SMSCODE_WRONG.getcode(), MemberEnums.SMSCODE_WRONG.getMsg());
		}

		User updateUser = new User();
		updateUser.setUserId(user.getUserId());
		updateUser.setPassword(Encryption.encryption(userLoginPass, user.getSalt()));
		this.update(updateUser);

		stringRedisTemplate.opsForValue().set(MemberConstant.SMS_PREFIX + MemberConstant.SMS_TYPE_RESETPASS + "_" + mobileNumber, "");
		return ResultGenerator.genSuccessResult("更新用户登录密码成功");
	}

	/**
	 * 设置密码
	 *
	 * @param userLoginPass
	 * @param mobileNumber
	 * @param smsCode
	 * @return
	 */
	public BaseResult<String> setUserLoginPass(SetLoginPassParam param, Integer userId) {
		if (param.getType() == 1 && StringUtils.isBlank(param.getOldLoginPass())) {
			return ResultGenerator.genResult(MemberEnums.NO_OLD_LOGIN_PASS_ERROR.getcode(), MemberEnums.NO_OLD_LOGIN_PASS_ERROR.getMsg());
		}
		String userLoginPass = param.getUserLoginPass();
		if (StringUtils.isBlank(userLoginPass) || !userLoginPass.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$")) {
			return ResultGenerator.genResult(MemberEnums.PASS_FORMAT_ERROR.getcode(), MemberEnums.PASS_FORMAT_ERROR.getMsg());
		}
		User user = this.findById(userId);
		if (null == user) {
			return ResultGenerator.genResult(MemberEnums.USER_NOT_FOUND_ERROR.getcode(), MemberEnums.USER_NOT_FOUND_ERROR.getMsg());
		}
		if (param.getType() == 1) {
			String oldPass = Encryption.encryption(param.getOldLoginPass(), user.getSalt());
			if (!oldPass.equals(user.getPassword())) {
				return ResultGenerator.genResult(MemberEnums.ERR_OLD_LOGIN_PASS_ERROR.getcode(), MemberEnums.ERR_OLD_LOGIN_PASS_ERROR.getMsg());
			}
		}
		User updateUser = new User();
		updateUser.setUserId(user.getUserId());
		updateUser.setPassword(Encryption.encryption(userLoginPass, user.getSalt()));
		this.update(updateUser);

		return ResultGenerator.genSuccessResult("用户登录密码设置成功");
	}


	public UserDTO queryUserInfo(UserIdParam params) {
		Integer userId = params.getUserId();
		User user = this.findById(userId);
		UserDTO userDTO = new UserDTO();
		if(user != null) {
			try {
				BeanUtils.copyProperties(userDTO, user);
				String strRandom4 = RandomUtil.generateUpperString(4);
				String mobile = user.getMobile();
				mobile = mobile.replace(mobile.substring(3, 7), strRandom4);
				userDTO.setMobile(mobile);
				userDTO.setUserMoney(String.valueOf(user.getUserMoney()));
				userDTO.setUserMoneyLimit(String.valueOf(user.getUserMoneyLimit()));
				userDTO.setIsSuperWhite(null == user.getIsSuperWhite()?"0":String.valueOf(user.getIsSuperWhite()));
				BigDecimal totalMoney = user.getUserMoney().add(user.getUserMoneyLimit());
				userDTO.setTotalMoney(String.valueOf(totalMoney));
			} catch (Exception e) {
				throw new ServiceException(RespStatusEnum.SERVER_ERROR.getCode(), RespStatusEnum.SERVER_ERROR.getMsg());
			}
		}
		return userDTO;
	}

	/**
	 * 查询用户信息：所有字段
	 *
	 * @return
	 */
	public UserDTO queryUserInfo() {
		Integer userId = SessionUtil.getUserId();
		UserIdParam userIdParams = new UserIdParam();
		userIdParams.setUserId(userId);
		return queryUserInfo(userIdParams);
	}

	/**
	 * 生成昵称：
	 *
	 * @param mobile
	 * @return
	 */
	public String generateNickName(String mobile) {
		if (StringUtils.isEmpty(mobile)) {
			return "****彩主";
		}
		String userName = String.format("%s彩主", mobile.substring(mobile.length() - 4, mobile.length()));
		return userName.toString();
	}

	/**
	 * 生成账号： 1.随机生成4位字母 2.生成用户名 3.查询重复的用户名条数 4.如果有重复用户名，则重新生成
	 *
	 * @param mobile
	 * @return
	 */
	public String generateUserName(String mobile) {
		StringBuffer userName = new StringBuffer("dl");
		String strRandom4 = RandomUtil.generateUpperString(4);
		userName.append(mobile.replace(mobile.substring(3, 7), strRandom4));
		User user = this.findBy("userName", userName.toString());
		if (null != user) {
			generateUserName(mobile);
		}
		return userName.toString();
	}




	public List<String> getClientIds(List<Integer> userIds) {
		return userMapper.getClientIds(userIds);
	}

	public Integer updateUserInfo(User user) {
		return userMapper.updateUserInfo(user);
	}

	public User findByMobile(String mobile) {
		Condition condition = new Condition(User.class);
		condition.createCriteria().andCondition("mobile = ", mobile);
		List<User> userList = userMapper.selectByCondition(condition);
		if (userList.size() > 0) {
			return userList.get(0);
		} else {
			return null;
		}
	}

	public BaseResult<YesOrNoDTO> hasUnreadMes(){
        YesOrNoDTO yesOrNoDTO = new YesOrNoDTO();
        yesOrNoDTO.setYesOrNo("1");//默认都是已读
		Integer userId = SessionUtil.getUserId();
		if(userId == null){
            return ResultGenerator.genSuccessResult("success",yesOrNoDTO);
        }

		DlUserAuths userAuths = dlUserAuthsService.queryBindsUser(userId);
		if(userAuths == null){
            return ResultGenerator.genSuccessResult("success",yesOrNoDTO);
        }

        Integer thirdUserId = userAuths.getThirdUserId();
		com.dl.member.param.NoticeParam noticeParam = new com.dl.member.param.NoticeParam();
        noticeParam.setObjType("5");
        noticeParam.setUserId(thirdUserId);
        BaseResult<UserNoticeDTO> userNoticeDTOBaseResult = iMessageService.queryUserNotice(noticeParam);
        if(userNoticeDTOBaseResult.isSuccess()){
            UserNoticeDTO userNoticeDTO = userNoticeDTOBaseResult.getData();
            if(null != userNoticeDTO){
                Integer mesNum = userNoticeDTO.getMessageNotice();
                if(mesNum > 0){
                    yesOrNoDTO.setYesOrNo("0");
                }else{
					yesOrNoDTO.setYesOrNo("1");
				}
            }
        }

        return ResultGenerator.genSuccessResult("success",yesOrNoDTO);
	}

    public BaseResult<String> readMes(){
        Integer userId = SessionUtil.getUserId();
        if(userId == null){
            return ResultGenerator.genSuccessResult("success");
        }

        DlUserAuths userAuths = dlUserAuthsService.queryBindsUser(userId);
        if(userAuths == null){
            return ResultGenerator.genSuccessResult("success");
        }

        Integer thirdUserId = userAuths.getThirdUserId();
        com.dl.member.param.NoticeParam noticeParam = new com.dl.member.param.NoticeParam();
        noticeParam.setObjType("5");
        noticeParam.setUserId(thirdUserId);
        BaseResult<String> userNoticeDTOBaseResult = iMessageService.readUserNotice(noticeParam);

        return ResultGenerator.genSuccessResult("success");
    }


}
