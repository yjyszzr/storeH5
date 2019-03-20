package com.dl.store.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;

@Data
public class DlDeviceActionControlDTO {

    @ApiModelProperty(value = " mac地址")
    private String mac = "";

    @ApiModelProperty(value = "业务类型：1-h5代表h5店铺弹框")
    private Integer busiType = 1;

    @Column(name = "弹出次数:0-代表未弹出过 1- 代表弹出过一次，n-代表弹出过n次")
    private Integer alertTimes = 0;

    @Column(name = "添加时间")
    private Integer addTime = 0;

    @Column(name = "更新时间")
    private Integer updateTime = 0;

    @Column(name = "二维码图片地址")
    private String picUrl = "";

    @Column(name = "登录token: 不为空的情况下替换前端缓存的token，为空的情况下不替换")
    private String userToken = "";
}