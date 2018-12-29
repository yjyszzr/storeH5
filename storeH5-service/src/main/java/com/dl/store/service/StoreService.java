package com.dl.store.service;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.store.dao3.StoreMapper;
import com.dl.store.dto.StoreSiteDTO;
import com.dl.store.model.Store;

@Service
@Transactional("transactionManager3")
public class StoreService {

	 @Resource
	 private StoreMapper storeMapper;
	 
	 public BaseResult<StoreSiteDTO> getStoreSite(int storeId){
		 Store store = new Store();
		 store.setStoreId(storeId);
		 Store rStore = storeMapper.queryInfo(store);
		 StoreSiteDTO storeDTO = null;
		 if(rStore != null) {
			 storeDTO = new StoreSiteDTO();
			 storeDTO.setStoreId(storeId);
			 storeDTO.setName(rStore.getName());
			 storeDTO.setWechat(rStore.getWechat());
			 storeDTO.setAddrPic(rStore.getAddrPic());
			 storeDTO.setStorePic(rStore.getStorePic());
		 }
		 return ResultGenerator.genSuccessResult("succ",storeDTO);
	 }
}
