package com.dl.store.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserBonusStatusParam{
	
	@ApiModelProperty("红包状态:空字符串-全部   0-未使用  1-已使用 2-已过期")
	@NotNull(message = "状态不能为空")
	private String status;

	@ApiModelProperty("店铺id")
	@NotNull(message = "店铺id不能为空")
	private Integer storeId;

}
