package com.dl.store.web;

import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.base.util.SessionUtil;
import com.dl.store.dto.YesOrNoDTO;
import com.dl.store.param.MobileAndPassParam;
import com.dl.store.dto.UserDTO;
import com.dl.store.dto.UserLoginDTO;
import com.dl.store.param.*;
import com.dl.store.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;


/**
 * Created by zzr on 2018/12/15.
 */
@RestController
@RequestMapping("/user")
public class UserController {
	@Resource
	private UserService userService;
	
	@ApiOperation(value = "校验手机号", notes = "校验手机号")
	@PostMapping("/validateMobile")
	public BaseResult<String> validateMobile(@RequestBody MobileNumberParam mobileNumberParam) {
		return userService.validateUserMobile(mobileNumberParam.getMobileNumber());
	}

	@ApiOperation(value = "找回用户登录密码", notes = "找回用户登录密码")
	@PostMapping("/updateLoginPass")
	public BaseResult<String> updateLoginPass(@RequestBody UserLoginPassParam userLoginPassParam) {
		return userService.updateUserLoginPass(userLoginPassParam.getUserLoginPass(), userLoginPassParam.getMobileNumber(), userLoginPassParam.getSmsCode());
	}

	@ApiOperation(value = "修改用户登录密码", notes = "修改用户登录密码")
	@PostMapping("/setLoginPass")
	public BaseResult<String> setLoginPass(@RequestBody SetLoginPassParam param) {
		Integer userId = SessionUtil.getUserId();
		return userService.setUserLoginPass(param, userId);
	}

	@ApiOperation(value = "查询用户信息除了登录密码和支付密码", notes = "查询用户信息除了登录密码和支付密码")
	@PostMapping("/userInfoExceptPass")
	public BaseResult<UserDTO> queryUserInfo(@RequestBody StrParam strParam) {
		return userService.queryUserByUserIdExceptPass();
	}

	@ApiOperation(value = "被动绑定彩小秘，针对弹框，入参是彩小秘的登录token，返回值店铺H5的登录token", notes = "绑定彩小秘，针对弹框，入参是彩小秘的登录token，返回值店铺H5的登录token")
	@PostMapping("/bindsThirdAndReg")
	public BaseResult<UserLoginDTO> bindsThirdAndReg(@RequestBody TokenParam tokenParam) {
		return userService.bindsThirdAndReg(tokenParam.getUserToken());
	}

	@ApiOperation(value = "主动绑定彩小秘，针对设置界面的绑定", notes = "主动绑定彩小秘，针对设置界面的绑定")
	@PostMapping("/bindsThird")
	public BaseResult<String> bindsThird(@RequestBody MobileAndPassParam param) {
		return userService.bindsThird(param);
	}

	@ApiOperation(value = "主动解绑，针对设置界面的解绑", notes = "主动解绑，针对设置界面的解绑")
	@PostMapping("/unBindsThird")
	public BaseResult<String> unBindsThird(@RequestBody MobileAndPassParam param) {
		return userService.unBindsThird(param);
	}

	@ApiOperation(value = "是否有未读消息", notes = "是否有未读消息绑")
	@PostMapping("/hasUnreadMes")
	public BaseResult<YesOrNoDTO> hasUnreadMes(@RequestBody EmptyParam param) {
		return userService.hasUnreadMes();
	}

	@ApiOperation(value = "已经读取消息", notes = "已经读取消绑")
	@PostMapping("/readMes")
	public BaseResult<String> readMes(@RequestBody EmptyParam param) {
		return userService.readMes();
	}

}
