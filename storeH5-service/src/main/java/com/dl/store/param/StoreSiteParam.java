package com.dl.store.param;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StoreSiteParam {
	 @ApiModelProperty(value = "storeId")
	 @NotBlank(message = "店铺ID")
	 private Integer storeId;
}
