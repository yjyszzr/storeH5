package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.DlPhoneChannel;
import org.apache.ibatis.annotations.Param;

public interface DlPhoneChannelMapper extends Mapper<DlPhoneChannel> {
	DlPhoneChannel queryPhoneChannelByChannel(@Param("channel") String channel);
}