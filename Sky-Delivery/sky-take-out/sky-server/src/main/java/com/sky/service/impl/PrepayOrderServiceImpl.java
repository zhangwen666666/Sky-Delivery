package com.sky.service.impl;

import com.sky.entity.PrepayOrder;
import com.sky.mapper.PrepayOrderMapper;
import com.sky.service.PrepayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrepayOrderServiceImpl implements PrepayOrderService {
    @Autowired
    private PrepayOrderMapper prepayOrderMapper;

    /**
     * 保存预支付交易单到数据库
     * 自动生成自增主键id并赋值给prepayOrder的id属性
     * @param prepayOrder
     */
    @Override
    public void save(PrepayOrder prepayOrder) {
        prepayOrderMapper.insert(prepayOrder);
        System.out.println(prepayOrder);
    }


    /**
     * 根据id查询预支付交易单
     * @param id
     * @return
     */
    @Override
    public PrepayOrder getById(Long id) {
        return prepayOrderMapper.selectById(id);
    }

    /**
     * 删除预支付交易单
     * @param prepayOrder
     */
    @Override
    public void removeById(PrepayOrder prepayOrder) {
        prepayOrderMapper.deleteById(prepayOrder.getId());
    }
}
