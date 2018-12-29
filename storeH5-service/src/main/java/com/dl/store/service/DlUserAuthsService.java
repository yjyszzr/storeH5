package com.dl.store.service;
import com.dl.store.model.DlUserAuths;
import com.dl.base.service.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dl.store.dao3.DlUserAuthsMapper;
import javax.annotation.Resource;

@Service
@Transactional("transactionManager3")
public class DlUserAuthsService extends AbstractService<DlUserAuths> {
    @Resource
    private DlUserAuthsMapper dlUserAuthsMapper;

    public Integer saveThirdUser(DlUserAuths userAuths){
       return  dlUserAuthsMapper.insertWithId(userAuths);
    }

    public Integer delThirdUser(Integer userId){
        return dlUserAuthsMapper.delUserAuthById(userId);
    }

    public Boolean queryBindThird(Integer thirdUserId){
        Integer rst =  dlUserAuthsMapper.countBindThird(thirdUserId);
        if(rst > 0 ){
            return true;
        }
        return false;
    }

    public DlUserAuths queryBindsUser(Integer userId){
        return dlUserAuthsMapper.getUserAuthById(userId);
    }

    public DlUserAuths queryBindsUserByThirdMobile(String mobile){
        return dlUserAuthsMapper.getUserAuthByThirdMobile(mobile);
    }
}
