package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.DlDeviceActionControl;
import org.apache.ibatis.annotations.Param;

public interface DlDeviceActionControlMapper extends Mapper<DlDeviceActionControl> {

    DlDeviceActionControl queryDeviceByIMEI(@Param("mac") String mac);

    DlDeviceActionControl queryDeviceAlertTimesForH5(@Param("userId") String userId,@Param("busiType") Integer busiType,@Param("updateTime") Integer updateTime);

    int updateDeviceUpdateTime(@Param("updateTime") Integer updateTime, @Param("mac") String mac);

}