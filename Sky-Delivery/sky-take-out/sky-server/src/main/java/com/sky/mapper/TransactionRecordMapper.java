package com.sky.mapper;

import com.sky.entity.TransactionRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionRecordMapper {
    /**
     * 插入一条交易记录
     * @param transactionRecord
     */
    void insert(TransactionRecord transactionRecord);
}
