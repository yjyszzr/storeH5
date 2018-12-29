package com.dl.store.dao3;

import com.dl.base.mapper.Mapper;
import com.dl.store.model.Store;


public interface StoreMapper extends Mapper<Store>{

	public Store queryInfo(Store store);
}
