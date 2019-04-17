package com.dl.store.service;

import com.dl.base.model.UserDeviceInfo;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.IpUtil;
import com.dl.base.util.RegexUtil;
import com.dl.base.util.SessionUtil;
import com.dl.store.core.ProjectConstant;
import com.dl.store.dao3.UserMapper;
import com.dl.store.enums.MemberEnums;
import com.dl.store.model.User;
import com.dl.store.param.UserParam;
import com.dl.store.param.UserRegisterParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Service
@Transactional("transactionManager3")
@Slf4j
public class UserRegisterService extends AbstractService<User> {
    
    @Resource
    private UserService userService;

	@Resource
	private UserMapper userMapper;
    
	/**
	 * 注册并登录
	 * @param userRegisterParam
	 * @param request
	 * @return
	 */
	public BaseResult<Integer> registerUser(UserRegisterParam userRegisterParam, HttpServletRequest request) {
    	if(!RegexUtil.checkMobile(userRegisterParam.getMobile())) {
    		return ResultGenerator.genResult(MemberEnums.MOBILE_VALID_ERROR.getcode(), MemberEnums.MOBILE_VALID_ERROR.getMsg());
    	}

		UserDeviceInfo userDeviceInfo = SessionUtil.getUserDevice();
		String appCodeNameStr = org.apache.commons.lang.StringUtils.isEmpty(userDeviceInfo.getAppCodeName())?"10":userDeviceInfo.getAppCodeName();
		User user = userMapper.queryUserByMobileAndAppCdde(userRegisterParam.getMobile(),appCodeNameStr);
    	if(null != user) {
    		return ResultGenerator.genResult(MemberEnums.ALREADY_REGISTER.getcode(), MemberEnums.ALREADY_REGISTER.getMsg());
    	}
    	
    	UserParam userParam = new UserParam();
    	userParam.setMobile(userRegisterParam.getMobile());
    	userParam.setPassWord(userRegisterParam.getPassWord());
    	userParam.setRegIp(IpUtil.getIpAddr(request));
    	userParam.setLastIp(IpUtil.getIpAddr(request));
		if (userRegisterParam.getLoginSource().equals(ProjectConstant.LOGIN_SOURCE_ANDROID)) {
			userParam.setLoginSource(ProjectConstant.ANDROID);
		} else if (userRegisterParam.getLoginSource().equals(ProjectConstant.LOGIN_SOURCE_IOS)) {
			userParam.setLoginSource(ProjectConstant.IOS);
		} else if (userRegisterParam.getLoginSource().equals(ProjectConstant.LOGIN_SOURCE_PC)) {
			userParam.setLoginSource(ProjectConstant.PC);
		} else if (userRegisterParam.getLoginSource().equals(ProjectConstant.LOGIN_SOURCE_H5)) {
			userParam.setLoginSource(ProjectConstant.H5);
		} else {
			userParam.setLoginSource("unknown");
		}
		
		if(!userRegisterParam.getLoginSource().equals(ProjectConstant.LOGIN_SOURCE_H5)) {
			if(null == userRegisterParam.getPushKey()) {
				userParam.setPushKey("");
			}else {
				userParam.setPushKey(userRegisterParam.getPushKey());
			}
		}else {
			userParam.setPushKey("");
		}
		
    	Integer userId = userService.saveUser(userParam);
   	
    	return ResultGenerator.genSuccessResult("注册成功",userId);
    }
 
}
