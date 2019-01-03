package com.dl.store.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ApiModel("订单实际付款金额")
@Data
public class BonusLimitConditionParam {

	@ApiModelProperty("订单实际付款金额")
	@NotNull
    private BigDecimal orderMoneyPaid;
	
    //可定义其他参数
	
}