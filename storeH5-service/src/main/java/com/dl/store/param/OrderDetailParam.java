package com.dl.store.param;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderDetailParam implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("订单编号")
	private String orderSn;
	@ApiModelProperty("店铺Id")
	private Integer storeId;
	@ApiModelProperty("订单Id")
	private String orderId;
}
