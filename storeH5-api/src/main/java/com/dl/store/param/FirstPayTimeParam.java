package com.dl.store.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("第一次支付时间参数")
@Data
public class FirstPayTimeParam {

    @ApiModelProperty("订单Sn")
    private String orderSn;

}
