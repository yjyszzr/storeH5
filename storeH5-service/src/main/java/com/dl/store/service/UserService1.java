package com.dl.store.service;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dl.base.service.AbstractService;
import com.dl.store.dao3.UserMapper;
import com.dl.store.model.User;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional("transactionManager1")
@Slf4j
public class UserService1 extends AbstractService<User>{
	@Resource
	private UserMapper userMapper;
	
	@Transactional("transactionManager1")
	public Boolean queryCxmUserIsSuperWhite(Integer mobile){
		User user = userMapper.queryUserByMobile(mobile);
		if(user.getIsSuperWhite() != null && user.getIsSuperWhite() == 1){
			return true;
		}else{
			return false;
		}
	}
}
