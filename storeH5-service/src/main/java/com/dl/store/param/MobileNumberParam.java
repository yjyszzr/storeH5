package com.dl.store.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 手机号参数
 *
 * @author zhangzirong
 */
@Data
public class MobileNumberParam {

    @ApiModelProperty(value = "手机号码")
    @NotBlank(message = "请填写手机号")
    private String mobileNumber;
}
