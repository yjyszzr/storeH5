package com.dl.store.exception;

import com.dl.base.exception.ServiceException;
import com.dl.store.enums.OrderExceptionEnum;

public class SubmitOrderException extends ServiceException {

	private static final long serialVersionUID = 7660513083199778997L;

	public SubmitOrderException(OrderExceptionEnum orderExceptionEnum) {
		super(orderExceptionEnum.getCode(), orderExceptionEnum.getMsg());
	}
	
	public SubmitOrderException(Integer code, String msg) {
		super(code, msg);
	}
}
