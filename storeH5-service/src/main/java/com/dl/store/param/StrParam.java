package com.dl.store.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 空字符串参数
 *
 * @author zhangzirong
 */
@Data
public class StrParam {
    @ApiModelProperty(value = "空字符串")
    private String str;

    public StrParam(){
    }

    public StrParam(String str){
        this.str = str;
    }
}
