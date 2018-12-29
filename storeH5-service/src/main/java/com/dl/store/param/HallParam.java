package com.dl.store.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("首页信息")
@Data
public class HallParam {
	@ApiModelProperty("店铺ID ")
    private Integer storeId;
}
