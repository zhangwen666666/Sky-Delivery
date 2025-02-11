package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private TransactionRecordMapper transactionRecordMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        // 处理业务异常（地址簿为空、购物车为空）
        List<AddressBook> addressBooks = addressBookMapper.select(AddressBook.builder().id(ordersSubmitDTO.getAddressBookId()).build());
        if (addressBooks == null || addressBooks.isEmpty()) {
            // 地址不存在
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        AddressBook addressBook = addressBooks.get(0);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            // 购物车为空
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getDetail());
        orderMapper.insert(orders);

        // 向订单明细表中插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetail.setId(null); // 订单明细的主键值要自增
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // 清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        // 封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(orders.getId()).orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber()).orderTime(orders.getOrderTime()).build();

        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        // 这里我们模拟实现，最终调用到的事PayController中的方法
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }


    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo 订单号
     */
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // 通过webSocket向客户端浏览器推送消息
        // 推送Json格式的串，包含字段：type，orderId，content
        Map<String, Object> map = new HashMap();
        map.put("type", 1); // 1表示来单提醒，2表示客户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }


    /**
     * 分页查询历史订单
     *
     * @param pageNum
     * @param pageSize
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    @Override
    public PageResult historyOrders(int pageNum, int pageSize, Integer status) {
        // 设置分页
        PageHelper.startPage(pageNum, pageSize);

        // 设置分页查询条件
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        // 分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        // 封装分页查询结果集合
        List<OrderVO> list = new ArrayList<>();

        if (page != null && !page.isEmpty()) {
            for (Orders orders : page) {
                Long orderId = orders.getId();
                // 根据订单id查询订单明细
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                // 封装结果集中的每一个数据
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }


    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void cancelById(Long id) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            // 抛出订单不存在异常
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        // 只有待付款和待接单的订单可以取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            /*weChatPayUtil.refund(
                    ordersDB.getNumber(), // 商户订单号
                    ordersDB.getNumber(), // 商户退款单号
                    ordersDB.getAmount(), // 退款金额，单位元
                    ordersDB.getAmount()); // 原订单金额*/

            // 模拟退款，直接删除交易记录表中存储的交易记录即可
            transactionRecordMapper.deleteByOutTradeNo(ordersDB.getNumber());

            // 支付状态修改为退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        // 将订单详情转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车重新批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }


    /**
     * 根据条件搜索订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 设置分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        // 分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        // 封装分页查询结果集合
        List<OrderVO> list = new ArrayList<>();

        if (page != null && !page.isEmpty()) {
            for (Orders orders : page) {
                // 封装结果集中的每一个数据
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                // 根据订单id查询订单明细
                Long orderId = orders.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
                List<String> collect = orderDetailList.stream().map(x -> x.getName() + "*" + x.getNumber() + ";").collect(Collectors.toList());
                String orderDishes = String.join("", collect);
                orderVO.setOrderDishes(orderDishes);
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }


    /**
     * 统计各个状态的订单数量
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        // 根据不同的状态查询数据库，得到每个状态的订单数量
        Integer toBeConfirmed = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);

        // 封装返回结果
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }


    /**
     * 接单 将订单id为id的订单的状态修改为已接单
     *
     * @param id
     */
    @Override
    public void confirm(Long id) {
        Orders orders = Orders.builder().id(id).status(Orders.CONFIRMED).build();
        orderMapper.update(orders);
    }


    /**
     * 拒单，将订单的状态修改为已取消，
     * 只有待接单的订单可以拒单
     * 如果用户完成了支付，需要退款
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 如果订单不存在或者状态不是未接单，则抛出异常
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 如果已经支付了，需要退款
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus.equals(Orders.PAID)) {
            // 退款
            // 将订单从支付记录表transaction_record中删除
            transactionRecordMapper.deleteByOutTradeNo(ordersDB.getNumber());
            log.info("申请退款：订单号为{}", ordersDB.getNumber());
            // 设置支付状态
            ordersDB.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orders.setPayStatus(ordersDB.getPayStatus());
        orderMapper.update(orders);
    }


    /**
     * 商家端取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (Objects.equals(payStatus, Orders.PAID)) {
            // 退款
            // 从transaction_record表中删除支付记录
            transactionRecordMapper.deleteByOutTradeNo(ordersDB.getNumber());
            // 设置支付状态
            ordersDB.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setPayStatus(ordersDB.getPayStatus());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 只有已接单（待派送）的订单才能派送
        if (!ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        // 更新数据库
        orderMapper.update(orders);
    }


    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 只有派送中的订单才能完成
        if (!ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        // 更新数据库
        orderMapper.update(orders);
    }


    /**
     * 客户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2); // 2表示客户催单，1表示来单提醒
        map.put("content", "订单号：" + ordersDB.getNumber());
        map.put("orderId", id);
        String json = JSON.toJSONString(map);

        // 通过webSocket向客户端浏览器推送消息
        webSocketServer.sendToAllClient(json);
    }
}
