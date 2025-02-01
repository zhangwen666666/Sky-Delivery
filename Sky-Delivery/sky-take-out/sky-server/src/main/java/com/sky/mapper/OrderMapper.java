package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    /**
     * 向订单表中插入数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据交易单号查找订单记录
     * @param number
     * @return
     */
    @Select("select * from orders where number = #{number}")
    Orders getByNumber(String number);

    /**
     * 动态更新订单记录
     * @param orders
     */
    void update(Orders orders);
}
