package com.dl.store.core;

public class MemberConstant {

	public static final String BASE_PACKAGE = "com.dl.store";

	public static final String MODEL_PACKAGE = BASE_PACKAGE + ".model";
	public static final String MAPPER_PACKAGE = BASE_PACKAGE + ".dao";
	public static final String SERVICE_PACKAGE = BASE_PACKAGE + ".service";
	public static final String SERVICE_IMPL_PACKAGE = SERVICE_PACKAGE + ".impl";
	public static final String CONTROLLER_PACKAGE = BASE_PACKAGE + ".web";

	public static final String MAPPER_INTERFACE_REFERENCE = BASE_PACKAGE + ".mapper.Mapper";
	public static final String MAPPER_BASE = "com.dl.base.mapper.Mapper";

	public static final String SMS_PREFIX = "sms_";
	public static final String SMS_TYPE_LOGIN = "0";
	public static final String SMS_TYPE_REGISTER = "1";
	public static final String SMS_TYPE_RESETPASS = "2";
	public static final String SMS_TYPE_VERIFY = "3";

	public static final String REGISTER_TPLID = "76179";
	public static final String LOGIN_TPLID = "76180";
	public static final String RESETPASS_TPLID = "76181";
	public static final String SERVICE_TPLID = "76178";

	// public static final String REGISTER_TPLID = "66686";
	// public static final String LOGIN_TPLID = "66839";
	// public static final String RESETPASS_TPLID = "66838";
	// public static final String SERVICE_TPLID = "75003";

	/**
	 * 缓存存放验证码的有效时长
	 */
	public final static int SMS_REDIS_EXPIRED = 300;
	public static final String USER_DEFAULT_HEADING_IMG = "http://i9-static.jjwxc.net/novelimage.php?novelid=3385656&coverid=100&ver=d8d2de8a8fb398618c161418abc58f04";
	public static final String JUHEIMAGE_URL = "http://images.juheapi.com/banklogo/";
	public static final String LOGIN_SOURCE_ANDROID = "1";
	public static final String LOGIN_SOURCE_IOS = "2";
	public static final String LOGIN_SOURCE_PC = "3";
	public static final String LOGIN_SOURCE_H5 = "4";

	public static final String ANDROID = "android";
	public static final String IOS = "ios";
	public static final String PC = "pc";
	public static final String H5 = "h5";

	public static final int USER_STATUS_NOMAL = 0;
	public static final int USER_STATUS_LOCK = 1;
	public static final int USER_STATUS_FROZEN = 2;

	public static final int BONUS_STATUS_UNUSED = 0;// 红包未使用
	public static final int BONUS_STATUS_USED = 1;// 红包已使用
	public static final int BONUS_STATUS_EXPIRE = 2;// 红包已过期

	public static final int DELETE = 1;// //1代表已删除
	public static final int NOT_DELETE = 0;// 0代表未删除

	public static final Integer ACCOUNT_TYPE_TRADE_SURPLUS_SEND = 8; // 使用了部分或全部余额扣款类型
	public static final Integer ACCOUNT_TYPE_TRADE_SURPLUS_SEND_ROLLBACK = 9; // 使用了部分或全部余额扣款回滚类型

	public static final String USER_BANK_NO_DEFAULT = "0";// 非当前默认银行卡
	public static final String USER_BANK_DEFAULT = "1";// 当前默认银行卡

	public static final String USER_IS_NOT_REAL = "0";// 用户已没有进行过实名认证
	public static final String USER_IS_REAL = "1";// 用户已经进行过实名认证

	// 提现状态
	public static final String NOT_FINISH = "0";// 提现中
	public static final String FINISH = "1";// 提现成功
	public static final String FAILURE = "2";// 提现失败

	public static final Integer REWARD = 1;// 奖金
	public static final Integer RECHARGE = 2;// 充值
	public static final Integer BUY = 3;// 购彩
	public static final Integer WITHDRAW = 4;// 提现
	public static final Integer BONUS = 5;// 红包
	public static final Integer ACCOUNT_ROLLBACK = 6;// 账户回滚
	public static final Integer REFOUND = 7;

	public static final Integer aliPay = 0;
	public static final Integer weixinPay = 1;
	public static final Integer yuePay = 2;
	public static final Integer mixPay = 3;

	public static final Integer ALL_LOTERRY_TYPE = 0;

	// 验证码
	public static final String VERIFY_TYPE_LOGIN = "0";
	public static final String VERIFY_TYPE_REG = "1";
	public static final String VERIFY_TYPE_FORGET = "2";
	public static final String VERIFY_TYPE_SERVICE = "3";

	public static final String BANKCARD_MATCH = "1";
	public static final String BANKCARD_NOT_MATCH = "2";

	// 业务类型
	public static final Integer REGISTER = 1;
	// 西安活动送红包
	public static final Integer XNREGISTER = 2;

	// ------红包使用范围------//
	// 全场通用
	public static final Integer BONUS_USE_RANGE_ALL = 0;

	// 一天的秒数
	public static final Integer OneDaySecond = 86400;

	// 红包快过期标识:快过期,未生效
	public static final String BONUS_SOONEXPIREBZ_NOTHIDE = "1";
	public static final String BONUS_SOONEXPIREBZ_HIDE = "0";
	public static final String BONUS_NOWORK = "2";

	// 文章收藏
	// 用户已收藏
	public static final String IS_COLLECTED = "1";

	// 用户未收藏
	public static final String IS_NOT_COLLECT = "0";

	// 银行卡默认和非默认
	public static final String BANK_DEFAULT = "1";
	public static final String BANK_NOT_DEFAULT = "0";

	public static final Integer REWARD_AUTO = 1;
	public static final Integer REWARD_MANUAL = 2;
	public static final String FORMAT_WINNING_MSG = "恭喜【{0}】投注竞足中奖";
	
	public static final Integer RECHARGE_ACT = 1;
	
	//账户明细的时间段
	public static final String ALL_TIME = "0";
	public static final String TODAY = "1";
	public static final String BEFORE_6_DAY = "2";
	public static final String BEFORE_29_DAY = "3";
	public static final String BEFORE_89_DAY = "4";
	
	//银行卡用途
	public static final Integer BANK_PURPOSE_WITHDRAW = 0;
	public static final Integer BANK_PURPOSE_PAY = 1;
	
	//交易版开关
	public static final Integer BISINESS_APP_CLOSE = 0;
	public static final Integer BISINESS_APP_OPEN = 1;

}
