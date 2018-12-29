package com.dl.store.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("回滚")
@Data
public class OrderRollBackParam {
	@ApiModelProperty("订单orderSn")
    private String orderSn;
}
