package com.dl.store.param;

import com.dl.member.param.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("账户类型信息")
@Data
public class AmountTypeParam extends PageParam{

	@ApiModelProperty("账户类型：0-全部 1-奖金 2-充值 3-购彩 4-提现 5-红包 8-退款")
    private String amountType;
	
	@ApiModelProperty("时间段：0-全部 1-当天 2-前一周 3-前一个月 4-前三个月 ")
    private String timeType;
	
}