package com.dl.store.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserBonusParam implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("订单号")
	private String orderSn;
	
	@ApiModelProperty("用户红包id")
	private Integer userBonusId;
}
