package com.sky.mapper;

import com.sky.entity.TransactionRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionRecordMapper {
    /**
     * 插入一条交易记录
     * @param transactionRecord
     */
    void insert(TransactionRecord transactionRecord);

    /**
     * 根据商户系统内部订单号删除交易记录
     * @param outTradeNo
     */
    @Delete("delete from transaction_record where out_trade_no = #{outTradeNo}")
    void deleteByOutTradeNo(String outTradeNo);
}
