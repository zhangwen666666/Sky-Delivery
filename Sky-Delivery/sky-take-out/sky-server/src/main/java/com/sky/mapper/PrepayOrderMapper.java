package com.sky.mapper;

import com.sky.entity.PrepayOrder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrepayOrderMapper {
    /**
     * 插入一条预支付交易单
     * @param prepayOrder
     */
    void insert(PrepayOrder prepayOrder);

    /**
     * 根据id查询预支付交易单
     * @param id
     * @return
     */
    PrepayOrder selectById(Long id);


    /**
     * 删除预支付交易记录
     * @param id
     */
    @Delete("delete from prepay_order where id = #{id}")
    void deleteById(Long id);
}
