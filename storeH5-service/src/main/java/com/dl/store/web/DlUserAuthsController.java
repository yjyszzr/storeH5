package com.dl.store.web;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.store.dto.YesOrNoDTO;
import com.dl.store.param.UserIdParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import com.dl.store.service.DlUserAuthsService;
import javax.annotation.Resource;
import java.util.List;

/**
* Created by CodeGenerator on 2018/12/17.
*/
@RestController
@RequestMapping("/user/auths")
public class DlUserAuthsController {
    @Resource
    private DlUserAuthsService dlUserAuthsService;

    @ApiOperation(value = "根据app用户id查询是否绑定店铺用户", notes = "根据app用户id查询是否绑定店铺用户")
    @PostMapping("/queryBinds")
    public BaseResult<YesOrNoDTO> queryBinds(@RequestBody UserIdParam param){
        YesOrNoDTO rstDto = new YesOrNoDTO();
        rstDto.setYesOrNo("0");
        Boolean rst = dlUserAuthsService.queryBindThird(param.getUserId());
        if(rst){
            rstDto.setYesOrNo("1");
        }
        return ResultGenerator.genSuccessResult("success",rstDto);
    }

}
