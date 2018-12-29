package com.dl.store.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("派奖")
@Data
public class AwardParam{
	@ApiModelProperty("订单Sn")
    private String orderSn;
}
