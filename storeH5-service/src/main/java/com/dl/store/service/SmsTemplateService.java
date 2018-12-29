package com.dl.store.service;

import com.dl.base.service.AbstractService;
import com.dl.store.dao3.SmsTemplateMapper;
import com.dl.store.model.SmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

@Service
@Transactional("transactionManager3")
public class SmsTemplateService extends AbstractService<SmsTemplate> {
    @Resource
    private SmsTemplateMapper smsTemplateMapper;
    
    public Integer querySmsByAppCodeName(Integer appCodeName) {
    	SmsTemplate sms = smsTemplateMapper.querySmsByAppCode(appCodeName);
		return sms.getSmsTemplateId();
    }

}
