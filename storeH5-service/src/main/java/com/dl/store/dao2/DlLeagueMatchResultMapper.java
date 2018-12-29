package com.dl.store.dao2;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.DlLeagueMatchResult;

public interface DlLeagueMatchResultMapper extends Mapper<DlLeagueMatchResult> {
 

	List<DlLeagueMatchResult> queryMatchResultsByPlayCodes(@Param("playCodes")List<String> playCodes);
}