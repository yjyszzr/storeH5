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
	@ApiModelProperty(value = "是否是超级白名单 0否 1是")
	private String isSuperWhite;
}
