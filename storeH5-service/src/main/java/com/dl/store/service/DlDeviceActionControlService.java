package com.dl.store.service;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.member.api.ISysConfigService;
import com.dl.member.api.IUserService;
import com.dl.member.dto.SysConfigDTO;
import com.dl.member.param.BusiIdsListParam;
import com.dl.shop.auth.api.IAuthService;
import com.dl.shop.auth.dto.InvalidateTokenDTO;
import com.dl.store.dao3.DlDeviceActionControlMapper;
import com.dl.store.dto.DlDeviceActionControlDTO;
import com.dl.store.dto.UserLoginDTO;
import com.dl.store.model.DlDeviceActionControl;
import com.dl.store.model.DlUserAuths;
import com.dl.store.param.TokenParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional("transactionManager3")
public class DlDeviceActionControlService extends AbstractService<DlDeviceActionControl> {
    @Resource
    private DlDeviceActionControlMapper dlDeviceActionControlMapper;

    @Resource
    private UserLoginService userLoginService;

    @Resource
    private IUserService iuserService;

    @Resource
    private IAuthService iAuthService;

    @Resource
    private DlUserAuthsService dlUserAuthsService;

    @Resource
    private UserService userService;

    @Resource
    private ISysConfigService iSysConfigService;

    public DlDeviceActionControl queryDeviceByIMEI(String mac){
       return dlDeviceActionControlMapper.queryDeviceByIMEI(mac);
    }

    public Integer updateDeviceCtrlUpdteTime(Integer updateTime,String mac){
        return dlDeviceActionControlMapper.updateDeviceUpdateTime(updateTime,mac);
    }

    public Integer updateDeviceCtrlAlertTime(Integer alertTimes,String mac){
        return dlDeviceActionControlMapper.updateDeviceCtrlAlertTime(alertTimes,mac);
    }

    public DlDeviceActionControl queryDeviceAlertTimesForH5(String userId,Integer busiType,Integer updateTime){
        return dlDeviceActionControlMapper.queryDeviceAlertTimesForH5(userId,busiType,updateTime);
    }

    public BaseResult<DlDeviceActionControlDTO> enterStoreAction(TokenParam param){
        DlDeviceActionControlDTO deviceCtrlDto = new DlDeviceActionControlDTO();
        if(StringUtils.isEmpty(param.getUserToken())){
            deviceCtrlDto.setAlertTimes(0);
            return ResultGenerator.genSuccessResult("success",deviceCtrlDto);
        }

        if(param.getType().equals("1")) {
            Integer userId = null;
            InvalidateTokenDTO invalidateTokenDTO = new InvalidateTokenDTO();
            invalidateTokenDTO.setToken(param.getUserToken());
            BaseResult<Integer> rst = iAuthService.getUserIdByToken(invalidateTokenDTO);
            if (rst.isSuccess()) {
                userId = rst.getData();
            }

            if (userId == null) {//非登录用户
                deviceCtrlDto.setAlertTimes(0);
            } else {//已登录用户
                Integer curTime = DateUtil.getCurrentTimeLong();
                DlDeviceActionControl deviveCtrl = this.queryDeviceAlertTimesForH5(String.valueOf(userId), 1, curTime);
                DlUserAuths userAuths = dlUserAuthsService.getUserAuthByThirdUserId(userId);
                if (deviveCtrl == null) {//首次进入的弹框
                    DlDeviceActionControl dctrl = new DlDeviceActionControl();
                    dctrl.setMac(String.valueOf(userId));
                    dctrl.setBusiType(1);
                    dctrl.setAddTime(DateUtil.getCurrentTimeLong());
                    dctrl.setUpdateTime(DateUtil.getCurrentTimeLong());
                    dctrl.setAlertTimes(1);
                    this.save(dctrl);

                    deviceCtrlDto.setAlertTimes(0);
                } else {//非首次告诉弹框几次
                    deviceCtrlDto.setAlertTimes(deviveCtrl.getAlertTimes());
                }
                if (userAuths == null) {//未绑定的用户,自动绑定
                    userService.bindsThirdAndReg(param.getUserToken());
                }
                UserLoginDTO userLoginDTO = userLoginService.queryUserLoginDTOByMobile(userAuths.getThirdMobile(), "4");
                deviceCtrlDto.setUserToken(userLoginDTO.getToken());
            }
        }else if(param.getType().equals("2")){
            deviceCtrlDto.setUserToken(param.getUserToken());
        }

        String picUrl = "";
        String wxNum = "";
        List<Integer> bidList = new ArrayList<>();
        BusiIdsListParam bids = new BusiIdsListParam();
        bidList.add(57);
        bidList.add(58);
        bids.setBusinessIdList(bidList);
        BaseResult<List<SysConfigDTO>> sysConfigList =  iSysConfigService.querySysConfigList(bids);
        if(sysConfigList.isSuccess()){
            picUrl = sysConfigList.getData().get(0).getValueTxt();
            wxNum = sysConfigList.getData().get(1).getValueTxt();
        }
        deviceCtrlDto.setPicUrl(picUrl);
        deviceCtrlDto.setWeixinNum(wxNum);

        return ResultGenerator.genSuccessResult("success",deviceCtrlDto);
    }

}
