package com.dl.store.web;

import com.dl.base.model.UserDeviceInfo;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.store.core.MemberConstant;
import com.dl.store.dto.UserLoginDTO;
import com.dl.store.enums.MemberEnums;
import com.dl.store.param.UserRegisterParam;
import com.dl.store.service.*;
import com.dl.store.util.TokenUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.lang3.StringUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* Created by CodeGenerator on 2018/03/08.
*/
@RestController
@RequestMapping("/user")
@Slf4j
public class UserRegisterController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
	
    @Resource
    private UserRegisterService userRegisterService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserLoginService userLoginService;

    /**
     * 新用户注册:
     * @param userRegisterParam
     * @param request
     * @return
     */
    @ApiOperation(value = "新用户注册", notes = "新用户注册")
    @PostMapping("/register")
    public BaseResult<UserLoginDTO> register(@RequestBody UserRegisterParam userRegisterParam, HttpServletRequest request) {
        String cacheSmsCode = stringRedisTemplate.opsForValue().get(MemberConstant.SMS_PREFIX + MemberConstant.SMS_TYPE_REGISTER + "_" + userRegisterParam.getMobile());
        if (StringUtils.isEmpty(cacheSmsCode) || !cacheSmsCode.equals(userRegisterParam.getSmsCode())) {
            return ResultGenerator.genResult(MemberEnums.SMSCODE_WRONG.getcode(), MemberEnums.SMSCODE_WRONG.getMsg());
        }

        String passWord = userRegisterParam.getPassWord();
        if(passWord.equals("-1")) {
        	userRegisterParam.setPassWord("");
        } else if(!passWord.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$")) {
    		return ResultGenerator.genResult(MemberEnums.PASS_FORMAT_ERROR.getcode(), MemberEnums.PASS_FORMAT_ERROR.getMsg());
    	} 	
        
    	BaseResult<Integer> regRst = userRegisterService.registerUser(userRegisterParam, request);
    	if(regRst.getCode() != 0) {
    		return ResultGenerator.genResult(regRst.getCode(),regRst.getMsg());
    	}
    	Integer userId = regRst.getData();
    	TokenUtil.genToken(userId, Integer.valueOf(userRegisterParam.getLoginSource()));
    	UserLoginDTO userLoginDTO = userLoginService.queryUserLoginDTOByMobile(userRegisterParam.getMobile(), userRegisterParam.getLoginSource());
		
    	stringRedisTemplate.delete(MemberConstant.SMS_PREFIX + MemberConstant.SMS_TYPE_REGISTER + "_" + userRegisterParam.getMobile());

    	return ResultGenerator.genSuccessResult("登录成功", userLoginDTO);
    }
    
    
    
}
