package com.dl.store.service;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dl.store.dao3.UserMapper;
import com.dl.store.model.User;
import lombok.extern.slf4j.Slf4j;


@Service
@Transactional("transactionManager1")
@Slf4j
public class UserService1 {
	@Resource
	private UserMapper userMapper;
	
	public Boolean queryCxmUserIsSuperWhite(String mobile){
		User user = userMapper.queryUserByMobile(mobile);
		log.info("[queryCxmUserIsSuperWhite]" + " user:" + user + " mobile:" + mobile);
		if(user != null && user.getIsSuperWhite() != null && user.getIsSuperWhite() == 1){
			return true;
		}else{
			return false;
		}
	}
}
