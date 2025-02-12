package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取指定时间内的营业额数据
     * @param begin 开始时间
     * @param end 结束时间
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>(); // 存放从begin到end的每天
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 查询每天的营业额数据
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询date日期对应的营业额数据
            // 状态为“已完成”的订单的金额合计
            // select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            // 当天开始时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            // 当天结束时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnoverStatistics = orderMapper.sumByMap(map);
            // 处理数据
            if (turnoverStatistics == null){
                turnoverStatistics = 0.0;
            }
            turnoverList.add(turnoverStatistics);
        }

        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(StringUtils.join(dateList, ",")); //设置日期
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList, ",")); // 设置营业额
        return turnoverReportVO;
    }


    /**
     * 获取指定时间内的用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>(); // 存放从begin到end的每天
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 存放每天的新增用户数量
        // select count(id) from user where create_time < ? and create_time > ?
        List<Integer> newUserList = new ArrayList<>();
        // 存放每天的用户总量
        // select count(id) from user where create_time < ?
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("end", endTime);
            // 先查询截止当天的总用户数量
            Integer totalCount = userMapper.countByMap(map);
            totalCount = totalCount == null ? 0 : totalCount; // 为空则置为0
            totalUserList.add(totalCount);
            // 查询当天新增的用户数量
            map.put("begin", beginTime);
            Integer newCount = userMapper.countByMap(map);
            newCount = newCount == null ? 0 : newCount;
            newUserList.add(newCount);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }


    /**
     * 获取指定时间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>(); // 存放从begin到end的每天
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        // 遍历集合，查询每天有效订单数和订单总数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查询每天的订单总数
            // select count(id) from orders where order_time > ? and order_time < ?
            orderCountList.add(getOrderCount(beginTime, endTime, null));

            // 查询每天的有效订单(已完成)数
            // select count(id) from orders where order_time > ? and order_time < ? and status = 5
            validOrderCountList.add(getOrderCount(beginTime, endTime, Orders.COMPLETED));
        }
        // 订单总数
        Integer totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        // 有效订单数
        Integer validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        // 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .validOrderCount(validOrderCount)
                .totalOrderCount(totalOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        Integer count = orderMapper.countByMap(map);
        return count == null ? 0 : count;
    }


    /**
     * 查询指定时间内的销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        // select od.name, sum(od.number) number from order_detail od
        // join orders o on od.order_id = o.id
        // where o.status = 5 and o.order_time > ? and o.order_time < ?
        // group by od.name
        // order by number desc
        // limit 0,10
        List<GoodsSalesDTO> list = orderMapper.getSalesTop10(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));
        List<String> nameList = list.stream().map(GoodsSalesDTO::getName).toList();
        List<Integer> salesList = list.stream().map(GoodsSalesDTO::getNumber).toList();
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(salesList, ","))
                .build();
    }
}
