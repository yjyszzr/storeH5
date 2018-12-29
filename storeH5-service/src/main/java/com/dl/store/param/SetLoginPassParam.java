package com.dl.store.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class SetLoginPassParam {

	@ApiModelProperty(value = "用户登录密码")
    @NotBlank(message = "请填写用户登录密码")
    private String userLoginPass;
    
    @ApiModelProperty(value = "原登录密码")
    private String oldLoginPass;
    
    @ApiModelProperty(value = "1修改密码0设置密码")
    private int type;
}
