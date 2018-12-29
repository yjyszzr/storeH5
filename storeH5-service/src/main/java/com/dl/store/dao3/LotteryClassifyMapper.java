package com.dl.store.dao3;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.dl.base.mapper.Mapper;
import com.dl.store.model.LotteryClassify;

public interface LotteryClassifyMapper extends Mapper<LotteryClassify> {
	
	List<LotteryClassify> selectAllLotteryClassData();
	
	List<LotteryClassify> selectAllLotteryClasses();
	
	LotteryClassify selectLotteryClassesById(@Param("lotteryClassifyId") Integer lotteryClassifyId);
}