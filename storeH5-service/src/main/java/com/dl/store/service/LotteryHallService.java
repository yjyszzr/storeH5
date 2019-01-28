package com.dl.store.service;

import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.lotto.api.ISuperLottoService;
import com.dl.lotto.dto.LottoDTO;
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
	 @Resource
	 private ISuperLottoService iSuperLottoService;

	 public List<DlPlayClassifyDetailDTO> listAllClassify(){
		List<DlPlayClassifyDetailDTO> dlPlayClassifyDetailDTOs = new ArrayList<DlPlayClassifyDetailDTO>();
		List<LotteryClassify> classifyList = lotteryClassifyMapper.selectAllLotteryClasses();
		for(LotteryClassify lotteryClassify:classifyList) {
			DlPlayClassifyDetailDTO dlPlayDetailDto = new DlPlayClassifyDetailDTO();
			dlPlayDetailDto.setLotteryId(String.valueOf(lotteryClassify.getLotteryClassifyId()));
			dlPlayDetailDto.setPlayClassifyImg(lotteryClassify.getLotteryImg());
			dlPlayDetailDto.setPlayClassifyLabelName(lotteryClassify.getStatusReason());
			dlPlayDetailDto.setStatus(lotteryClassify.getStatus());
			dlPlayDetailDto.setSubTitle(lotteryClassify.getSubTitle());
			if(1 == lotteryClassify.getLotteryClassifyId()) {
//				String isLogin = "&isLogin=0";
//				if(userId != null) {
//					isLogin = "&isLogin=1";
//				}
				dlPlayDetailDto.setRedirectUrl(lotteryClassify.getRedirectUrl());
				dlPlayDetailDto.setPlayClassifyName(lotteryClassify.getLotteryName());
			}else if(2 == lotteryClassify.getLotteryClassifyId()){
				dlPlayDetailDto.setRedirectUrl(lotteryClassify.getRedirectUrl());
				dlPlayDetailDto.setPlayClassifyName(lotteryClassify.getLotteryName());
				String subTitle = queryLatestLottoPrizes();
				dlPlayDetailDto.setSubTitle(subTitle);
			}else {
				dlPlayDetailDto.setRedirectUrl("");
				dlPlayDetailDto.setPlayClassifyName(lotteryClassify.getStatusReason());
			}
			dlPlayClassifyDetailDTOs.add(dlPlayDetailDto);
		}

		return dlPlayClassifyDetailDTOs;
	 }
	 
	public String queryLatestLottoPrizes() {
		String prizes = "";
		EmptyParam emptyParam = new EmptyParam();
		emptyParam.setEmptyStr("");
		BaseResult<LottoDTO> lottoDtoRst = iSuperLottoService.queryLottoLatestPrizes(emptyParam);
		if (0 != lottoDtoRst.getCode()) {
			return prizes;
		}
		LottoDTO lottoDTO = lottoDtoRst.getData();
		return lottoDTO.getPrizes();
	}
}
