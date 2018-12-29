package com.dl.store.service;

import com.dl.base.service.AbstractService;
import com.dl.store.dao3.DlPhoneChannelMapper;
import com.dl.store.model.DlPhoneChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional("transactionManager3")
public class DlPhoneChannelService extends AbstractService<DlPhoneChannel> {
    @Resource
    private DlPhoneChannelMapper dlPhoneChannelMapper;
    
    /**
     * 根据channel反查appCodeName
     * @param channel
     * @return
     */
    public Integer  queryAppCodeName(String channel) {
    	DlPhoneChannel phoneChannel = dlPhoneChannelMapper.queryPhoneChannelByChannel(channel);
    	Integer appCodeName = phoneChannel.getAppCodeName();
    	return appCodeName;
    }

}
