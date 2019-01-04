package com.dl.store.web;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.store.dto.UserBonusDTO;
import com.dl.store.param.BonusLimitConditionParam;
import com.dl.store.param.OrderSnParam;
import com.dl.store.param.PayBonusParam;
import com.dl.store.param.UserBonusStatusParam;
import com.dl.store.service.UserBonusService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
* Created by zhangzirong on 2018/03/15.
*/
@RestController
@RequestMapping("/user/bonus")
public class UserBonusController {
    @Resource
    private UserBonusService userBonusService;


    @ApiOperation(value="根据状态查询有效的红包集合", notes="根据状态查询有效的红包集合",hidden=false)
    @PostMapping("/bonusListByStatus")
    public BaseResult<List<UserBonusDTO>> queryBonusListByStatus(@RequestBody UserBonusStatusParam userBonusStatusParam) {
        List<UserBonusDTO> userBonusDTOList =  userBonusService.queryBonusListByStatus(userBonusStatusParam.getStatus(),userBonusStatusParam.getStoreId());
        return ResultGenerator.genSuccessResult("根据状态查询有效的红包集合成功",userBonusDTOList);
    }

    @ApiOperation(value="支付的时候查询用户可用的红包列表，前端直接调用", notes="支付的时候查询用户可用的红包列表：前端直接调用",hidden=false)
    @PostMapping("/validBonusList")
    public BaseResult<List<UserBonusDTO>> queryValidBonusList(@RequestBody OrderSnParam orderSnParam) {
        Integer userId = SessionUtil.getUserId();
        if (null == userId) {
            return ResultGenerator.genNeedLoginResult("请登录");
        }
        List<UserBonusDTO> userBonusDTOList =  userBonusService.queryValidBonusListForPayV2(orderSnParam,userId);
        return ResultGenerator.genSuccessResult("查询用户有效的红包列表成功",userBonusDTOList);
    }

    @ApiOperation(value="支付的时候可用的红包列表数量: 内部接口", notes="可用的红包列表数量：内部接口",hidden=false)
    @PostMapping("/validBonusSize")
    public BaseResult<Integer> validBonusSize(@RequestBody BonusLimitConditionParam bonusLimitConditionParam) {
       Integer size =  userBonusService.queryValidBonusListSize(bonusLimitConditionParam);
        return ResultGenerator.genSuccessResult("查询用户有效的红包列表成功",size);
    }

    @ApiOperation(value="更新红包状态为已使用: 内部接口", notes="更新红包状态为已使用：内部接口",hidden=false)
    @PostMapping("/updateUserBonusStatusUsed")
    public BaseResult<String> updateUserBonusStatusUsed(@RequestBody PayBonusParam payBonusParam) {
        userBonusService.updateUserBonusStatusUsed(payBonusParam.getUserBonusId(),payBonusParam.getOrderSn());
        return ResultGenerator.genSuccessResult("更新红包状态为已使用成功");
    }


}
