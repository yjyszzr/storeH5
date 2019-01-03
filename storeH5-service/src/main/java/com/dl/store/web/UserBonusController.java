package com.dl.store.web;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.store.dto.UserBonusDTO;
import com.dl.store.param.OrderSnParam;
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


//    @ApiOperation(value="根据userBonusId查询单个红包", notes="根据userBonusId查询单个红包",hidden=false)
//    @PostMapping("/queryUserBonus")
//    public BaseResult<UserBonusDTO> queryUserBonus(@RequestBody UserBonusIdParam userBonusIdParam) {
//    	UserBonusDTO userBonusDTO = userBonusService.queryUserBonus(userBonusIdParam.getUserBonusId());
//    	if(null == userBonusDTO) {
//    		return ResultGenerator.genFailResult("未查询到userBonusId="+userBonusIdParam+"的红包");
//    	}
//    	return ResultGenerator.genSuccessResult("根据userBonusId查询单个红包成功",userBonusDTO);
//    }

    @ApiOperation(value="根据状态查询有效的红包集合V2", notes="根据状态查询有效的红包集合V2",hidden=false)
    @PostMapping("/queryBonusListByStatusV2")
    public BaseResult<List<UserBonusDTO>> queryBonusListByStatusV2(@RequestBody UserBonusStatusParam userBonusStatusParam) {
        List<UserBonusDTO> userBonusDTOList =  userBonusService.queryBonusListByStatus(userBonusStatusParam.getStatus(),userBonusStatusParam.getStoreId());
        return ResultGenerator.genSuccessResult("根据状态查询有效的红包集合成功",userBonusDTOList);
    }

    @ApiOperation(value="支付的时候查询用户可用的红包列表V2，前端直接调用", notes="支付的时候查询用户可用的红包列表V2：前端直接调用",hidden=false)
    @PostMapping("/queryValidBonusListV2")
    public BaseResult<List<UserBonusDTO>> queryValidBonusListV2(@RequestBody OrderSnParam orderSnParam) {
        Integer userId = SessionUtil.getUserId();
        if (null == userId) {
            return ResultGenerator.genNeedLoginResult("请登录");
        }
        List<UserBonusDTO> userBonusDTOList =  userBonusService.queryValidBonusListForPayV2(orderSnParam,userId);
        return ResultGenerator.genSuccessResult("查询用户有效的红包列表成功",userBonusDTOList);
    }

//    @ApiOperation(value="更新红包的过期状态", notes="更新红包的过期状态",hidden=false)
//	@RequestMapping(path="/updateBonusExpire", method=RequestMethod.POST)
//	public BaseResult<String> updateBonusExpire(@RequestBody EmptyParam emptyParam) {
//    	userBonusService.updateBonusExpire();
//		return ResultGenerator.genSuccessResult("更新过期红包成功");
//	}
    
}
