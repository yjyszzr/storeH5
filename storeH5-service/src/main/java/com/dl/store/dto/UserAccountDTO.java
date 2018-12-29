package com.dl.store.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 用户账户流水DTO
 * @author zhangzirong
 *
 */
@ApiModel("用户账户流水DTO")
@Data
public class UserAccountDTO {
	@ApiModelProperty("id")
    private Integer id;

	@ApiModelProperty("流水号")
    private String accountSn;

	@ApiModelProperty("变化的金额")
    private String changeAmount;

    @ApiModelProperty("添加时间:年月日")
    private String addTime;
    
    @ApiModelProperty("时分秒")
    private String shotTime;
    
    @ApiModelProperty("操作类型名称汉字")
    private String processTypeChar;
    
    @ApiModelProperty("操作类型名称")
    private String processTypeName;
    
    @ApiModelProperty("操作类型:0-全部 1-奖金 2-充值 3-购彩 4-提现 5-红包 6-账户回滚")
    private String processType;
    
    @ApiModelProperty("状态:目前指提现的状态:2-提现失败,1-提现成功,0-提现中")
    private String status;

    @ApiModelProperty("操作备注")
    private String note;
    
    @ApiModelProperty("查看第三方支付详情的id:对于提现详情的withdraw_sn就是这个值")
    private String payId;
}