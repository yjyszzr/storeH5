package com.dl.store.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
@Data
@Table(name = "dl_artifi_print_lottery")
public class DlArtifiPrintLottery {
    /**
     * id
     */
    @Id
    @Column(name = "id")
    private Integer id;

    /**
     * 订单号
     */
    @Column(name = "order_sn")
    private String orderSn;
    
    
    /**
     * 实际付款金额
     */
    @Column(name = "money_paid")
    private BigDecimal moneyPaid;

    /**
     * 状态0待确认1出票成功2出票失败,默认为待确认状态
     */
    @Column(name = "order_status")
    private Byte orderStatus;
    
    
    /**
     * 状态0待确认1出票成功2出票失败,默认为待确认状态
     */
    @Column(name = "statistics_paid")
    private Integer statisticsPaid;

    /**
     * 轮询状态:0未轮询,1已轮询,默认未轮询
     */
    @Column(name = "operation_status")
    private Byte operationStatus;

    /**
     * 添加时间
     */
    @Column(name = "add_time")
    private Integer addTime;

    /**
     * 操作人ID
     */
    @Column(name = "admin_id")
    private Integer adminId;

    /**
     * 操作人名称
     */
    @Column(name = "admin_name")
    private String adminName;

	/**
     * 操作时间
     */
    @Column(name = "operation_time")
    private Integer operationTime;
  
    /**
     * 店铺Id
     */
    @Column(name = "store_id")
    private Integer storeId;
    
    
}