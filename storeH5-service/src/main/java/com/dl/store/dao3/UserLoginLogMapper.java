package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.UserLoginLog;
import org.apache.ibatis.annotations.Param;

public interface UserLoginLogMapper extends Mapper<UserLoginLog> {

	UserLoginLog getLastLog(@Param("userId") Integer userId);

	void updateLogOutTime(UserLoginLog ull);
}