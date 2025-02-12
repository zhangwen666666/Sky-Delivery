package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.utils.NumberUtil;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 查询今日运营数据
     * @return
     */
    @Override
    public BusinessDataVO businessData(LocalDateTime begin, LocalDateTime end) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        // 新增用户数
        Integer newUserCount = NumberUtil.check(userMapper.countByMap(map));
        // 总订单数
        Integer totalOrderCount = NumberUtil.check(orderMapper.countByMap(map));
        // 营业额
        map.put("status", Orders.COMPLETED);
        Double turnover = NumberUtil.check(orderMapper.sumByMap(map));
        // 有效订单数
        Integer validOrderCount = NumberUtil.check(orderMapper.countByMap(map));
        // 平均客单价
        Double unitPrice = 0.0;
        // 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0){
            unitPrice = turnover / validOrderCount;
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return BusinessDataVO.builder()
                .newUsers(newUserCount)
                .turnover(turnover)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .validOrderCount(validOrderCount)
                .build();
    }


    /**
     * 查询订单管理数据
     * @return
     */
    @Override
    public OrderOverViewVO overviewOrders() {
        Map<String, Object> map = new HashMap();
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));
        map.put("status", Orders.TO_BE_CONFIRMED);

        //待接单
        Integer waitingOrders = NumberUtil.check(orderMapper.countByMap(map));

        //待派送
        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = NumberUtil.check(orderMapper.countByMap(map));

        //已完成
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = NumberUtil.check(orderMapper.countByMap(map));

        //已取消
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = NumberUtil.check(orderMapper.countByMap(map));

        //全部订单
        map.put("status", null);
        Integer allOrders = NumberUtil.check(orderMapper.countByMap(map));

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }


    /**
     * 查询菜品总览
     * @return
     */
    @Override
    public DishOverViewVO overviewDishes() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = NumberUtil.check(dishMapper.countByMap(map));

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = NumberUtil.check(dishMapper.countByMap(map));

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }


    /**
     * 查询套餐总览
     * @return
     */
    @Override
    public SetmealOverViewVO overviewSetmeals() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = NumberUtil.check(setmealMapper.countByMap(map));

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = NumberUtil.check(setmealMapper.countByMap(map));

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
