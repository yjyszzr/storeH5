package com.dl.store.service;

import com.dl.base.service.AbstractService;
import com.dl.store.dao3.UserLoginLogMapper;
import com.dl.store.model.UserLoginLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional("transactionManager3")
public class UserLoginLogService extends AbstractService<UserLoginLog> {
    @Resource
    private UserLoginLogMapper userLoginLogMapper;

}
