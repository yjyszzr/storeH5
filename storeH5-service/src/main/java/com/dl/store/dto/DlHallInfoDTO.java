package com.dl.store.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DlHallInfoDTO {
	@ApiModelProperty(value = "首页列表")
	private List<DlPlayClassifyDetailDTO> list;

	@ApiModelProperty(value = "金额")
	private String money;

	@ApiModelProperty(value = "是否是超级白名单 0否 1是")
	private String isSuperWhite;

	@ApiModelProperty(value = "我的卡券数量")
	private String myBonusNum = "";
}
