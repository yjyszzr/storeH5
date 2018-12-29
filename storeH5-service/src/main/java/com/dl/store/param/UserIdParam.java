package com.dl.store.param;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserIdParam {
	 @ApiModelProperty(value = "用户ID")
	 private Integer userId;
}
