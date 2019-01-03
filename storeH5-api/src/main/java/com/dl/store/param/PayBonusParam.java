package com.dl.store.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PayBonusParam implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("订单编号")
	@NotNull(message = "order_sn不能为空")
	private String orderSn;
	
	@ApiModelProperty("用户红包id")
	@NotNull(message = "红包id不能为空")
	private Integer userBonusId;
}
