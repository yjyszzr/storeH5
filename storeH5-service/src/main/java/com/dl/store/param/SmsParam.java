package com.dl.store.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 短信参数
 *
 * @author zhangzirong
 */
@Data
public class SmsParam {
		
    @ApiModelProperty(value = "手机号")
    @NotBlank(message = "手机号")
    private String mobile;

    @ApiModelProperty(value = "短信类型:0-短信登录验证码 1-注册验证码  2-忘记密码验证码")
    @NotBlank(message = "短信类型不能为空")
    private String smsType;
}
