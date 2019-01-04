package com.dl.store.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class OrderSnParam implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("订单编号")
	@NotNull(message = "orderSn不能为空")
	private String orderSn;
	
	@ApiModelProperty("店铺ID")
	@NotNull(message = "storeId不能为空")
	private Integer storeId;

	@ApiModelProperty("红包状态: 0-未使用 2-已过期")
	@NotNull(message = "状态不能为空")
	private String status;
}
