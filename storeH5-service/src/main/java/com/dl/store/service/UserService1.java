package com.dl.store.service;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dl.base.service.AbstractService;
import com.dl.store.dao.UserMapper1;
import com.dl.store.model.User;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional("transactionManager1")
public class UserService1 extends AbstractService<User>{
	@Resource
	private UserMapper1 userMapper;

	public Boolean queryCxmUserIsSuperWhite(String mobile){
		User user = userMapper.queryUserByMobile(mobile);
		log.info("[queryCxmUserIsSuperWhite]" + " user:" + user);
		if(user != null && user.getIsSuperWhite() != null && user.getIsSuperWhite() == 1){
			return true;
		}else{
			return false;
		}
	}
}
