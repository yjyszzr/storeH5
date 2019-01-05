package com.dl.store.dao;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.User;
import org.apache.ibatis.annotations.Param;

/**
 * 数据源1下的UserMapper
 */
public interface UserMapper1 extends Mapper<User> {

	User queryUserByMobile(@Param("mobile") String mobile);
}