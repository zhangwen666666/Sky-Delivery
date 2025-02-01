package com.sky.service;

import com.sky.entity.PrepayOrder;

public interface PrepayOrderService {
    /**
     * 保存预支付交易单到数据库
     * @param prepayOrder
     */
    void save(PrepayOrder prepayOrder);

    /**
     * 根据id查询预支付交易单
     * @param id
     * @return
     */
    PrepayOrder getById(Long id);


    /**
     * 删除预支付交易单
     * @param prepayOrder
     */
    void removeById(PrepayOrder prepayOrder);

}
