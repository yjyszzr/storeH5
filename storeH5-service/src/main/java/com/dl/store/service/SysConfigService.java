package com.dl.store.service;

import com.dl.store.dao3.SysConfigMapper;
import com.dl.store.dto.SysConfigDTO;
import com.dl.store.model.SysConfig;
import com.dl.base.service.AbstractService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

@Service
@Transactional("transactionManager3")
public class SysConfigService extends AbstractService<SysConfig> {
    @Resource
    private SysConfigMapper sysConfigMapper;
    
    public SysConfigDTO querySysConfig(Integer businessId) {
    	SysConfig sysConfig = this.findBy("businessId", businessId);
    	if(null == sysConfig) {
    		return new SysConfigDTO();
    	}
    	SysConfigDTO sysConfigDTO = new SysConfigDTO();
    	BeanUtils.copyProperties(sysConfig, sysConfigDTO);
		return sysConfigDTO;
    }

}
