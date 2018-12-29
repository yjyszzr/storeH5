package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.SmsTemplate;
import org.apache.ibatis.annotations.Param;

public interface SmsTemplateMapper extends Mapper<SmsTemplate> {
	
	SmsTemplate querySmsByAppCode(@Param("appCodeName") Integer appCodeName);
}