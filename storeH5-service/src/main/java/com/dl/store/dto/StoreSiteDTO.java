package com.dl.store.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StoreSiteDTO {

	@ApiModelProperty(value = "店铺ID")
	private Integer storeId;

	@ApiModelProperty(value = "店铺名称")
	private String name;
	
	@ApiModelProperty(value = "店铺微信")
	private String wechat;
	
	@ApiModelProperty(value = "地址图片")
	private String addrPic;
	
	@ApiModelProperty(value = "店铺Pic")
	private String storePic;
}
