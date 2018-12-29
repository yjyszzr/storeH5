package com.dl.store.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Table(name = "dl_store")
@Data
public class Store {
	/**
	 * 主键自增ID
	 */
    @Id
    @Column(name = "id")
    private Integer storeId;

    /**
     * name
     */
    @Column(name = "name")
    private String name;
    
    /**
     * logo
     */
    @Column(name = "logo")
    private String logo;
    
    /**
     * wechat
     */
    @Column(name = "wechat")
    private String wechat;
    
    /**
     * phone
     */
    @Column(name = "phone")
    private String phone;
    
    /**
     * 地址图片
     */
    @Column(name = "addr_pic")
    private String addrPic;

    /**
     * 地址图片
     */
    @Column(name = "store_pic")
    private String storePic;
    
    /**
     * 添加时间
     */
    @Column(name = "add_time")
    private Integer addTime;
}
