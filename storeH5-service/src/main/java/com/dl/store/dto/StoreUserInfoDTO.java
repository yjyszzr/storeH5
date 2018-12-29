package com.dl.store.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("业务配置信息")
@Data
public class StoreUserInfoDTO {
	@ApiModelProperty(value = "是否是超级白名单")
	private String isSuperWhite;
	@ApiModelProperty(value = "用户余额")
	private String money;
}
