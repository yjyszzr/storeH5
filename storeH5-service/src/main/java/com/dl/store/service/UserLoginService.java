package com.dl.store.service;

import com.alibaba.fastjson.JSON;
import com.dl.base.model.UserDeviceInfo;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.RegexUtil;
import com.dl.base.util.SessionUtil;
import com.dl.store.core.MemberConstant;
import com.dl.store.dao3.UserLoginLogMapper;
import com.dl.store.dao3.UserMapper;
import com.dl.store.dto.UserLoginDTO;
import com.dl.store.enums.MemberEnums;
import com.dl.store.model.User;
import com.dl.store.model.UserLoginLog;
import com.dl.store.param.UserLoginWithPassParam;
import com.dl.store.param.UserLoginWithSmsParam;
import com.dl.store.param.UserRegisterParam;
import com.dl.store.util.Encryption;
import com.dl.store.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 用户登录服务
 *
 * @author zhangzirong
 * @date 2018.03.09
 */
@Service
@Transactional("transactionManager3")
@Slf4j
public class UserLoginService extends AbstractService<User> {
	private final static Logger logger = LoggerFactory.getLogger(UserLoginService.class);
	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private UserLoginLogMapper userLoginMapper;

	@Resource
	private UserMapper userMapper;

	@Resource
	private UserService userService;

	@Resource
	private UserRegisterService userRegisterService;

	@Resource
	private DlUserAuthsService dlUserAuthsService;

	/**
	 * 密码登录
	 *
	 * @param userLoginMobileParam
	 * @return
	 */
	public BaseResult<UserLoginDTO> loginByPass(UserLoginWithPassParam userLoginMobileParam) {
		String mobile = userLoginMobileParam.getMobile();
		String password = userLoginMobileParam.getPassword();
		UserLoginDTO userLoginDTO = new UserLoginDTO();
		userLoginMobileParam.setPassword("******");
		String loginParams = JSONHelper.bean2json(userLoginMobileParam);
		UserDeviceInfo userDeviceInfo = SessionUtil.getUserDevice();
		String appCodeNameStr = org.apache.commons.lang.StringUtils.isEmpty(userDeviceInfo.getAppCodeName())?"10":userDeviceInfo.getAppCodeName();
		User user = userMapper.queryUserByMobile(userLoginMobileParam.getMobile()); //.queryUserByMobileAndAppCdde(userLoginMobileParam.getMobile(),appCodeNameStr);
		if (null == user) {
			//this.loginLog(-1, 0, 1, loginParams, MemberEnums.NO_REGISTER.getMsg());
			return ResultGenerator.genResult(MemberEnums.NO_REGISTER.getcode(), MemberEnums.NO_REGISTER.getMsg());
		}

		Integer userStatus = user.getUserStatus();
		logger.info("[loginByPass]" + " mobile:" + mobile + " userStatus:" + userStatus);
		if (userStatus.equals(MemberConstant.USER_STATUS_NOMAL)) {// 账号正常
			BaseResult<UserLoginDTO> userLoginRst = this.verifyUserPass(password, user, userLoginMobileParam);
			if (userLoginRst.getCode() != 0) {
				logger.info("[loginByPass]" + " mobile:" + mobile + " code:" + userLoginRst.getCode());
				//this.loginLog(user.getUserId(), 0, 1, loginParams, userLoginRst.getMsg());
				return ResultGenerator.genResult(userLoginRst.getCode(), userLoginRst.getMsg());
			}
			userLoginDTO = userLoginRst.getData();
			logger.info("[loginByPass]" + " mobile:" + mobile + " userLoginDTO:" + userLoginDTO);
			if (!userLoginMobileParam.getLoginSource().equals(MemberConstant.LOGIN_SOURCE_H5)) {
				if (null == userLoginMobileParam.getPushKey()) {
					this.updatePushKey("", user);
				} else {
					this.updatePushKey(userLoginMobileParam.getPushKey(), user);
				}
			}

			UserDeviceInfo userDevice = SessionUtil.getUserDevice();
			return ResultGenerator.genSuccessResult("登录成功", userLoginDTO);

		} else if (userStatus.equals(MemberConstant.USER_STATUS_LOCK)) {// 账号处于被锁状态
			Integer time = DateUtil.getCurrentTimeLong() - user.getLastTime();
			if (time > 60) {

				BaseResult<UserLoginDTO> userLoginRst = this.verifyUserPass(password, user, userLoginMobileParam);
				if (userLoginRst.getCode() != 0) {
					//this.loginLog(user.getUserId(), 0, 1, loginParams, userLoginRst.getMsg());
					return ResultGenerator.genResult(userLoginRst.getCode(), userLoginRst.getMsg());
				}
				userLoginDTO = userLoginRst.getData();
				logger.info("[loginByPass]" + " userLoginDTO:" + userLoginDTO);
				if (!userLoginMobileParam.getLoginSource().equals(MemberConstant.LOGIN_SOURCE_H5)) {
					if (null == userLoginMobileParam.getPushKey()) {
						this.updatePushKey("", user);
					} else {
						this.updatePushKey(userLoginMobileParam.getPushKey(), user);
					}
				}
				//this.loginLog(user.getUserId(), 0, 0, loginParams, JSONHelper.bean2json(userLoginDTO));
				return ResultGenerator.genSuccessResult("登录成功", userLoginDTO);
			} else {
				//this.loginLog(user.getUserId(), 0, 1, loginParams, MemberEnums.PASS_WRONG_BEYOND_5.getMsg());
				return ResultGenerator.genResult(MemberEnums.PASS_WRONG_BEYOND_5.getcode(), MemberEnums.PASS_WRONG_BEYOND_5.getMsg());
			}
		} else if (userStatus.equals(MemberConstant.USER_STATUS_FROZEN)) {// 账户被冻结
			//this.loginLog(user.getUserId(), 0, 1, loginParams, MemberEnums.USER_ACCOUNT_FROZEN.getMsg());
			return ResultGenerator.genResult(MemberEnums.USER_ACCOUNT_FROZEN.getcode(), MemberEnums.USER_ACCOUNT_FROZEN.getMsg());
		}

		return null;
	}

	/**
	 * 更新用户的推送token
	 * 
	 * @param pushKey
	 * @param user
	 * @return
	 */
	public void updatePushKey(String pushKey, User user) {
		String pushTokenDB = user.getPushKey();
		if (!pushKey.equals(pushTokenDB)) {
			User updateUser = new User();
			updateUser.setUserId(user.getUserId());
			updateUser.setPushKey(pushKey);
			userMapper.updateByPrimaryKeySelective(updateUser);
		}
	}


	/**
	 * 按照规则校验用户登录密码
	 * 
	 * @param password
	 * @param user
	 * @param userLoginMobileParam
	 * @return
	 */
	public BaseResult<UserLoginDTO> verifyUserPass(String password, User user, UserLoginWithPassParam userLoginMobileParam) {
		if (user.getPassword().equals(Encryption.encryption(password, user.getSalt()))) {// 密码正确
			if (user.getPassWrongCount() > 0) {
				User updatePassWrongCountUser = new User();
				updatePassWrongCountUser.setUserId(user.getUserId());
				updatePassWrongCountUser.setPassWrongCount(0);
				updatePassWrongCountUser.setUserStatus(0);
				userService.update(updatePassWrongCountUser);
			}
			UserLoginDTO userLoginDTO = queryUserLoginDTOByMobile(userLoginMobileParam.getMobile(), userLoginMobileParam.getLoginSource());
			return ResultGenerator.genSuccessResult("登录成功", userLoginDTO);

		} else {
			int nowWrongPassCount = user.getPassWrongCount();
			if (nowWrongPassCount < 4) {
				User updatePassWrongCountUser = new User();
				updatePassWrongCountUser.setUserId(user.getUserId());
				int passWrongCount = nowWrongPassCount + 1;
				updatePassWrongCountUser.setPassWrongCount(passWrongCount);
				userService.update(updatePassWrongCountUser);
				return ResultGenerator.genResult(MemberEnums.WRONG_IDENTITY.getcode(), "密码错误，您还有" + (nowWrongPassCount == 0 ? 4 : 4 - nowWrongPassCount) + "次机会");
			} else {// 输入错误密码超过5次，锁定用户
				User lockUser = new User();
				lockUser.setUserId(user.getUserId());
				lockUser.setLastTime(DateUtil.getCurrentTimeLong());
				lockUser.setUserStatus(1);
				userService.update(lockUser);
				return ResultGenerator.genResult(MemberEnums.PASS_WRONG_BEYOND_5.getcode(), MemberEnums.PASS_WRONG_BEYOND_5.getMsg());
			}
		}
	}


	/**
	 * 短信验证码登录
	 *
	 * @param userLoginMobileParam
	 * @return
	 */
	public BaseResult<UserLoginDTO> loginBySms(UserLoginWithSmsParam userLoginMobileParam, HttpServletRequest request) {
		String loginParams = JSONHelper.bean2json(userLoginMobileParam);
		if (!RegexUtil.checkMobile(userLoginMobileParam.getMobile())) {
			this.loginLog(-1, 0, 1, loginParams, com.dl.member.enums.MemberEnums.MOBILE_VALID_ERROR.getMsg());
			return ResultGenerator.genResult(com.dl.member.enums.MemberEnums.MOBILE_VALID_ERROR.getcode(), com.dl.member.enums.MemberEnums.MOBILE_VALID_ERROR.getMsg());
		}

		String mobile = userLoginMobileParam.getMobile();
		String cacheSmsCode = stringRedisTemplate.opsForValue().get(MemberConstant.SMS_PREFIX + MemberConstant.SMS_TYPE_LOGIN + "_" + userLoginMobileParam.getMobile());
		if (StringUtils.isEmpty(cacheSmsCode) || !cacheSmsCode.equals(userLoginMobileParam.getSmsCode())) {
			this.loginLog(-1, 0, 1, loginParams, com.dl.member.enums.MemberEnums.SMSCODE_WRONG.getMsg());
			return ResultGenerator.genResult(com.dl.member.enums.MemberEnums.SMSCODE_WRONG.getcode(), com.dl.member.enums.MemberEnums.SMSCODE_WRONG.getMsg());
		}

		UserDeviceInfo userDeviceInfo = SessionUtil.getUserDevice();
		String appCodeNameStr = org.apache.commons.lang.StringUtils.isEmpty(userDeviceInfo.getAppCodeName())?"10":userDeviceInfo.getAppCodeName();
		User user = userMapper.queryUserByMobile(userLoginMobileParam.getMobile()); //.queryUserByMobileAndAppCdde(userLoginMobileParam.getMobile(),appCodeNameStr);
		if (null == user) {// 新用户注册并登录

			UserRegisterParam userRegisterParam = new UserRegisterParam();
			userRegisterParam.setMobile(userLoginMobileParam.getMobile());
			userRegisterParam.setPassWord("");
			userRegisterParam.setLoginSource(userLoginMobileParam.getLoginSource());
			BaseResult<Integer> regRst = userRegisterService.registerUser(userRegisterParam,request);
			if (regRst.getCode() != 0) {
				this.loginLog(-1, 0, 1, loginParams, "注册失败");
				return ResultGenerator.genFailResult(regRst.getMsg());
			}

			UserLoginDTO userLoginDTO = queryUserLoginDTOByMobile(userLoginMobileParam.getMobile(), userLoginMobileParam.getLoginSource());

			stringRedisTemplate.delete(MemberConstant.SMS_PREFIX + MemberConstant.SMS_TYPE_LOGIN + "_" + userLoginMobileParam.getMobile());

			this.loginLog(regRst.getData(), 0, 0, loginParams, JSONHelper.bean2json(userLoginDTO));
			return ResultGenerator.genSuccessResult("登录成功", userLoginDTO);
		} else {
			Integer userStatus = user.getUserStatus();
			if (userStatus.equals(MemberConstant.USER_STATUS_NOMAL)) {// 账号正常
				UserLoginDTO userLoginDTO = queryUserLoginDTOByMobile(userLoginMobileParam.getMobile(), userLoginMobileParam.getLoginSource());
				stringRedisTemplate.delete(MemberConstant.SMS_PREFIX + MemberConstant.SMS_TYPE_LOGIN + "_" + userLoginMobileParam.getMobile());
				if (!userLoginMobileParam.getLoginSource().equals(MemberConstant.LOGIN_SOURCE_H5)) {
					if (null == userLoginMobileParam.getPushKey()) {
						this.updatePushKey("", user);
					} else {
						this.updatePushKey(userLoginMobileParam.getPushKey(), user);
					}
				}

				this.loginLog(user.getUserId(), 0, 0, loginParams, JSONHelper.bean2json(userLoginDTO));
				return ResultGenerator.genSuccessResult("登录成功", userLoginDTO);
			} else if (userStatus.equals(MemberConstant.USER_STATUS_LOCK)) {// 账号处于被锁状态
				boolean beyond1h = DateUtil.getCurrentTimeLong() - user.getLastTime() > 60 ? true : false;
				if (beyond1h) {
					UserLoginDTO userLoginDTO = queryUserLoginDTOByMobile(userLoginMobileParam.getMobile(), userLoginMobileParam.getLoginSource());
					stringRedisTemplate.delete(MemberConstant.SMS_PREFIX + MemberConstant.SMS_TYPE_LOGIN + "_" + userLoginMobileParam.getMobile());

					if (!userLoginMobileParam.getLoginSource().equals(MemberConstant.LOGIN_SOURCE_H5)) {
						if (null == userLoginMobileParam.getPushKey()) {
							this.updatePushKey("", user);
						} else {
							this.updatePushKey(userLoginMobileParam.getPushKey(), user);
						}
					}

					this.loginLog(user.getUserId(), 0, 0, loginParams, JSONHelper.bean2json(userLoginDTO));
					return ResultGenerator.genSuccessResult("登录成功", userLoginDTO);
				} else {
					this.loginLog(user.getUserId(), 0, 1, loginParams, com.dl.member.enums.MemberEnums.PASS_WRONG_BEYOND_5.getMsg());
					return ResultGenerator.genResult(com.dl.member.enums.MemberEnums.PASS_WRONG_BEYOND_5.getcode(), com.dl.member.enums.MemberEnums.PASS_WRONG_BEYOND_5.getMsg());
				}
			} else if (userStatus.equals(MemberConstant.USER_STATUS_FROZEN)) {// 账户被冻结
				this.loginLog(user.getUserId(), 0, 1, loginParams, com.dl.member.enums.MemberEnums.USER_ACCOUNT_FROZEN.getMsg());
				return ResultGenerator.genResult(com.dl.member.enums.MemberEnums.USER_ACCOUNT_FROZEN.getcode(), com.dl.member.enums.MemberEnums.USER_ACCOUNT_FROZEN.getMsg());
			}
		}

		return null;
	}

	/**
	 * 根据手机号查询用户登录信息
	 * 
	 * @return
	 */
	public UserLoginDTO queryUserLoginDTOByMobile(String mobile, String loginSource) {
		UserDeviceInfo userDeviceInfo = SessionUtil.getUserDevice();
		//String appCodeNameStr = org.apache.commons.lang.StringUtils.isEmpty(userDeviceInfo.getAppCodeName())?"10":userDeviceInfo.getAppCodeName();
		User userInfo = userMapper.queryUserByMobile(mobile);
		UserLoginDTO userLoginDTO = new UserLoginDTO();
		userLoginDTO.setMobile(userInfo.getMobile());
		userLoginDTO.setHeadImg(userInfo.getHeadImg());
		userLoginDTO.setNickName(userInfo.getNickname());
		userLoginDTO.setIsReal(userInfo.getIsReal().equals(MemberConstant.USER_IS_REAL) ? "1" : "0");
		userLoginDTO.setToken(TokenUtil.genToken(userInfo.getUserId(), Integer.valueOf(loginSource)));
		logger.info("queryUserLoginDTOByMobile方法,根据手机号查询用户登录信息======{}", userLoginDTO);
		return userLoginDTO;
	}


	public void loginLogOut() {
		Integer userId = SessionUtil.getUserId();
		if (userId != null) {
			UserLoginLog ull = userLoginMapper.getLastLog(userId);
			if (ull != null) {
				ull.setLogoutTime(DateUtil.getCurrentTimeLong());
				userLoginMapper.updateLogOutTime(ull);
			}
		}
	}


    /**
     * 登录日志添加
     *
     * @param userId
     *            用户ID
     * @param device
     *            登录设备类型
     * @param loginType
     *            登录类型
     */
    public void loginLog(Integer userId, Integer loginType, int loginSstatus, String loginParams, String loginResult) {
        UserDeviceInfo device = SessionUtil.getUserDevice();
        if (device == null) {
            device = new UserDeviceInfo();
        }
        // 登录日志添加
        UserLoginLog ull = new UserLoginLog();
        ull.setUserId(userId);
        ull.setLoginType(loginType);
        ull.setLoginStatus(loginSstatus);
        ull.setLoginIp(StringUtils.isBlank(device.getUserIp()) ? "" : device.getUserIp());
        int time = DateUtil.getCurrentTimeLong();
        ull.setLoginTime(time);
        ull.setPlat(StringUtils.isBlank(device.getPlat()) ? "" : device.getPlat());
        ull.setBrand(StringUtils.isBlank(device.getBrand()) ? "" : device.getBrand());
        ull.setMid(StringUtils.isBlank(device.getMid()) ? "" : device.getMid());
        ull.setOs(StringUtils.isBlank(device.getOs()) ? "" : device.getOs());
        ull.setW(StringUtils.isBlank(device.getW()) ? "" : device.getW());
        ull.setH(StringUtils.isBlank(device.getH()) ? "" : device.getH());
        ull.setImei("");
        ull.setLoginSource("");
        ull.setDeviceChannel(device.getChannel());
        ull.setLon(device.getLon() != null?device.getLon():0);
        ull.setLat(device.getLat() != null?device.getLat():0);
        log.info(JSON.toJSONString(device));
        if(!StringUtils.isEmpty(device.getCity())  && !StringUtils.isEmpty(device.getProvince())){
            try {
                log.info("city:"+URLDecoder.decode(device.getCity(),"UTF-8"));
                ull.setCity(URLDecoder.decode(device.getCity(),"UTF-8"));
                ull.setProvince(URLDecoder.decode(device.getProvince(),"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        ull.setLoginParams(loginParams);
        ull.setLoginResult(loginResult);

        int number = userLoginMapper.insertSelective(ull);
        if (number != 1) {
            log.error("登录日志添加失败:" + JSONHelper.bean2json(ull));
        }
    }
}
