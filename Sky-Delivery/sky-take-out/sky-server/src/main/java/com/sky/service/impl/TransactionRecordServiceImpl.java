package com.sky.service.impl;

import com.sky.entity.TransactionRecord;
import com.sky.mapper.TransactionRecordMapper;
import com.sky.service.TransactionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionRecordServiceImpl implements TransactionRecordService {
    @Autowired
    private TransactionRecordMapper transactionRecordMapper;

    /**
     * 保存交易记录
     * @param transactionRecord
     */
    @Override
    public void save(TransactionRecord transactionRecord) {
        transactionRecordMapper.insert(transactionRecord);
    }
}
