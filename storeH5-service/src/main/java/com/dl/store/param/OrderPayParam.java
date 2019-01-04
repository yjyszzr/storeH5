package com.dl.store.param;

import org.hibernate.validator.constraints.NotBlank;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("订单支付")
@Data
public class OrderPayParam {
	@ApiModelProperty("店铺ID ")
	@NotBlank(message="店铺号不能为空")
    private Integer storeId;
	@ApiModelProperty("订单号")
	@NotBlank(message="订单号不能为空")
	private String orderSn;
	@ApiModelProperty("代金券id")
	@NotBlank(message="代金券id")
	private Integer bonusId;
}
