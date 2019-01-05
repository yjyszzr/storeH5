package com.dl.store.service;

import com.dl.store.dao3.LotteryClassifyMapper;
import com.dl.store.dto.DlPlayClassifyDetailDTO;
import com.dl.store.model.LotteryClassify;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional("transactionManager3")
public class LotteryHallService {
	 @Resource
	 private LotteryClassifyMapper lotteryClassifyMapper;


	 public List<DlPlayClassifyDetailDTO> listAllClassify(){
		List<DlPlayClassifyDetailDTO> dlPlayClassifyDetailDTOs = new ArrayList<DlPlayClassifyDetailDTO>();
		List<LotteryClassify> classifyList = lotteryClassifyMapper.selectAllLotteryClasses();
		for(LotteryClassify lotteryClassify:classifyList) {
			DlPlayClassifyDetailDTO dlPlayDetailDto = new DlPlayClassifyDetailDTO();
			dlPlayDetailDto.setLotteryId(String.valueOf(lotteryClassify.getLotteryClassifyId()));
			dlPlayDetailDto.setPlayClassifyImg(lotteryClassify.getLotteryImg());
			dlPlayDetailDto.setPlayClassifyLabelName(lotteryClassify.getSubTitle());
			dlPlayDetailDto.setStatus(lotteryClassify.getStatus());
			if(1 == lotteryClassify.getLotteryClassifyId()) {
//				String isLogin = "&isLogin=0";
//				if(userId != null) {
//					isLogin = "&isLogin=1";
//				}
				dlPlayDetailDto.setRedirectUrl(lotteryClassify.getRedirectUrl());
				dlPlayDetailDto.setPlayClassifyName(lotteryClassify.getLotteryName());
			}else {
				dlPlayDetailDto.setRedirectUrl("");
				dlPlayDetailDto.setPlayClassifyName(lotteryClassify.getStatusReason());
			}
			dlPlayDetailDto.setSubTitle(lotteryClassify.getSubTitle());
			dlPlayClassifyDetailDTOs.add(dlPlayDetailDto);
		}

		return dlPlayClassifyDetailDTOs;
	 }
}
