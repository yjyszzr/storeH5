package com.dl.store.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("账户token信息")
@Data
public class TokenParam {
    @ApiModelProperty("用户的token ")
    private String userToken;

    @ApiModelProperty("type:1-app 2-h5")
    private String type;
}
