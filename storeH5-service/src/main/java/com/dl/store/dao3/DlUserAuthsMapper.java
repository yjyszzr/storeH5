package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.DlUserAuths;
import org.apache.ibatis.annotations.Param;

public interface DlUserAuthsMapper extends Mapper<DlUserAuths> {

    Integer insertWithId(DlUserAuths dlUserAuths);

    Integer delUserAuthById(@Param("userId") Integer userId);

    Integer countBindThird(@Param("thirdUserId") Integer thirdUserId);

    DlUserAuths getUserAuthById(@Param("userId") Integer userId);

    DlUserAuths getUserAuthByThirdUserId(@Param("thirdUserId") Integer thirdUserId);

    DlUserAuths getUserAuthByThirdMobile(@Param("thirdMobile") String thirdMobile);
}