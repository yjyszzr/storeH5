package com.dl.store.web;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.store.configurer.MemberConfig;
import com.dl.store.core.MemberConstant;
import com.dl.store.enums.MemberEnums;
import com.dl.store.model.User;
import com.dl.store.param.SmsParam;
import com.dl.store.service.DlPhoneChannelService;
import com.dl.store.service.SmsService;
import com.dl.store.service.SmsTemplateService;
import com.dl.store.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/sms")
@Slf4j
public class SmsController {

	private final static Logger logger = LoggerFactory.getLogger(SmsController.class);

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private SmsService smsService;

	@Resource
	private UserService userService;

	@Resource
	private MemberConfig memberConfig;

	@Resource
	private SmsTemplateService smsTemplateService;

	/**
	 * 发送短信验证码
	 * 
	 * @param mobileNumberParam
	 * @return
	 */
	@ApiOperation(value = "发送短信验证码", notes = "发送短信验证码")
	@PostMapping("/sendSmsCode")
	public BaseResult<String> sendSms(@RequestBody SmsParam smsParam) {
		return smsService.sendSms(smsParam);
	}

}
