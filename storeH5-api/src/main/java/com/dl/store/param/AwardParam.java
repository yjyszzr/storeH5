package com.dl.store.param;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("派奖@久幺扣钱")
@Data
public class AwardParam{
	@ApiModelProperty("订单Sn")
    private String orderSn;
	
	@ApiModelProperty("久幺userid")
    private Integer userId;
	
	@ApiModelProperty("久幺storeid")
    private Integer storeId;
	
	@ApiModelProperty("久幺操作金额")
    private BigDecimal ticketAmount;
	
	
}
