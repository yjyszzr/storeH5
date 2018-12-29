package com.dl.store.enums;

public enum OrderEnums {
	//301012
	ORDER_PAID(302006,"订单已支付"),
	USER_MONEY_PAY_FAILE(302005,"订单支付余额扣除失败"),
	USER_MONEY_NOTENOUGH(302004,"用户余额不够"),
	USER_ID_EMPTY(302003,"用户ID为空"),
	STORE_ID_EMPTY(302002,"店铺ID为空"),
	ORDER_SN_EMPTY(302001,"订单号为空"),
	ORDER_EMPTY(302000,"查询订单失败");
	
	private Integer code;
    private String msg;

    private OrderEnums(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getcode() {
        return code;
    }

    public void setcode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
