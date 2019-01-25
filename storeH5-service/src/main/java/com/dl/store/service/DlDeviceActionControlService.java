package com.dl.store.service;

import com.dl.base.service.AbstractService;
import com.dl.store.dao3.DlDeviceActionControlMapper;
import com.dl.store.model.DlDeviceActionControl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional("transactionManager3")
public class DlDeviceActionControlService extends AbstractService<DlDeviceActionControl> {
    @Resource
    private DlDeviceActionControlMapper dlDeviceActionControlMapper;

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

}
