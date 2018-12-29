package com.dl.store.exception;

import com.dl.base.exception.ServiceException;
import com.dl.store.enums.OrderExceptionEnum;

public class UserAccountException extends ServiceException {

	private static final long serialVersionUID = 1038934593021823059L;

	public UserAccountException(OrderExceptionEnum orderExceptionEnum) {
		super(orderExceptionEnum.getCode(), orderExceptionEnum.getMsg());
	}
	
	public UserAccountException(Integer code, String msg) {
		super(code, msg);
	}

}
