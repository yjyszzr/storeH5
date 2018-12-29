package com.dl.store.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("是否结果")
@Data
public class YesOrNoDTO {

	@ApiModelProperty("0-否 1-是")
    private String yesOrNo;

}