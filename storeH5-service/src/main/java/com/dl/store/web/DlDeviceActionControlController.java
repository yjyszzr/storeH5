package com.dl.store.web;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.member.api.IUserService;
import com.dl.shop.auth.api.IAuthService;
import com.dl.shop.auth.dto.InvalidateTokenDTO;
import com.dl.store.dto.DlDeviceActionControlDTO;
import com.dl.store.dto.UserLoginDTO;
import com.dl.store.model.DlDeviceActionControl;
import com.dl.store.model.User;
import com.dl.store.param.TokenParam;
import com.dl.store.service.DlDeviceActionControlService;
import com.dl.store.service.DlUserAuthsService;
import com.dl.store.service.UserLoginService;
import com.dl.store.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
* Created by zzr on 2018/12/14.
*/
@RestController
@RequestMapping("/deviceActionControl")
@Slf4j
public class DlDeviceActionControlController {
    @Resource
    private DlDeviceActionControlService dlDeviceActionControlService;

    @Resource
    private IUserService iuserService;

    @Resource
    private IAuthService iAuthService;

    @Resource
    private DlUserAuthsService dlUserAuthsService;

    @Resource
    private UserLoginService userLoginService;

    @Resource
    private UserService userService;



    @ApiOperation(value = "查询用户设备弹框次数forH5", notes = "查询用户设备弹框次数forH5")
    @PostMapping("/queryDeviceAlertTimesForH5")
    public BaseResult<DlDeviceActionControlDTO> queryDeviceAlertTimesForH5(@RequestBody TokenParam param){
        DlDeviceActionControlDTO deviceCtrlDto = new DlDeviceActionControlDTO();
        if(StringUtils.isEmpty(param.getUserToken())){
            deviceCtrlDto.setAlertTimes(1);
            return ResultGenerator.genSuccessResult("success",deviceCtrlDto);
        }

        Integer userId = null;
        InvalidateTokenDTO invalidateTokenDTO = new InvalidateTokenDTO();
        invalidateTokenDTO.setToken(param.getUserToken());
        BaseResult<Integer> rst = iAuthService.getUserIdByToken(invalidateTokenDTO);
        if(rst.isSuccess()){
            userId = rst.getData();
            log.info("success-----"+userId);
        }

        log.info("userId:"+userId);
        if(userId == null){
            deviceCtrlDto.setAlertTimes(1);
        }else{
            Integer curTime = DateUtil.getCurrentTimeLong();
            DlDeviceActionControl deviveCtrl = dlDeviceActionControlService.queryDeviceAlertTimesForH5(String.valueOf(userId),1,curTime);
            if(deviveCtrl!= null && deviveCtrl.getAlertTimes() == 1){
                deviceCtrlDto.setAlertTimes(deviveCtrl.getAlertTimes());
            }else{
                DlDeviceActionControl dctrl = new DlDeviceActionControl();
                dctrl.setMac(String.valueOf(userId));
                dctrl.setBusiType(1);
                dctrl.setAddTime(DateUtil.getCurrentTimeLong());
                dctrl.setUpdateTime(DateUtil.getCurrentTimeLong());
                dctrl.setAlertTimes(1);
                dlDeviceActionControlService.save(dctrl);
                deviceCtrlDto.setAlertTimes(0);
            }
            //已经绑定并且登录的用户，返回最新的登录token
            Boolean binds = dlUserAuthsService.queryBindThird(userId);
            if(binds){
                User user = userService.queryUserByUserId(userId);
                if(user!=null){
                    UserLoginDTO userLoginDTO = userLoginService.queryUserLoginDTOByMobile(user.getMobile(),"4");
                    deviceCtrlDto.setUserToken(userLoginDTO.getToken());
                }
            }
        }
        return ResultGenerator.genSuccessResult("success",deviceCtrlDto);
    }

    //        com.dl.member.param.TokenParam tokenParam = new com.dl.member.param.TokenParam();
//        tokenParam.setUserToken(param.getUserToken());
//        BaseResult<UserDTO> userDTOBaseResult = iuserService.queryUserInfoByToken(tokenParam);
//        if(!userDTOBaseResult.isSuccess()){
//            log.error("iuserService.queryUserInfoByToken 接口异常"+userDTOBaseResult.getMsg());
//            return ResultGenerator.genResult(userDTOBaseResult.getCode(),userDTOBaseResult.getMsg());
//        }
//
//        UserDTO userDTO = userDTOBaseResult.getData();
}
