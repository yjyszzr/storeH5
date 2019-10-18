package com.dl.store.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Table(name = "dl_user_store_money")
@Data
public class UserStoreMoney {
	/**
	 * 主键自增ID
	 */
    @Id
    @Column(name = "id")
    private Integer id;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Integer userId;
    
    /**
     * store ID
     */
    @Column(name = "store_id")
    private Integer storeId;
    
    /**
     * money
     */
    @Column(name = "money")
    private BigDecimal money;
    
    @Column(name = "money_limit")
    private BigDecimal moneyLimit;
    
    /**
     * 最后变更时间
     */
    @Column(name = "last_time")
    private Integer lastTime;
}
