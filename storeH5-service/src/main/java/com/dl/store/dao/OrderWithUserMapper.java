package com.dl.store.dao;

import java.util.List;

import com.dl.store.dto.OrderWithUserDTO;

public interface OrderWithUserMapper  {

	List<OrderWithUserDTO> selectOpenedAllRewardOrderList();
 
}