package com.dl.store.web;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.IUserLoginService;
import com.dl.member.api.IUserService;
import com.dl.member.dto.UserDTO;
import com.dl.member.param.MobileAndPassParam;
import com.dl.store.enums.MemberEnums;
import com.dl.store.model.DlUserAuths;
import com.dl.store.param.StrParam;
import com.dl.store.param.UserLoginWithPassParam;
import com.dl.store.param.UserLoginWithSmsParam;
import com.dl.store.service.DlUserAuthsService;
import com.dl.store.service.UserLoginService;
import com.dl.store.service.UserService;
import com.dl.store.util.TokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户登录接口 zhangzirong
 */
@Api(value = "用户登录接口")
@RestController
@RequestMapping("/login")
@Slf4j
public class UserLoginContorller {
	private final static Logger logger = LoggerFactory.getLogger(UserLoginContorller.class);
	@Resource
	private UserLoginService userLoginService;

	@Resource 
	private UserService userService;

	@Resource
	private IUserLoginService iUserLoginService;

	@Resource
	private IUserService iUserService;

	@Resource
	private DlUserAuthsService dlUserAuthsService;


	@ApiOperation(value = "彩小秘系列（包括球多多）密码登录店铺", notes = "彩小秘系列（包括球多多）密码登录店铺")
	@PostMapping("/loginByPassByCxm")
	public BaseResult<com.dl.store.dto.UserLoginDTO> loginByPassByCxm(@RequestBody UserLoginWithPassParam userLoginMobileParam, HttpServletRequest request) {
		DlUserAuths dlUserAuths = dlUserAuthsService.queryBindsUserByThirdMobile(userLoginMobileParam.getMobile());
		if(dlUserAuths == null){
			return ResultGenerator.genResult(MemberEnums.NOT_BINDS_CXM.getcode(),MemberEnums.NOT_BINDS_CXM.getMsg());
		}

		//验证密码
		MobileAndPassParam mobileAndPassParam = new MobileAndPassParam();
		mobileAndPassParam.setMobile(userLoginMobileParam.getMobile());
		mobileAndPassParam.setPass(userLoginMobileParam.getPassword());
		BaseResult<UserDTO>  userDTOBaseResult = iUserService.queryUserByMobileAndPass(mobileAndPassParam);
		if(!userDTOBaseResult.isSuccess()){
			return ResultGenerator.genResult(userDTOBaseResult.getCode(),userDTOBaseResult.getMsg());
		}

		//生成token
		Integer userId = dlUserAuths.getUserId();
		String token = TokenUtil.genToken(userId, Integer.valueOf(userLoginMobileParam.getLoginSource()));
		com.dl.store.dto.UserLoginDTO uDTO = new com.dl.store.dto.UserLoginDTO();
		uDTO.setMobile(userDTOBaseResult.getData().getMobile());
		uDTO.setToken(token);

		return ResultGenerator.genSuccessResult("success",uDTO);
	}

	@ApiOperation(value = "密码登录", notes = "密码登录")
	@PostMapping("/loginByPass")
	public BaseResult<com.dl.store.dto.UserLoginDTO> loginByPass(@RequestBody UserLoginWithPassParam userLoginMobileParam, HttpServletRequest request) {
		BaseResult<com.dl.store.dto.UserLoginDTO> loginByPass = userLoginService.loginByPass(userLoginMobileParam);
		if (loginByPass.getCode() == 0) {
			String token = loginByPass.getData().getToken();
			request.getSession().setAttribute("user_token", token);
		}
		return loginByPass;
	}

	@ApiOperation(value = "短信验证码登录", notes = "短信验证码登录")
	@PostMapping("/loginBySms")
	public BaseResult<com.dl.store.dto.UserLoginDTO> loginBySms(@RequestBody UserLoginWithSmsParam userLoginMobileParam, HttpServletRequest request) {
		BaseResult<com.dl.store.dto.UserLoginDTO> loginBySms = userLoginService.loginBySms(userLoginMobileParam, request);
		if (loginBySms.getCode() == 0) {
			String token = loginBySms.getData().getToken();
			request.getSession().setAttribute("user_token", token);
		}
		return loginBySms;
	}

	@ApiOperation(value = "用户注销", notes = "用户注销")
	@PostMapping("/logout")
	public BaseResult<String> logout(@RequestBody StrParam strPaaram, HttpServletRequest request) {
		userLoginService.loginLogOut();
		TokenUtil.invalidateCurToken();
		request.getSession().removeAttribute("user_token");
		return ResultGenerator.genSuccessResult("用户注销成功");
	}

}
