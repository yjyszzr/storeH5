package com.dl.store.dto;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DlHallInfoDTO {
	@ApiModelProperty(value = "首页列表")
	private List<DlPlayClassifyDetailDTO> list;

	@ApiModelProperty(value = "金额")
	private String money;
	
	@ApiModelProperty(value = "不可提现余额")
	private String moneyLimit;

	@ApiModelProperty(value = "是否是超级白名单 0否 1是")
	private String isSuperWhite;

	@ApiModelProperty(value = "我的卡券数量")
	private String myBonusNum = "";

	@ApiModelProperty(value = "是否显示账户明细：0-不显示 1-显示")
	private String isShowAccount = "0";

	@ApiModelProperty(value = "是否显示我的代金券：0-不显示 1-显示")
	private String isShowBonus = "0";
}
