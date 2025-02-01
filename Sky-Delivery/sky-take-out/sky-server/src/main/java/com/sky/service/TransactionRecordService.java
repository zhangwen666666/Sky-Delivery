package com.sky.service;

import com.sky.entity.TransactionRecord;

public interface TransactionRecordService {
    /**
     * 保存交易记录
     * @param transactionRecord
     */
    void save(TransactionRecord transactionRecord);
}
