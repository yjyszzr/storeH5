package com.dl.store.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
@Data
@Table(name = "dl_order")
public class Order {
    /**
     * 订单id
     */
    @Id
    @Column(name = "order_id")
    private Integer orderId;

    /**
     * 订单号
     */
    @Column(name = "order_sn")
    private String orderSn;

    /**
     * 父订单号
     */
    @Column(name = "parent_sn")
    private String parentSn;

    /**
     * 买家id
     */
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 订单状态:0-待开奖,1-未中奖,2-已中奖
     */
    @Column(name = "order_status")
    private Integer orderStatus;
    
    /**
     * 订单出票状态:1-待出票,2-部分出票失败已退款 3-全部出票失败已退款 4 出票成功
     */
    @Column(name = "print_lottery_status")
    private Integer printLotteryStatus;
    
    /**
     * 订单出票失败退款金额
     */
    @Column(name = "print_lottery_refund_amount")
    private BigDecimal printLotteryRefundAmount;
    
    

    /**
     * 支付状态
     */
    @Column(name = "pay_status")
    private Integer payStatus;

    /**
     * 支付id
     */
    @Column(name = "pay_id")
    private Integer payId;

    /**
     * 支付代码
     */
    @Column(name = "pay_code")
    private String payCode;

    /**
     * 支付名称
     */
    @Column(name = "pay_name")
    private String payName;

    @Column(name = "pay_sn")
    private String paySn;

    /**
     * 订单实付金额
     */
    @Column(name = "money_paid")
    private BigDecimal moneyPaid;

    /**
     * 彩票总金额
     */
    @Column(name = "ticket_amount")
    private BigDecimal ticketAmount;

    /**
     * 余额支付
     */
    private BigDecimal surplus;

    /**
     * 可提现余额支付
     */
    @Column(name = "user_surplus")
    private BigDecimal userSurplus;

    /**
     * 不可提现余额支付
     */
    @Column(name = "user_surplus_limit")
    private BigDecimal userSurplusLimit;
    
    /**
     * 第三方支付金额
     */
    @Column(name = "third_party_paid")
    private BigDecimal thirdPartyPaid;

    /**
     * 用户红包id
     */
    @Column(name = "user_bonus_id")
    private Integer userBonusId;

    /**
     * 用户红包金额
     */
    private BigDecimal bonus;

    /**
     * 订单赠送的积分
     */
    @Column(name = "give_integral")
    private Integer giveIntegral;

    /**
     * 订单来源
     */
    @Column(name = "order_from")
    private String orderFrom;
    
    /**
     * 彩票照片url
     */
    @Column(name = "pic")
    private String pic;

    /**
     * 订单生成时间
     */
    @Column(name = "add_time")
    private Integer addTime;

    /**
     * 订单支付时间
     */
    @Column(name = "pay_time")
    private Integer payTime;



	/**
     * 订单类型
     */
    @Column(name = "order_type")
    private Integer orderType;
    
    /**
     * 彩票种类id
     */
    @Column(name = "lottery_classify_id")
    private Integer lotteryClassifyId;
    
    
    @Column(name = "device_channel")
    private String deviceChannel;
    
    /**
     * 彩票子分类
     */
    @Column(name = "lottery_play_classify_id")
    private Integer lotteryPlayClassifyId;
    
    @Column(name = "award_time")
    private Integer awardTime;
    
    @Column(name = "store_id")
    private Integer storeId;
    
	/**
     * 比赛时间
     */
    @Column(name = "match_time")
    private Date matchTime;
    
    /**
     * 中奖金额
     */
    @Column(name = "winning_money")
    private BigDecimal winningMoney;
    
    /**
     * 过关方式
     */
    @Column(name = "pass_type")
    private String passType;
    
    /**
     * 玩法
     */
    @Column(name = "play_type")
    private String playType;
    
    /**
     * 投注倍数
     */
    @Column(name = "cathectic")
    private Integer cathectic; 
    
    /**
     * 投注倍数
     */
    @Column(name = "bet_num")
    private Integer betNum;
    
    /**
     * 店铺接单时间（出票开始时间）
     */
    @Column(name = "accept_time")
    private Integer acceptTime;
    
    /**
     * 出票时间
     */
    @Column(name = "ticket_time")
    private Integer ticketTime;
    
    /**
     * 预测奖金
     */
    @Column(name = "forecast_money")
    private String forecastMoney;
    
    /**
     * 投注最后一场比赛期次
     */
    @Column(name = "issue")
    private String issue;

    /**
     * 是否彻底删除
     */
    @Column(name = "is_delete")
    private Integer isDelete;
    
    @Column(name = "ticket_num")
    private Integer ticketNum;
    
    @Column(name = "mobile")
    private String mobile;


    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public String getParentSn() {
        return parentSn;
    }

    public void setParentSn(String parentSn) {
        this.parentSn = parentSn;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getPrintLotteryStatus() {
        return printLotteryStatus;
    }

    public void setPrintLotteryStatus(Integer printLotteryStatus) {
        this.printLotteryStatus = printLotteryStatus;
    }

    public BigDecimal getPrintLotteryRefundAmount() {
        return printLotteryRefundAmount;
    }

    public void setPrintLotteryRefundAmount(BigDecimal printLotteryRefundAmount) {
        this.printLotteryRefundAmount = printLotteryRefundAmount;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public Integer getPayId() {
        return payId;
    }

    public void setPayId(Integer payId) {
        this.payId = payId;
    }

    public String getPayCode() {
        return payCode;
    }

    public void setPayCode(String payCode) {
        this.payCode = payCode;
    }

    public String getPayName() {
        return payName;
    }

    public void setPayName(String payName) {
        this.payName = payName;
    }

    public String getPaySn() {
        return paySn;
    }

    public void setPaySn(String paySn) {
        this.paySn = paySn;
    }

    public BigDecimal getMoneyPaid() {
        return moneyPaid;
    }

    public void setMoneyPaid(BigDecimal moneyPaid) {
        this.moneyPaid = moneyPaid;
    }

    public BigDecimal getTicketAmount() {
        return ticketAmount;
    }

    public void setTicketAmount(BigDecimal ticketAmount) {
        this.ticketAmount = ticketAmount;
    }

    public BigDecimal getSurplus() {
        return surplus;
    }

    public void setSurplus(BigDecimal surplus) {
        this.surplus = surplus;
    }

    public BigDecimal getUserSurplus() {
        return userSurplus;
    }

    public void setUserSurplus(BigDecimal userSurplus) {
        this.userSurplus = userSurplus;
    }

    public BigDecimal getUserSurplusLimit() {
        return userSurplusLimit;
    }

    public void setUserSurplusLimit(BigDecimal userSurplusLimit) {
        this.userSurplusLimit = userSurplusLimit;
    }

    public BigDecimal getThirdPartyPaid() {
        return thirdPartyPaid;
    }

    public void setThirdPartyPaid(BigDecimal thirdPartyPaid) {
        this.thirdPartyPaid = thirdPartyPaid;
    }

    public Integer getUserBonusId() {
        return userBonusId;
    }

    public void setUserBonusId(Integer userBonusId) {
        this.userBonusId = userBonusId;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public Integer getGiveIntegral() {
        return giveIntegral;
    }

    public void setGiveIntegral(Integer giveIntegral) {
        this.giveIntegral = giveIntegral;
    }

    public String getOrderFrom() {
        return orderFrom;
    }

    public void setOrderFrom(String orderFrom) {
        this.orderFrom = orderFrom;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Integer getAddTime() {
        return addTime;
    }

    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }

    public Integer getPayTime() {
        return payTime;
    }

    public void setPayTime(Integer payTime) {
        this.payTime = payTime;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Integer getLotteryClassifyId() {
        return lotteryClassifyId;
    }

    public void setLotteryClassifyId(Integer lotteryClassifyId) {
        this.lotteryClassifyId = lotteryClassifyId;
    }

    public String getDeviceChannel() {
        return deviceChannel;
    }

    public void setDeviceChannel(String deviceChannel) {
        this.deviceChannel = deviceChannel;
    }

    public Integer getLotteryPlayClassifyId() {
        return lotteryPlayClassifyId;
    }

    public void setLotteryPlayClassifyId(Integer lotteryPlayClassifyId) {
        this.lotteryPlayClassifyId = lotteryPlayClassifyId;
    }

    public Integer getAwardTime() {
        return awardTime;
    }

    public void setAwardTime(Integer awardTime) {
        this.awardTime = awardTime;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Date getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(Date matchTime) {
        this.matchTime = matchTime;
    }

    public BigDecimal getWinningMoney() {
        return winningMoney;
    }

    public void setWinningMoney(BigDecimal winningMoney) {
        this.winningMoney = winningMoney;
    }

    public String getPassType() {
        return passType;
    }

    public void setPassType(String passType) {
        this.passType = passType;
    }

    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    public Integer getCathectic() {
        return cathectic;
    }

    public void setCathectic(Integer cathectic) {
        this.cathectic = cathectic;
    }

    public Integer getBetNum() {
        return betNum;
    }

    public void setBetNum(Integer betNum) {
        this.betNum = betNum;
    }

    public Integer getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(Integer acceptTime) {
        this.acceptTime = acceptTime;
    }

    public Integer getTicketTime() {
        return ticketTime;
    }

    public void setTicketTime(Integer ticketTime) {
        this.ticketTime = ticketTime;
    }

    public String getForecastMoney() {
        return forecastMoney;
    }

    public void setForecastMoney(String forecastMoney) {
        this.forecastMoney = forecastMoney;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getTicketNum() {
        return ticketNum;
    }

    public void setTicketNum(Integer ticketNum) {
        this.ticketNum = ticketNum;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}